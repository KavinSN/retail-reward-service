package com.retailrewards.exception;

import com.retailrewards.util.ApplicationConstants;

public class CustomerNotFoundException extends RuntimeException {

    /**
     * Creates an exception for a missing customer lookup.
     *
     * @param customerId identifier that could not be found
     */
    public CustomerNotFoundException(String customerId) {
        super(ApplicationConstants.MESSAGE_CUSTOMER_NOT_FOUND_PREFIX + customerId);
    }
}
