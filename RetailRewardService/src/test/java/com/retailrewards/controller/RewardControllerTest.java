package com.retailrewards.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnCombinedRewardsAndTransactionsForSpecifiedCustomer() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C1001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C1001"))
                .andExpect(jsonPath("$.customerName").value("Kavin"))
                .andExpect(jsonPath("$.startDate").value("2026-01-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-31"))
                .andExpect(jsonPath("$.monthlyPoints.2026-Mar").value(271))
                .andExpect(jsonPath("$.monthlyPoints.2026-Feb").value(110))
                .andExpect(jsonPath("$.monthlyPoints.2026-Jan").value(115))
                .andExpect(jsonPath("$.totalPoints").value(496))
                .andExpect(jsonPath("$.transactions", hasSize(6)))
                .andExpect(jsonPath("$.transactions[0].transactionId").value("T10001"));
    }

    @Test
    void shouldRejectRewardRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRewardRequestWhenCustomerIdIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("customerId is required"));
    }

    @Test
    void shouldReturnRewardsForSpecifiedMonths() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C1001\",\"months\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C1001"))
                .andExpect(jsonPath("$.startDate").value("2026-02-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-31"))
                .andExpect(jsonPath("$.monthlyPoints.2026-Jan").doesNotExist())
                .andExpect(jsonPath("$.totalPoints").value(381))
                .andExpect(jsonPath("$.transactions", hasSize(4)));
    }

    @Test
    void shouldReturnCustomerSummaryForDateRange() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C1002\",\"startDate\":\"2026-02-01\",\"endDate\":\"2026-03-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C1002"))
                .andExpect(jsonPath("$.startDate").value("2026-02-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-31"))
                .andExpect(jsonPath("$.totalPoints").value(674))
                .andExpect(jsonPath("$.transactions", hasSize(4)));
    }

    @Test
    void shouldReturnNotFoundForUnknownCustomer() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C9999\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]").value("Customer not found: C9999"));
    }

    @Test
    void shouldRejectInvalidMonthCount() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C1001\",\"months\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("months must be greater than 0"));
    }

    @Test
    void shouldRejectInvalidDateRange() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C1001\",\"startDate\":\"2026-03-31\",\"endDate\":\"2026-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("Provide a valid date range with startDate on or before endDate"));
    }

    @Test
    void shouldPreferMonthsWhenMonthsAndDateRangeAreProvidedTogether() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C1001\",\"months\":2,\"startDate\":\"2026-02-01\",\"endDate\":\"2026-03-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C1001"))
                .andExpect(jsonPath("$.startDate").value("2026-02-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-31"))
                .andExpect(jsonPath("$.transactions", hasSize(4)));
    }
}
