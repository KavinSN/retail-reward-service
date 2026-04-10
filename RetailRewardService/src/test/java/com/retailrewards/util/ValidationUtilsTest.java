package com.retailrewards.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.retailrewards.exception.InvalidRequestException;
import java.lang.reflect.Constructor;
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
    void shouldRejectBlankCustomerId() {
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> ValidationUtils.validateCustomerId("   "));

        assertEquals(ApplicationConstants.MESSAGE_CUSTOMER_ID_REQUIRED, exception.getMessage());
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
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> ValidationUtils.validateRequest(2, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31)));

        assertEquals(ApplicationConstants.MESSAGE_MONTHS_AND_DATE_RANGE, exception.getMessage());
    }

    @Test
    void shouldRejectRequestWhenMonthsAndOnlyEndDateAreProvidedTogether() {
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> ValidationUtils.validateRequest(2, null, LocalDate.of(2026, 3, 31)));

        assertEquals(ApplicationConstants.MESSAGE_MONTHS_AND_DATE_RANGE, exception.getMessage());
    }

    @Test
    void shouldRejectNonPositiveMonths() {
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> ValidationUtils.validateRequest(0, null, null));

        assertEquals(ApplicationConstants.MESSAGE_INVALID_MONTHS, exception.getMessage());
    }

    @Test
    void shouldRejectStartDateAfterEndDate() {
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> ValidationUtils.validateRequest(null, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 3, 31)));

        assertEquals(ApplicationConstants.MESSAGE_INVALID_DATE_RANGE, exception.getMessage());
    }

    @Test
    void shouldAllowOrderedDateRange() {
        assertDoesNotThrow(
                () -> ValidationUtils.validateRequest(null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)));
    }

    @Test
    void shouldInstantiateUtilityClassesForCoverage() throws Exception {
        Constructor<ValidationUtils> validationUtilsConstructor = ValidationUtils.class.getDeclaredConstructor();
        validationUtilsConstructor.setAccessible(true);
        validationUtilsConstructor.newInstance();

        Constructor<ApplicationConstants> applicationConstantsConstructor =
                ApplicationConstants.class.getDeclaredConstructor();
        applicationConstantsConstructor.setAccessible(true);
        applicationConstantsConstructor.newInstance();
    }
}
