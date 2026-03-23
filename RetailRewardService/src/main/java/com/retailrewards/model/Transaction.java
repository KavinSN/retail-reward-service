package com.retailrewards.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Transaction {

    private final String transactionId;
    private final String customerId;
    private final LocalDate transactionDate;
    private final BigDecimal amount;
    private final String description;
}
