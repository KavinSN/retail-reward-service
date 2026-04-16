package com.retailrewards.util;

import com.retailrewards.exception.RewardException;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    /**
     * Validates that the customer identifier is present and non-blank.
     *
     * @param customerId customer identifier to validate
     */
    public static void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new RewardException(ApplicationConstants.MESSAGE_CUSTOMER_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Validates mutually exclusive and ordered request filter inputs.
     *
     * @param months optional rolling month count
     * @param startDate optional range start date
     * @param endDate optional range end date
     */
    public static void validateRequest(Integer months, LocalDate startDate, LocalDate endDate) {
        if (months != null && months < 1) {
            throw new RewardException(ApplicationConstants.MESSAGE_INVALID_MONTHS, HttpStatus.BAD_REQUEST);
        }
        if (months != null && (startDate != null || endDate != null)) {
            throw new RewardException(ApplicationConstants.MESSAGE_MONTHS_AND_DATE_RANGE, HttpStatus.BAD_REQUEST);
        }
        if (startDate == null && endDate == null) {
            return;
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new RewardException(ApplicationConstants.MESSAGE_INVALID_DATE_RANGE, HttpStatus.BAD_REQUEST);
        }
    }
}
