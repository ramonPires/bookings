package com.bookings.service;

import com.bookings.exception.BusinessException;
import com.bookings.exception.ErrorCode;
import com.bookings.exception.PropertyBookingBlockedException;
import com.bookings.exception.PropertyUnavailableException;
import com.bookings.models.Booking;
import com.bookings.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BlockService blockService;

    public BookingService(BookingRepository bookingRepository, BlockService blockService) {
        this.bookingRepository = bookingRepository;
        this.blockService = blockService;
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        validateBooking(booking);
        if (canBookAProperty(booking, false)) {
            return bookingRepository.save(booking);
        }
        throw new PropertyUnavailableException(booking);
    }

    public Booking rebook(Booking booking) {
        validateBooking(booking);
        if (canBookAProperty(booking, true)) {
            booking.setCanceled(false);
        }
        return this.bookingRepository.save(booking);
    }

    public void deleteBooking(Long bookingId) {
        this.bookingRepository.deleteById(bookingId);
    }

    @Transactional
    public Booking updateBooking(Booking bookingToUpdate) {
        validateBooking(bookingToUpdate);
        return bookingRepository.findById(bookingToUpdate.getId())
                .filter(b -> !b.isCanceled())
                .map(b -> bookingRepository.save(bookingToUpdate))
                .orElseThrow(() -> new PropertyUnavailableException(bookingToUpdate));
    }

    @Transactional(readOnly = true)
    public Optional<Booking> getBooking(Long id) {
        return bookingRepository.findById(id);
    }

    public void cancelBooking(Long id) {
        bookingRepository.findById(id)
                .map(booking -> {
                    booking.setCanceled(true);
                    return booking;
                }).map(bookingRepository::save);
    }

    private boolean canBookAProperty(Booking booking, boolean canceled) {
        return !isBooked(booking, canceled);
    }

    private void validateBooking(Booking booking) {
        hasValidBookingDates(booking);
        checkBlockedProperty(booking);
    }

    private boolean isBlocked(Booking booking) {
        return blockService.isBlocked(booking.getCheckInDate(), booking.getPropertyId());
    }

    private boolean isBooked(Booking booking, boolean canceled) {
        return bookingRepository.isBooked(booking.getCheckInDate(), booking.getCheckOutDate(), booking.getProperty().getId(), canceled);
    }

    private void checkBlockedProperty(Booking booking) {
        if (isBlocked(booking)) {
            throw new PropertyBookingBlockedException(booking);
        }
    }

    private static void hasValidBookingDates(Booking booking) {
        if (!booking.hasValidBookingDates()) {
            throw new BusinessException("Booking dates are invalid", ErrorCode.INVALID_BOOKING_DATES);
        }
    }
}
