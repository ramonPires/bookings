package com.bookings.controller;

import com.bookings.exception.BusinessException;
import com.bookings.exception.ErrorCode;
import com.bookings.exception.PropertyBookingBlockedException;
import com.bookings.models.Booking;
import com.bookings.models.Guest;
import com.bookings.models.Property;
import com.bookings.service.BookingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static com.bookings.controller.ControllerTestUtils.TIMESTAMP_REGEX;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("Should fail to create a booking and return status code 400 if booking dates are invalid")
    public void shouldFailToCreateABookingIfBookingAreInvalid() throws Exception {
        Booking newBooking = validNewBooking(Month.FEBRUARY);
        newBooking.setCheckInDate(newBooking.getCheckInDate().minusYears(1));
        newBooking.setCheckOutDate(newBooking.getCheckInDate().minusWeeks(1));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newBooking))
                )
                .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.errors.checkInDate").value("CheckIn date need to be in the future"))
                .andExpect(jsonPath("$.errors.checkOutDate").value("CheckOut date need to be in the future"))
                .andExpect(status().isBadRequest());
        verify(bookingService, never()).createBooking(newBooking);
    }

    @Test
    @DisplayName("Should fail to create a booking and return status code 400 if booking guest is invalid")
    public void shouldFailToCreateABookingIfBookingGuestIsInvalid() throws Exception {
        Booking newBooking = validNewBooking(Month.FEBRUARY);
        newBooking.getGuest().setFirstName("A");
        newBooking.getGuest().setLastName("A");
        newBooking.getGuest().setAge(1);
        newBooking.getGuest().setSocialSecurityId(" ");


        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newBooking))
                )
                .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.errors['guest.age']").exists())
                .andExpect(jsonPath("$.errors['guest.firstName']").exists())
                .andExpect(jsonPath("$.errors['guest.lastName']").exists())
                .andExpect(jsonPath("$.errors['guest.socialSecurityId']").exists())
                .andExpect(status().isBadRequest());
        verify(bookingService, never()).createBooking(newBooking);
    }


    @Test
    @DisplayName("Should create a booking by and return a status code 201")
    public void shouldCreateABooking() throws Exception {
        var bookingId = 1L;
        Booking newBooking = validNewBooking(Month.FEBRUARY);
        Booking createdBooking = validNewBooking(Month.FEBRUARY);
        createdBooking.setId(bookingId);
        when(bookingService.createBooking(newBooking)).thenReturn(createdBooking);
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newBooking))
                )
                .andExpect(content().json(toJson(createdBooking)))
                .andExpect(status().isCreated());
        verify(bookingService).createBooking(newBooking);
    }

    @Test
    @DisplayName("Should try get a booking by id and return a status code 200")
    public void shouldGetABookingById() throws Exception {
        var booking = validNewBooking(Month.APRIL);
        booking.setId(1L);
        when(bookingService.getBooking(1L)).thenReturn(Optional.of(booking));
        mockMvc.perform(get("/bookings/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(booking)))
                .andExpect(status().isOk());
        verify(bookingService).getBooking(1L);
    }

    @Test
    @DisplayName("Should try to get a booking by id and return a status code 404 if couldn't find the booking")
    public void shouldTryToGetABookingByIdAndThrowNotFoundStatusCode() throws Exception {
        var booking = validNewBooking(Month.APRIL);
        booking.setId(1L);
        when(bookingService.getBooking(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/bookings/{id}", 1L))
                .andExpect(status().isNotFound());
        verify(bookingService).getBooking(1L);
    }

    @Test
    @DisplayName("Should cancel a booking by id and return a status code 200")
    public void shouldCancelABookingById() throws Exception {
        var bookingId = 1L;
        mockMvc.perform(delete("/bookings/{id}/cancel", bookingId))
                .andExpect(status().isOk());
        verify(bookingService).cancelBooking(bookingId);
    }

    @Test
    @DisplayName("Should delete a booking by id and return a status code 204")
    public void shouldDeleteABookingById() throws Exception {
        var bookingId = 1L;
        mockMvc.perform(delete("/bookings/{id}", bookingId))
                .andDo(print())
                .andExpect(status().isNoContent());
        verify(bookingService).deleteBooking(bookingId);
    }

    @Test
    @DisplayName("Should fail to update booking and return a status code 422 if booking dates are invalid")
    public void shouldFailToUpdateBooking() throws Exception {
        var bookingId = 1L;
        Booking booking = validNewBooking(Month.DECEMBER);
        booking.setCheckInDate(booking.getCheckOutDate().plusDays(5L));
        BusinessException businessException = new BusinessException("Booking dates are invalid", ErrorCode.INVALID_BOOKING_DATES);
        when(bookingService.updateBooking(any(Booking.class))).thenThrow(businessException);

        mockMvc.perform(put("/bookings/{id}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(booking)))
                .andDo(print())
                .andExpect(jsonPath("$.errorCode").value(businessException.getErrorCode().name()))
                .andExpect(jsonPath("$.message").value(businessException.getMessage()))
                .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)))
                .andExpect(status().isUnprocessableEntity());
        verify(bookingService).updateBooking(any(Booking.class));
    }

    @Test
    @DisplayName("Should fail to update booking and return a status code 422 if property is blocked")
    public void shouldFailToUpdateBookingIfPropertyIsBlocked() throws Exception {
        var bookingId = 1L;
        var booking = validNewBooking(Month.DECEMBER);
        var exception = new PropertyBookingBlockedException(booking);
        when(bookingService.updateBooking(any(Booking.class))).thenThrow(exception);
        mockMvc.perform(put("/bookings/{id}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(booking)))
                .andDo(print())
                .andExpect(jsonPath("$.errorCode").value(exception.getErrorCode().name()))
                .andExpect(jsonPath("$.message").value(exception.getMessage()))
                .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)))
                .andExpect(status().isUnprocessableEntity());
        verify(bookingService).updateBooking(any(Booking.class));
    }

    @Test
    @DisplayName("Should update booking and return a status code 200")
    public void shouldUpdateABooking() throws Exception {
        var bookingId = 1L;
        Booking booking = validNewBooking(Month.DECEMBER);
        when(bookingService.updateBooking(any(Booking.class))).thenReturn(booking);
        mockMvc.perform(put("/bookings/{id}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(booking)))
                .andDo(print())
                .andExpect(content().json(toJson(booking)))
                .andExpect(status().isOk());
        verify(bookingService).updateBooking(any(Booking.class));
    }

    @Test
    @DisplayName("Should rebook a booking and return a status code 200")
    public void shouldRebookABooking() throws Exception {
        var bookingId = 1L;
        Booking booking = validNewBooking(Month.DECEMBER);
        when(bookingService.rebook(any(Booking.class))).thenReturn(booking);
        mockMvc.perform(put("/bookings/{id}/rebook", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(booking)))
                .andDo(print())
                .andExpect(content().json(toJson(booking)))
                .andExpect(status().isOk());
        verify(bookingService).rebook(any(Booking.class));
    }

    private String toJson(Booking booking) throws JsonProcessingException {
        return objectMapper.writeValueAsString(booking);
    }

    private static Booking validNewBooking(Month month) {
        var booking = new Booking();
        booking.setProperty(new Property(1L));
        LocalDate checkinDate = LocalDate.of(2024, month, 10);
        booking.setCheckInDate(checkinDate);
        LocalDate checkoutDate = LocalDate.of(2024, month, 15);
        booking.setCheckOutDate(checkoutDate);
        Guest guest = new Guest("Paul", "Leroy", 22, "123456");
        booking.setGuest(guest);
        return booking;
    }
}