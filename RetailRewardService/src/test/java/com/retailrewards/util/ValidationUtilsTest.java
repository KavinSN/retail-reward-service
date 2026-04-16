package com.retailrewards.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.retailrewards.exception.RewardException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ValidationUtilsTest {

    @Test
    void shouldAllowDefaultRequestWithoutDatesOrMonths() {
        assertDoesNotThrow(() -> ValidationUtils.validateRequest(null, null, null));
    }

    @Test
    void shouldRejectMissingCustomerId() {
        RewardException exception = assertThrows(RewardException.class,
                () -> ValidationUtils.validateCustomerId(null));

        assertEquals(ApplicationConstants.MESSAGE_CUSTOMER_ID_REQUIRED, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldRejectBlankCustomerId() {
        RewardException exception = assertThrows(RewardException.class,
                () -> ValidationUtils.validateCustomerId("   "));

        assertEquals(ApplicationConstants.MESSAGE_CUSTOMER_ID_REQUIRED, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldAllowValidCustomerId() {
        assertDoesNotThrow(() -> ValidationUtils.validateCustomerId("C1001"));
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
        RewardException exception = assertThrows(RewardException.class,
                () -> ValidationUtils.validateRequest(2, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31)));

        assertEquals(ApplicationConstants.MESSAGE_MONTHS_AND_DATE_RANGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldRejectRequestWhenMonthsAndOnlyEndDateAreProvidedTogether() {
        RewardException exception = assertThrows(RewardException.class,
                () -> ValidationUtils.validateRequest(2, null, LocalDate.of(2026, 3, 31)));

        assertEquals(ApplicationConstants.MESSAGE_MONTHS_AND_DATE_RANGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldRejectNonPositiveMonths() {
        RewardException exception = assertThrows(RewardException.class,
                () -> ValidationUtils.validateRequest(0, null, null));

        assertEquals(ApplicationConstants.MESSAGE_INVALID_MONTHS, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldRejectStartDateAfterEndDate() {
        RewardException exception = assertThrows(RewardException.class,
                () -> ValidationUtils.validateRequest(null, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 3, 31)));

        assertEquals(ApplicationConstants.MESSAGE_INVALID_DATE_RANGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldAllowOrderedDateRange() {
        assertDoesNotThrow(
                () -> ValidationUtils.validateRequest(null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)));
    }
}
