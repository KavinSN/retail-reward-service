package com.retailrewards.exception;

import com.retailrewards.dto.response.ErrorResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Converts a missing-customer exception into a 404 response.
     *
     * @param exception missing customer exception
     * @param request current HTTP request
     * @return standardized error response
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException exception,
            HttpServletRequest request) {
        LOGGER.warn("Customer lookup failed: {}", exception.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, Collections.singletonList(exception.getMessage()), request);
    }

    /**
     * Converts request validation or business-request errors into a 400 response.
     *
     * @param exception invalid request exception
     * @param request current HTTP request
     * @return standardized error response
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException exception,
            HttpServletRequest request) {
        LOGGER.warn("Invalid request: {}", exception.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, Collections.singletonList(exception.getMessage()), request);
    }

    /**
     * Converts request parameter type mismatches into a 400 response.
     *
     * @param exception parameter type mismatch exception
     * @param request current HTTP request
     * @return standardized error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        String message = "Invalid value for parameter '" + exception.getName() + "'";
        LOGGER.warn("Type mismatch: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, Collections.singletonList(message), request);
    }

    /**
     * Converts bean validation failures on request bodies into a 400 response.
     *
     * @param exception method argument validation exception
     * @param request current HTTP request
     * @return standardized error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<String> messages = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error instanceof FieldError ? ((FieldError) error).getDefaultMessage()
                        : error.getDefaultMessage())
                .distinct()
                .collect(Collectors.toList());
        LOGGER.warn("Validation failed: {}", messages);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, messages, request);
    }

    /**
     * Converts parameter-level constraint violations into a 400 response.
     *
     * @param exception constraint violation exception
     * @param request current HTTP request
     * @return standardized error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception,
            HttpServletRequest request) {
        List<String> messages = exception.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .distinct()
                .collect(Collectors.toList());
        LOGGER.warn("Constraint validation failed: {}", messages);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, messages, request);
    }

    /**
     * Converts unreadable request payload errors into a 400 response.
     *
     * @param exception unreadable message exception
     * @param request current HTTP request
     * @return standardized error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        LOGGER.warn("Unreadable request body: {}", exception.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST,
                Collections.singletonList("Request body is required and must be valid JSON"), request);
    }

    /**
     * Converts any unhandled exception into a generic 500 response.
     *
     * @param exception unhandled exception
     * @param request current HTTP request
     * @return standardized error response
     */
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
