package com.retailrewards.calculator;

import com.retailrewards.dto.response.MonthlyRewardPoints;
import com.retailrewards.model.RewardTransaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.TextStyle;
import java.time.YearMonth;
import java.util.Locale;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RewardCalculator {

    /**
     * Calculates reward points for a single transaction amount using the configured tiered rules.
     *
     * @param transactionAmount transaction amount used for point calculation
     * @return reward points earned for the transaction
     */
    public long calculatePoints(BigDecimal transactionAmount) {
        long amount = transactionAmount.setScale(0, RoundingMode.DOWN).longValueExact();
        if (amount <= 50L) {
            return 0L;
        }
        if (amount <= 100L) {
            return amount - 50L;
        }
        return 50L + ((amount - 100L) * 2L);
    }

    /**
     * Calculates total reward points across a list of transactions.
     *
     * @param transactions transactions to evaluate
     * @return total reward points for the provided transactions
     */
    public long calculateTotalPoints(List<RewardTransaction> transactions) {
        return transactions.stream()
                .mapToLong(transaction -> calculatePoints(transaction.getAmount()))
                .sum();
    }

    /**
     * Aggregates reward points by year and month in descending chronological order.
     *
     * @param transactions transactions to group by month
     * @return monthly reward summaries ordered from latest month to earliest
     */
    public List<MonthlyRewardPoints> calculateMonthlyPoints(List<RewardTransaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.getTransactionDate()),
                        Collectors.summingLong(transaction -> calculatePoints(transaction.getAmount()))))
                .entrySet().stream()
                .sorted((left, right) -> right.getKey().compareTo(left.getKey()))
                .map(entry -> toMonthlyRewardPoints(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private MonthlyRewardPoints toMonthlyRewardPoints(YearMonth yearMonth, Long rewardPoints) {
        return new MonthlyRewardPoints(yearMonth.getYear(),
                yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH), rewardPoints);
    }
}
