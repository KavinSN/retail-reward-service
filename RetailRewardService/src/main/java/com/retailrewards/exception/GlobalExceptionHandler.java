package com.retailrewards.exception;

import com.retailrewards.dto.response.ErrorResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException exception,
            HttpServletRequest request) {
        LOGGER.warn("Customer lookup failed: {}", exception.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, Collections.singletonList(exception.getMessage()), request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException exception,
            HttpServletRequest request) {
        LOGGER.warn("Invalid request: {}", exception.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, Collections.singletonList(exception.getMessage()), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        String message = "Invalid value for parameter '" + exception.getName() + "'";
        LOGGER.warn("Type mismatch: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, Collections.singletonList(message), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unhandled exception", exception);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                Collections.singletonList("An unexpected error occurred"), request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, List<String> messages,
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                messages, request.getRequestURI());
        return ResponseEntity.status(status).body(errorResponse);
    }
}
