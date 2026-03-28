package com.retailrewards.exception;

public class InvalidRequestException extends RuntimeException {

    /**
     * Creates an exception for invalid request input or unsupported request state.
     *
     * @param message validation or request error message
     */
    public InvalidRequestException(String message) {
        super(message);
    }
}
