package com.bookings.exception;

import com.bookings.models.Booking;

public class PropertyUnavailableException extends BusinessException {
    public static final String ERROR_MESSAGE_PATTERN =
            "Property currently not available to be booked with checkIn='%s' and checkOut='%s' dates";

    public PropertyUnavailableException(Booking booking) {
        super(ERROR_MESSAGE_PATTERN.formatted(
                booking.getCheckInDate(),
                booking.getCheckOutDate()), ErrorCode.PROPERTY_UNAVAILABLE);
    }
}
