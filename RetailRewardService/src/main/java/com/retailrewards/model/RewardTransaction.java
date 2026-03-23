package com.retailrewards.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RewardTransaction {

    private final BigDecimal amount;
    private final LocalDate transactionDate;
}
