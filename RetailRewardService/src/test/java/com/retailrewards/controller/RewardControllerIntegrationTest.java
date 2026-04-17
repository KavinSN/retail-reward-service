package com.retailrewards.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RewardControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/rewards/customers";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnCombinedRewardsAndTransactionsForSpecifiedCustomer() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C1001"))
                .andExpect(jsonPath("$.customerName").value("Kavin"))
                .andExpect(jsonPath("$.startDate").value("2026-01-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-31"))
                .andExpect(jsonPath("$.monthlyPoints", hasSize(3)))
                .andExpect(jsonPath("$.monthlyPoints[0].year").value(2026))
                .andExpect(jsonPath("$.monthlyPoints[0].month").value("March"))
                .andExpect(jsonPath("$.monthlyPoints[0].rewardPoints").value(271.25))
                .andExpect(jsonPath("$.monthlyPoints[1].year").value(2026))
                .andExpect(jsonPath("$.monthlyPoints[1].month").value("February"))
                .andExpect(jsonPath("$.monthlyPoints[1].rewardPoints").value(110.0))
                .andExpect(jsonPath("$.monthlyPoints[2].year").value(2026))
                .andExpect(jsonPath("$.monthlyPoints[2].month").value("January"))
                .andExpect(jsonPath("$.monthlyPoints[2].rewardPoints").value(115.0))
                .andExpect(jsonPath("$.totalPoints").value(496.25))
                .andExpect(jsonPath("$.transactions", hasSize(6)))
                .andExpect(jsonPath("$.transactions[0].transactionId").value("T10001"))
                .andExpect(jsonPath("$.transactions[5].rewardPoints").value(1.25));
    }

    @Test
    void shouldReturnRewardsForSpecifiedMonths() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C1001").param("months", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C1001"))
                .andExpect(jsonPath("$.startDate").value("2026-02-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-31"))
                .andExpect(jsonPath("$.monthlyPoints", hasSize(2)))
                .andExpect(jsonPath("$.monthlyPoints[0].month").value("March"))
                .andExpect(jsonPath("$.monthlyPoints[1].month").value("February"))
                .andExpect(jsonPath("$.totalPoints").value(381.25))
                .andExpect(jsonPath("$.transactions", hasSize(4)));
    }

    @Test
    void shouldReturnCustomerSummaryForDateRange() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C1002")
                        .param("startDate", "2026-02-01")
                        .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C1002"))
                .andExpect(jsonPath("$.startDate").value("2026-02-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-31"))
                .andExpect(jsonPath("$.totalPoints").value(675.5))
                .andExpect(jsonPath("$.transactions", hasSize(4)))
                .andExpect(jsonPath("$.transactions[3].rewardPoints").value(491.5));
    }

    @Test
    void shouldResolveDateRangeWhenOnlyStartDateIsProvided() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C1001").param("startDate", "2026-02-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C1001"))
                .andExpect(jsonPath("$.startDate").value("2026-02-01"))
                .andExpect(jsonPath("$.endDate").value("2026-04-30"))
                .andExpect(jsonPath("$.totalPoints").value(381.25))
                .andExpect(jsonPath("$.transactions", hasSize(4)));
    }

    @Test
    void shouldResolveDateRangeWhenOnlyEndDateIsProvided() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C1001").param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C1001"))
                .andExpect(jsonPath("$.startDate").value("2026-01-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-31"))
                .andExpect(jsonPath("$.totalPoints").value(496.25))
                .andExpect(jsonPath("$.transactions", hasSize(6)));
    }

    @Test
    void shouldReturnNotFoundForUnknownCustomer() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]").value("Customer not found: C9999"));
    }

    @Test
    void shouldRejectInvalidMonthCount() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C1001").param("months", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("must be greater than or equal to 1"));
    }

    @Test
    void shouldRejectInvalidDateRange() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C1001")
                        .param("startDate", "2026-03-31")
                        .param("endDate", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("Provide a valid date range with startDate on or before endDate"));
    }

    @Test
    void shouldRejectWhenMonthsAndDateRangeAreProvidedTogether() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C1001")
                        .param("months", "2")
                        .param("startDate", "2026-02-01")
                        .param("endDate", "2026-03-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("Provide either months or a startDate/endDate range, not both"));
    }

    @Test
    void shouldRejectInvalidDateFormat() throws Exception {
        mockMvc.perform(get(BASE_URL + "/C1001")
                        .param("startDate", "01-02-2026")
                        .param("endDate", "31-03-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Invalid value for parameter 'startDate'"));
    }
}
