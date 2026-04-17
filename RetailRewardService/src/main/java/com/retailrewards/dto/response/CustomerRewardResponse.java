package com.retailrewards.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerRewardResponse {

    private final String customerId;
    private final String customerName;
    private final String startDate;
    private final String endDate;
    private final List<MonthlyRewardPoints> monthlyPoints;
    private final double totalPoints;
    private final List<TransactionRewardDetails> transactions;
}
