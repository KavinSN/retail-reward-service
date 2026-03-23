package com.retailrewards.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.retailrewards.exception.InvalidRequestException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    void shouldRejectRequestWhenOnlyOneDateIsProvided() {
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> ValidationUtils.validateRequest(null, LocalDate.of(2026, 3, 1), null));

        assertEquals(ApplicationConstants.MESSAGE_INVALID_DATE_RANGE, exception.getMessage());
    }

    @Test
    void shouldCreateUtilityClassesViaReflectionOnly() throws Exception {
        Constructor<ValidationUtils> validationUtilsConstructor = ValidationUtils.class.getDeclaredConstructor();
        validationUtilsConstructor.setAccessible(true);
        validationUtilsConstructor.newInstance();

        Constructor<ApplicationConstants> applicationConstantsConstructor =
                ApplicationConstants.class.getDeclaredConstructor();
        applicationConstantsConstructor.setAccessible(true);
        applicationConstantsConstructor.newInstance();
    }
}
