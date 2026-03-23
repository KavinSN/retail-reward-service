package com.retailrewards.exception;

import com.retailrewards.util.ApplicationConstants;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerId) {
        super(ApplicationConstants.MESSAGE_CUSTOMER_NOT_FOUND_PREFIX + customerId);
    }
}
