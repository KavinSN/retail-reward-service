package com.retailrewards.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.retailrewards.exception.InvalidRequestException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ValidationUtilsTest {

    @Test
    void shouldAllowDefaultRequestWithoutDatesOrMonths() {
        assertDoesNotThrow(() -> ValidationUtils.validateRequest(null, null, null));
    }

    @Test
    void shouldRejectMissingCustomerId() {
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> ValidationUtils.validateCustomerId(null));

        assertEquals(ApplicationConstants.MESSAGE_CUSTOMER_ID_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldAllowRequestWhenOnlyStartDateIsProvided() {
        assertDoesNotThrow(() -> ValidationUtils.validateRequest(null, LocalDate.of(2026, 3, 1), null));
    }

    @Test
    void shouldAllowRequestWhenOnlyEndDateIsProvided() {
        assertDoesNotThrow(() -> ValidationUtils.validateRequest(null, null, LocalDate.of(2026, 3, 31)));
    }

    @Test
    void shouldRejectRequestWhenMonthsAndDateRangeAreProvidedTogether() {
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> ValidationUtils.validateRequest(2, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31)));

        assertEquals(ApplicationConstants.MESSAGE_MONTHS_AND_DATE_RANGE, exception.getMessage());
    }
}
