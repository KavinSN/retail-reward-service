package com.retailrewards.service;

import com.retailrewards.model.RewardTransaction;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RewardCalculator {

    public long calculatePoints(BigDecimal transactionAmount) {
        long amount = transactionAmount.longValue();
        if (amount <= 50L) {
            return 0L;
        }
        if (amount <= 100L) {
            return amount - 50L;
        }
        return 50L + ((amount - 100L) * 2L);
    }

    public long calculateTotalPoints(List<RewardTransaction> transactions) {
        return transactions.stream()
                .mapToLong(transaction -> calculatePoints(transaction.getAmount()))
                .sum();
    }

    public Map<String, Long> calculateMonthlyPoints(List<RewardTransaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.getTransactionDate()),
                        Collectors.summingLong(transaction -> calculatePoints(transaction.getAmount()))))
                .entrySet().stream()
                .sorted((left, right) -> right.getKey().compareTo(left.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey().getMonth().name(), Map.Entry::getValue,
                        (left, right) -> left, LinkedHashMap::new));
    }
}
