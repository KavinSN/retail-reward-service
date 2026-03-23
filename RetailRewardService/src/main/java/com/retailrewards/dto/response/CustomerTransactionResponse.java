package com.retailrewards.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerTransactionResponse {

    private final String customerId;
    private final String customerName;
    private final long totalPoints;
    private final List<TransactionRewardDetails> transactions;
}
