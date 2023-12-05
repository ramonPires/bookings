package com.bookings.exception;

import com.bookings.models.Block;

public class PropertyAlreadyBlockedException extends BusinessException {

    public static final String ERROR_MESSAGE_PATTERN =
            "Property id=%s is already blocked for startDate='%s' and endDate='%s' dates";

    public PropertyAlreadyBlockedException(Block block) {
        super(ERROR_MESSAGE_PATTERN.formatted(
                        block.getPropertyId(),
                        block.getStartDate(),
                        block.getEndDate()),
                ErrorCode.PROPERTY_ALREADY_BLOCKED
        );
    }
}
