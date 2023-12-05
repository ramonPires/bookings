package com.bookings.exception;

import com.bookings.models.Booking;

public class PropertyBookingBlockedException extends BusinessException {

    public static final String ERROR_MESSAGE_PATTERN =
            "Property id=%s is blocked for bookings with checkIn='%s' and checkOut='%s' dates";

    public PropertyBookingBlockedException(Booking booking) {
        super(ERROR_MESSAGE_PATTERN.formatted(
                        booking.getPropertyId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate()),
                ErrorCode.PROPERTY_BOOKING_BLOCKED
        );
    }
}
