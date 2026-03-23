package com.retailrewards.util;

import com.retailrewards.exception.InvalidRequestException;
import java.time.LocalDate;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new InvalidRequestException(ApplicationConstants.MESSAGE_CUSTOMER_ID_REQUIRED);
        }
    }

    public static void validateRequest(Integer months, LocalDate startDate, LocalDate endDate) {
        if (months != null && months < 1) {
            throw new InvalidRequestException(ApplicationConstants.MESSAGE_INVALID_MONTHS);
        }
//        if (months != null && (startDate != null || endDate != null)) {
//            throw new InvalidRequestException(ApplicationConstants.MESSAGE_MONTHS_AND_DATE_RANGE);
//        }
        if (startDate == null && endDate == null) {
            return;
        }
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new InvalidRequestException(ApplicationConstants.MESSAGE_INVALID_DATE_RANGE);
        }
    }
}
