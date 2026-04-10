package com.retailrewards.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.retailrewards.dto.response.CustomerRewardResponse;
import com.retailrewards.dto.response.MonthlyRewardPoints;
import com.retailrewards.dto.response.TransactionRewardDetails;
import com.retailrewards.exception.CustomerNotFoundException;
import com.retailrewards.exception.InvalidRequestException;
import com.retailrewards.service.RewardService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class RewardControllerTest {

    @Mock
    private RewardService rewardService;

    private RewardController rewardController;

    @BeforeEach
    void setUp() {
        rewardController = new RewardController(rewardService);
    }

    @Test
    void shouldReturnCustomerRewardsForValidMonthsRequest() {
        CustomerRewardResponse expectedResponse = buildResponse();
        given(rewardService.getCustomerRewards(eq("C1001"), eq(2), eq(null), eq(null)))
                .willReturn(expectedResponse);

        ResponseEntity<CustomerRewardResponse> response = rewardController.getRewards("C1001", 2, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(rewardService).getCustomerRewards("C1001", 2, null, null);
    }

    @Test
    void shouldPassDateRangeParametersToService() {
        CustomerRewardResponse expectedResponse = buildResponse();
        LocalDate startDate = LocalDate.of(2026, 2, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 31);
        given(rewardService.getCustomerRewards(eq("C1002"), eq(null), eq(startDate), eq(endDate)))
                .willReturn(expectedResponse);

        ResponseEntity<CustomerRewardResponse> response =
                rewardController.getRewards("C1002", null, startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(rewardService).getCustomerRewards("C1002", null, startDate, endDate);
    }

    @Test
    void shouldPassSingleStartDateToService() {
        CustomerRewardResponse expectedResponse = buildResponse();
        LocalDate startDate = LocalDate.of(2026, 2, 1);
        given(rewardService.getCustomerRewards(eq("C1001"), eq(null), eq(startDate), eq(null)))
                .willReturn(expectedResponse);

        ResponseEntity<CustomerRewardResponse> response =
                rewardController.getRewards("C1001", null, startDate, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(rewardService).getCustomerRewards("C1001", null, startDate, null);
    }

    @Test
    void shouldPassSingleEndDateToService() {
        CustomerRewardResponse expectedResponse = buildResponse();
        LocalDate endDate = LocalDate.of(2026, 3, 31);
        given(rewardService.getCustomerRewards(eq("C1001"), eq(null), eq(null), eq(endDate)))
                .willReturn(expectedResponse);

        ResponseEntity<CustomerRewardResponse> response =
                rewardController.getRewards("C1001", null, null, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(rewardService).getCustomerRewards("C1001", null, null, endDate);
    }

    @Test
    void shouldPropagateCustomerNotFoundException() {
        given(rewardService.getCustomerRewards(eq("C9999"), eq(null), eq(null), eq(null)))
                .willThrow(new CustomerNotFoundException("C9999"));

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,
                () -> rewardController.getRewards("C9999", null, null, null));

        assertEquals("Customer not found: C9999", exception.getMessage());
        verify(rewardService).getCustomerRewards("C9999", null, null, null);
    }

    @Test
    void shouldPropagateInvalidRequestException() {
        given(rewardService.getCustomerRewards(eq("C1001"), eq(null), eq(null), eq(null)))
                .willThrow(new InvalidRequestException("No transaction data available"));

        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> rewardController.getRewards("C1001", null, null, null));

        assertEquals("No transaction data available", exception.getMessage());
        verify(rewardService).getCustomerRewards("C1001", null, null, null);
    }

    @Test
    void shouldPropagateUnexpectedException() {
        given(rewardService.getCustomerRewards(eq("C1001"), eq(null), eq(null), eq(null)))
                .willThrow(new RuntimeException("boom"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> rewardController.getRewards("C1001", null, null, null));

        assertEquals("boom", exception.getMessage());
        verify(rewardService).getCustomerRewards("C1001", null, null, null);
    }

    private CustomerRewardResponse buildResponse() {
        return new CustomerRewardResponse(
                "C1001",
                "Kavin",
                "2026-02-01",
                "2026-03-31",
                Arrays.asList(
                        new MonthlyRewardPoints(2026, "March", 271),
                        new MonthlyRewardPoints(2026, "February", 110)),
                381L,
                Collections.singletonList(
                        new TransactionRewardDetails("T10004", "2026-02-16", "Electronics accessories", "130.00",
                                110L)));
    }
}
