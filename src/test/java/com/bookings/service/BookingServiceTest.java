package com.bookings.service;

import com.bookings.exception.BusinessException;
import com.bookings.exception.ErrorCode;
import com.bookings.exception.PropertyBookingBlockedException;
import com.bookings.exception.PropertyUnavailableException;
import com.bookings.models.*;
import com.bookings.repository.BlockRepository;
import com.bookings.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.Month;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookingServiceTest {
    @Autowired
    BookingService bookingService;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    BlockRepository blockRepository;

    @BeforeEach
    public void cleanDB() {
        bookingRepository.deleteAll();
        blockRepository.deleteAll();
    }

    @ParameterizedTest
    @CsvSource({
            "2024-02-01, 2024-02-05",
            "2024-02-02, 2024-02-10"
    })
    @DisplayName("Should create a booking with success")
    public void shouldCreateABooking(ArgumentsAccessor arguments) {
        var booking = validNewBooking(arguments.get(0, LocalDate.class), arguments.get(1, LocalDate.class));
        assertDoesNotThrow(() -> {
            Booking persistedBooking = bookingService.createBooking(booking);
            assertThat(booking, is(equalTo(persistedBooking)));
        });
    }

    @ParameterizedTest
    @CsvSource({
            "2024-02-01, 2024-02-05, 2024-01-25, 2024-02-10",
            "2024-02-01, 2024-02-05, 2024-01-25, 2024-02-10"
    })
    @DisplayName("Should prevent create a booking when the property is blocked")
    public void shouldPreventCreateABookingWhenThePropertyIsBlocked(ArgumentsAccessor arguments) {
        var checkInDate = arguments.get(0, LocalDate.class);
        var checkOutDate = arguments.get(1, LocalDate.class);
        var blockStartDate = arguments.get(2, LocalDate.class);
        var blockEndDate = arguments.get(3, LocalDate.class);
        var booking = validNewBooking(checkInDate, checkOutDate);
        createBlock(blockStartDate, blockEndDate, booking);
        PropertyBookingBlockedException exception = assertThrowsExactly(PropertyBookingBlockedException.class, () -> bookingService.createBooking(booking));
        assertThat(exception.getErrorCode(), is(ErrorCode.PROPERTY_BOOKING_BLOCKED));
    }

    @ParameterizedTest
    @CsvSource({
            "2024-02-01, 2024-02-01",
            "2024-02-08, 2024-02-07",
            "2025-02-08, 2024-02-07",
    })
    @DisplayName("Should prevent create a booking when booking dates are not valid")
    public void shouldPreventCreateABookingWhenBookingDatesAreNotValid(ArgumentsAccessor arguments) {
        var checkInDate = arguments.get(0, LocalDate.class);
        var checkOutDate = arguments.get(1, LocalDate.class);
        var booking = validNewBooking(checkInDate, checkOutDate);
        BusinessException exception = assertThrows(BusinessException.class, () -> bookingService.createBooking(booking));
        assertThat(exception.getErrorCode(), is(ErrorCode.INVALID_BOOKING_DATES));
    }

    @Test
    @DisplayName("Should prevent a booking of a property already booked")
    public void shouldPreventBookingPropertyAlreadyBooked() {
        var firstBooking = validNewBooking(Month.APRIL);
        Booking persistedBooking = bookingService.createBooking(firstBooking);
        assertThat(firstBooking, is(equalTo(persistedBooking)));

        var secondBooking = validNewBooking(Month.APRIL);
        BusinessException businessException = assertThrowsExactly(PropertyUnavailableException.class, () -> bookingService.createBooking(secondBooking));
        assertThat(businessException.getMessage(),
                is(equalTo(PropertyUnavailableException.ERROR_MESSAGE_PATTERN.formatted(secondBooking.getCheckInDate(), secondBooking.getCheckOutDate()))));
        assertThat(businessException.getErrorCode(), is(equalTo(ErrorCode.PROPERTY_UNAVAILABLE)));
    }

    @Test
    @DisplayName("Should cancel a booking")
    public void shouldCancelABooking() {
        var booking = validNewBooking(Month.FEBRUARY);
        Booking persistedBooking = bookingService.createBooking(booking);
        assertThat(persistedBooking.isCanceled(), is(false));
        Long persistedBookingId = persistedBooking.getId();
        bookingService.cancelBooking(persistedBookingId);
        var canceledBooking = bookingService.getBooking(persistedBookingId).get();
        assertThat(canceledBooking.isCanceled(), is(true));
    }

    @Test
    @DisplayName("Should rebook a canceled booking")
    public void shouldRebookACanceledBooking() {
        // Create a canceled booking
        var originalCheckInDate = LocalDate.of(2024, 3, 5);
        var originalCheckOutDate = LocalDate.of(2024, 3, 10);
        var booking = validNewBooking(originalCheckInDate, originalCheckOutDate);
        booking.setCanceled(true);
        Booking canceledBooking = bookingRepository.save(booking);
        assertThat(canceledBooking.isCanceled(), is(true));

        // Create a block
        var rebookedCheckInDate = originalCheckOutDate.plusDays(5);
        var rebookedCheckOutDate = rebookedCheckInDate.plusDays(3);
        canceledBooking.setCheckInDate(rebookedCheckInDate);
        canceledBooking.setCheckOutDate(rebookedCheckOutDate);

        // Try to rebook
        assertDoesNotThrow(() -> {
            var rebooked = bookingService.rebook(canceledBooking);
            assertThat(rebooked.isCanceled(), is(false));
            assertThat(rebooked.hasValidBookingDates(), is(true));
        });
    }

    @Test
    @DisplayName("Should prevent rebook a booking from a blocked property")
    public void shouldPreventRebookACanceledBooking() {
        // Create a canceled booking
        var originalCheckInDate = LocalDate.of(2024, 3, 5);
        var originalCheckOutDate = LocalDate.of(2024, 3, 10);
        var booking = validNewBooking(originalCheckInDate, originalCheckOutDate);
        booking.setCanceled(true);
        Booking canceledBooking = bookingRepository.save(booking);
        assertThat(canceledBooking.isCanceled(), is(true));

        // Create a block
        LocalDate blockStartDate = LocalDate.of(2024, 3, 15);
        LocalDate blockEndDate = LocalDate.of(2024, 3, 25);
        createBlock(blockStartDate, blockEndDate, booking);

        // Try to rebook
        var rebookedCheckInDate = blockStartDate.plusDays(5);
        var rebookedCheckOutDate = rebookedCheckInDate.plusDays(5);
        canceledBooking.setCheckInDate(rebookedCheckInDate);
        canceledBooking.setCheckOutDate(rebookedCheckOutDate);
        PropertyBookingBlockedException exception = assertThrowsExactly(PropertyBookingBlockedException.class, () -> bookingService.rebook(canceledBooking), PropertyBookingBlockedException.ERROR_MESSAGE_PATTERN.formatted(
                booking.getPropertyId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate()));
        assertThat(exception.getErrorCode(), is(ErrorCode.PROPERTY_BOOKING_BLOCKED));
    }

    @Test
    @DisplayName("Should get a booking by id")
    public void shouldGetABooking() {
        var newBooking = validNewBooking(Month.DECEMBER);
        Booking persistedBooking = bookingService.createBooking(newBooking);
        assertThat(newBooking, is(equalTo(persistedBooking)));
        var booking = bookingService.getBooking(persistedBooking.getId()).get();
        assertThat(booking, is(equalTo(persistedBooking)));
    }

    @Test
    @DisplayName("Should delete a booking")
    public void shouldDeleteABooking() {
        var newBooking = validNewBooking(Month.SEPTEMBER);
        Booking persistedBooking = bookingService.createBooking(newBooking);
        assertThat(newBooking, is(equalTo(persistedBooking)));
        Long persistedBookingId = persistedBooking.getId();
        bookingService.deleteBooking(persistedBookingId);
        assertThat(bookingService.getBooking(persistedBookingId).isPresent(), is(false));
    }

    @Test
    @DisplayName("Should update a booking")
    public void shouldUpdateABooking() {
        var newBooking = validNewBooking(Month.OCTOBER);
        newBooking.setGuest(new Guest("Jimmy", "Leroy", 22, "123456"));
        LocalDate originalCheckInDate = LocalDate.of(2024, 5, 10);
        newBooking.setCheckInDate(originalCheckInDate);
        LocalDate originalCheckOutDate = originalCheckInDate.plusDays(5);
        newBooking.setCheckOutDate(originalCheckOutDate);
        Booking persistedBooking = bookingService.createBooking(newBooking);

        Guest guest = persistedBooking.getGuest();
        guest.setAge(25);
        guest.setFirstName("Jimmy");
        guest.setLastName("Potatoes");
        guest.setSocialSecurityId("654321");
        LocalDate updatedCheckInDate = persistedBooking.getCheckInDate().plusDays(2);
        LocalDate updatedCheckOutDate = persistedBooking.getCheckOutDate().plusDays(2);
        persistedBooking.setCheckInDate(updatedCheckInDate);
        persistedBooking.setCheckOutDate(updatedCheckOutDate);
        Booking updatedBooking = bookingService.updateBooking(persistedBooking);

        Guest updatedBookingGuest = updatedBooking.getGuest();
        assertThat(updatedBookingGuest.getAge(), is(25));
        assertThat(updatedBookingGuest.getFirstName(), is("Jimmy"));
        assertThat(updatedBookingGuest.getLastName(), is("Potatoes"));
        assertThat(updatedBookingGuest.getSocialSecurityId(), is("654321"));
        assertThat(updatedBooking.getCheckInDate(), is(updatedCheckInDate));
        assertThat(updatedBooking.getCheckOutDate(), is(updatedCheckOutDate));
    }

    private static Booking validNewBooking(Month month) {
        var booking = new Booking();
        booking.setProperty(new Property(1L, new Owner(1L)));
        LocalDate checkinDate = LocalDate.of(2024, month, 10);
        booking.setCheckInDate(checkinDate);
        LocalDate checkoutDate = LocalDate.of(2024, month, 15);
        booking.setCheckOutDate(checkoutDate);
        Guest guest = new Guest("Paul", "Leroy", 22, "123456");
        booking.setGuest(guest);
        return booking;
    }

    private void createBlock(LocalDate blockStartDate, LocalDate blockEndDate, Booking booking) {
        blockRepository.save(new Block(blockStartDate, blockEndDate, booking.getProperty()));
    }

    private Booking validNewBooking(LocalDate checkInDate, LocalDate checkOutDate) {
        var booking = new Booking();
        booking.setProperty(new Property(1L, new Owner(1L)));
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        Guest guest = new Guest("Paul", "Leroy", 22, "123456");
        booking.setGuest(guest);
        return booking;
    }
}