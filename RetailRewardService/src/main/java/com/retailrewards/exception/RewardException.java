package com.retailrewards.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RewardException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Creates an exception for reward-service business errors.
     *
     * @param message business error message
     * @param status HTTP status to return for the error
     */
    public RewardException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
