package com.retailrewards.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.retailrewards.dto.response.CustomerRewardResponse;
import com.retailrewards.dto.response.MonthlyRewardPoints;
import com.retailrewards.dto.response.TransactionRewardDetails;
import com.retailrewards.exception.CustomerNotFoundException;
import com.retailrewards.exception.GlobalExceptionHandler;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RewardControllerUnitTest {

    private static final String BASE_URL = "/api/v1/rewards/customers";

    @Mock
    private RewardService rewardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RewardController(rewardService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnCustomerRewardsForValidMonthsRequest() throws Exception {
        given(rewardService.getCustomerRewards(eq("C1001"), eq(2), eq(null), eq(null)))
                .willReturn(buildResponse());

        mockMvc.perform(get(BASE_URL + "/C1001").param("months", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C1001"))
                .andExpect(jsonPath("$.customerName").value("Kavin"))
                .andExpect(jsonPath("$.startDate").value("2026-02-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-31"))
                .andExpect(jsonPath("$.monthlyPoints[0].year").value(2026))
                .andExpect(jsonPath("$.monthlyPoints[0].month").value("March"))
                .andExpect(jsonPath("$.monthlyPoints[0].rewardPoints").value(271))
                .andExpect(jsonPath("$.transactions[0].transactionId").value("T10004"));

        verify(rewardService).getCustomerRewards("C1001", 2, null, null);
    }

    @Test
    void shouldPassDateRangeParametersToService() throws Exception {
        given(rewardService.getCustomerRewards(eq("C1002"), eq(null), eq(LocalDate.of(2026, 2, 1)),
                eq(LocalDate.of(2026, 3, 31)))).willReturn(buildResponse());

        mockMvc.perform(get(BASE_URL + "/C1002")
                        .param("startDate", "2026-02-01")
                        .param("endDate", "2026-03-31"))
                .andExpect(status().isOk());

        verify(rewardService).getCustomerRewards("C1002", null, LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 3, 31));
    }

    @Test
    void shouldPassSingleStartDateToService() throws Exception {
        given(rewardService.getCustomerRewards(eq("C1001"), eq(null), eq(LocalDate.of(2026, 2, 1)), eq(null)))
                .willReturn(buildResponse());

        mockMvc.perform(get(BASE_URL + "/C1001").param("startDate", "2026-02-01"))
                .andExpect(status().isOk());

        verify(rewardService).getCustomerRewards("C1001", null, LocalDate.of(2026, 2, 1), null);
    }

    @Test
    void shouldPassSingleEndDateToService() throws Exception {
        given(rewardService.getCustomerRewards(eq("C1001"), eq(null), eq(null), eq(LocalDate.of(2026, 3, 31))))
                .willReturn(buildResponse());

        mockMvc.perform(get(BASE_URL + "/C1001").param("endDate", "2026-03-31"))
                .andExpect(status().isOk());

        verify(rewardService).getCustomerRewards("C1001", null, null, LocalDate.of(2026, 3, 31));
    }

    @Test
    void shouldReturnBadRequestWhenMonthValueIsNotNumeric() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C1001").param("months", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Invalid value for parameter 'months'"));

        verifyNoInteractions(rewardService);
    }

    @Test
    void shouldReturnBadRequestWhenDateFormatIsInvalidBeforeServiceExecution() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C1001").param("startDate", "01-02-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Invalid value for parameter 'startDate'"));

        verifyNoInteractions(rewardService);
    }

    @Test
    void shouldTranslateCustomerNotFoundException() throws Exception {
        given(rewardService.getCustomerRewards(eq("C9999"), eq(null), eq(null), eq(null)))
                .willThrow(new CustomerNotFoundException("C9999"));

        mockMvc.perform(get(BASE_URL + "/C9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/C9999"))
                .andExpect(jsonPath("$.messages[0]").value("Customer not found: C9999"));
    }

    @Test
    void shouldTranslateInvalidRequestException() throws Exception {
        given(rewardService.getCustomerRewards(eq("C1001"), eq(null), eq(null), eq(null)))
                .willThrow(new InvalidRequestException("No transaction data available"));

        mockMvc.perform(get(BASE_URL + "/C1001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/C1001"))
                .andExpect(jsonPath("$.messages[0]").value("No transaction data available"));
    }

    @Test
    void shouldTranslateUnexpectedException() throws Exception {
        given(rewardService.getCustomerRewards(eq("C1001"), eq(null), eq(null), eq(null)))
                .willThrow(new RuntimeException("boom"));

        mockMvc.perform(get(BASE_URL + "/C1001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/C1001"))
                .andExpect(jsonPath("$.messages[0]").value("An unexpected error occurred"));
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
