package com.bookings.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class BlockTest {
    @ParameterizedTest
    @CsvSource({
            "2024-02-01, 2024-02-01, false",
            "2024-02-08, 2024-02-07, false",
            "2025-02-08, 2024-02-07, false",
            "2024-02-01, 2024-02-05, true",
            "2024-03-01, 2024-04-05, true",
            "2024-03-01, 2024-03-23, true",
    })
    @DisplayName("Should check if booking dates are valid")
    void shouldCheckIfBookingDatesAreValid(ArgumentsAccessor arguments) {
        var startDate = arguments.get(0, LocalDate.class);
        var endDate = arguments.get(1, LocalDate.class);
        var valid = arguments.get(2, Boolean.class);
        var block = new Block(startDate, endDate, new Property(1L));
        assertThat(block.hasValidBlockDates(), is(valid));
    }
}