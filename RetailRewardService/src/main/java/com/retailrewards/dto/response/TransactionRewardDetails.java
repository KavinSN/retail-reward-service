package com.retailrewards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransactionRewardDetails {

    private final String transactionId;
    private final String transactionDate;
    private final String description;
    private final String amount;
    private final double rewardPoints;
}
