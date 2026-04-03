package com.retailrewards.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.retailrewards.dto.response.ErrorResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/rewards/customers/C1001");
    }

    @Test
    void shouldHandleCustomerNotFoundException() {
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCustomerNotFound(
                new CustomerNotFoundException("C9999"), request);

        assertErrorResponse(response, HttpStatus.NOT_FOUND, "Not Found",
                "Customer not found: C9999");
    }

    @Test
    void shouldHandleInvalidRequestException() {
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidRequest(
                new InvalidRequestException("Invalid request data"), request);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Bad Request",
                "Invalid request data");
    }

    @Test
    void shouldHandleTypeMismatchException() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "bad-value", Integer.class, "months", (MethodParameter) null,
                new TypeMismatchException("bad-value", Integer.class));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatch(exception, request);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Bad Request",
                "Invalid value for parameter 'months'");
    }

    @Test
    void shouldHandleMethodArgumentNotValidAndDeduplicateMessages() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "months", null, false, null, null,
                "must be greater than or equal to 1"));
        bindingResult.addError(new ObjectError("request", "request body is invalid"));
        bindingResult.addError(new FieldError("request", "months", null, false, null, null,
                "must be greater than or equal to 1"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValid(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getMessages().size());
        assertEquals(Arrays.asList("must be greater than or equal to 1", "request body is invalid"),
                response.getBody().getMessages());
        assertCommonBody(response.getBody(), HttpStatus.BAD_REQUEST, "Bad Request");
    }

    @Test
    void shouldHandleConstraintViolationAndDeduplicateMessages() {
        ConstraintViolation<?> monthsViolation = mock(ConstraintViolation.class);
        ConstraintViolation<?> startDateViolation = mock(ConstraintViolation.class);
        ConstraintViolation<?> duplicateViolation = mock(ConstraintViolation.class);
        when(monthsViolation.getMessage()).thenReturn("months must be greater than 0");
        when(startDateViolation.getMessage()).thenReturn("startDate must be before endDate");
        when(duplicateViolation.getMessage()).thenReturn("months must be greater than 0");
        Set<ConstraintViolation<?>> violations = new LinkedHashSet<>(
                Arrays.asList(monthsViolation, startDateViolation, duplicateViolation));

        ConstraintViolationException exception = new ConstraintViolationException("validation failed", violations);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(new HashSet<>(Arrays.asList("months must be greater than 0", "startDate must be before endDate")),
                new HashSet<>(response.getBody().getMessages()));
        assertEquals(2, response.getBody().getMessages().size());
        assertCommonBody(response.getBody(), HttpStatus.BAD_REQUEST, "Bad Request");
    }

    @Test
    void shouldHandleUnreadableMessage() {
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnreadableMessage(
                new HttpMessageNotReadableException("Malformed JSON"), request);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Bad Request",
                "Request body is required and must be valid JSON");
    }

    @Test
    void shouldHandleUnexpectedExceptions() {
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(
                new RuntimeException("boom"), request);

        assertErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred");
    }

    private void assertErrorResponse(ResponseEntity<ErrorResponse> response, HttpStatus status, String error,
            String message) {
        assertEquals(status, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getMessages().size());
        assertEquals(message, response.getBody().getMessages().get(0));
        assertCommonBody(response.getBody(), status, error);
    }

    private void assertCommonBody(ErrorResponse body, HttpStatus status, String error) {
        assertNotNull(body.getTimestamp());
        assertEquals(status.value(), body.getStatus());
        assertEquals(error, body.getError());
        assertEquals("/api/v1/rewards/customers/C1001", body.getPath());
        assertTrue(body.getTimestamp().getYear() >= 2026);
    }
}
