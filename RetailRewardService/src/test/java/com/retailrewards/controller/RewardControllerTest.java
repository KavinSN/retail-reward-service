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
    void shouldReturnDefaultThreeMonthRewardsForAllCustomers() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].customerId").value("C1001"))
                .andExpect(jsonPath("$[0].customerName").value("Kavin"))
                .andExpect(jsonPath("$[0].monthlyPoints.MARCH").value(271))
                .andExpect(jsonPath("$[0].monthlyPoints.FEBRUARY").value(110))
                .andExpect(jsonPath("$[0].monthlyPoints.JANUARY").value(115))
                .andExpect(jsonPath("$[0].totalPoints").value(496));
    }

    @Test
    void shouldReturnRewardsWhenRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void shouldReturnRewardsForSpecifiedMonths() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"months\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].customerId").value("C1001"))
                .andExpect(jsonPath("$[0].monthlyPoints.JANUARY").doesNotExist())
                .andExpect(jsonPath("$[0].totalPoints").value(381));
    }

    @Test
    void shouldReturnCustomerSummaryWhenCustomerIdIsProvidedInBody() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C1002\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value("C1002"))
                .andExpect(jsonPath("$[0].customerName").value("Prabhu"))
                .andExpect(jsonPath("$[0].totalPoints").value(726));
    }

    @Test
    void shouldReturnMultipleCustomersWhenCommaSeparatedIdsAreProvided() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C1001,C1003\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerId").value("C1001"))
                .andExpect(jsonPath("$[1].customerId").value("C1003"));
    }

    @Test
    void shouldReturnCustomerSummaryForDateRange() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C1002\",\"startDate\":\"2026-02-01\",\"endDate\":\"2026-03-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value("C1002"))
                .andExpect(jsonPath("$[0].totalPoints").value(674));
    }

    @Test
    void shouldReturnCustomerTransactionsFromSeparateEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C1002\",\"startDate\":\"2026-02-01\",\"endDate\":\"2026-03-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value("C1002"))
                .andExpect(jsonPath("$[0].customerName").value("Prabhu"))
                .andExpect(jsonPath("$[0].totalPoints").value(674))
                .andExpect(jsonPath("$[0].transactions", hasSize(4)))
                .andExpect(jsonPath("$[0].transactions[0].transactionId").value("T20003"));
    }

    @Test
    void shouldReturnTransactionsWhenRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void shouldReturnMultipleCustomerTransactionsWhenCommaSeparatedIdsAreProvided() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"C1001,C1002\",\"months\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerId").value("C1001"))
                .andExpect(jsonPath("$[0].totalPoints").value(271))
                .andExpect(jsonPath("$[1].customerId").value("C1002"));
    }

    @Test
    void shouldUseMonthsWhenMonthsAndDateRangeAreProvided() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"months\":3,\"startDate\":\"2026-01-01\",\"endDate\":\"2026-03-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].customerId").value("C1001"))
                .andExpect(jsonPath("$[0].totalPoints").value(496));
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
                        .content("{\"months\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("months must be greater than 0"));
    }

    @Test
    void shouldRejectInvalidDateRange() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"2026-03-31\",\"endDate\":\"2026-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("Provide a valid date range with startDate on or before endDate"));
    }
}
