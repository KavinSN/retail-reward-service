package com.retailrewards.service;

import com.retailrewards.model.RewardTransaction;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RewardCalculatorTest {

    private RewardCalculator rewardCalculator;

    @BeforeEach
    void setUp() {
        rewardCalculator = new RewardCalculator();
    }

    @Test
    void shouldReturnZeroPointsForAmountsAtOrBelowFifty() {
        assertEquals(0L, rewardCalculator.calculatePoints(new BigDecimal("50.00")));
        assertEquals(0L, rewardCalculator.calculatePoints(new BigDecimal("35.10")));
    }

    @Test
    void shouldReturnSingleRatePointsBetweenFiftyOneAndOneHundred() {
        assertEquals(25L, rewardCalculator.calculatePoints(new BigDecimal("75.99")));
        assertEquals(50L, rewardCalculator.calculatePoints(new BigDecimal("100.00")));
    }

    @Test
    void shouldReturnTieredPointsAboveOneHundred() {
        assertEquals(90L, rewardCalculator.calculatePoints(new BigDecimal("120.00")));
        assertEquals(110L, rewardCalculator.calculatePoints(new BigDecimal("130.89")));
    }

    @Test
    void shouldCalculatePointsUsingWholeDollarSpendOnly() {
        assertEquals(0L, rewardCalculator.calculatePoints(new BigDecimal("50.99")));
        assertEquals(50L, rewardCalculator.calculatePoints(new BigDecimal("100.99")));
        assertEquals(90L, rewardCalculator.calculatePoints(new BigDecimal("120.99")));
    }

    @Test
    void shouldCalculateTotalPointsForTransactions() {
        long totalPoints = rewardCalculator.calculateTotalPoints(Arrays.asList(
                new RewardTransaction(new BigDecimal("120.00"), LocalDate.of(2026, 3, 10)),
                new RewardTransaction(new BigDecimal("75.00"), LocalDate.of(2026, 3, 11)),
                new RewardTransaction(new BigDecimal("40.00"), LocalDate.of(2026, 3, 12))));

        assertEquals(115L, totalPoints);
    }

    @Test
    void shouldCalculateMonthlyPointsInDescendingMonthOrder() {
        Map<String, Long> monthlyPoints = rewardCalculator.calculateMonthlyPoints(Arrays.asList(
                new RewardTransaction(new BigDecimal("75.00"), LocalDate.of(2026, 1, 11)),
                new RewardTransaction(new BigDecimal("140.00"), LocalDate.of(2026, 3, 12)),
                new RewardTransaction(new BigDecimal("99.00"), LocalDate.of(2026, 2, 10)),
                new RewardTransaction(new BigDecimal("120.00"), LocalDate.of(2026, 3, 13))));

        assertEquals(Arrays.asList("2026-Mar", "2026-Feb", "2026-Jan"), Arrays.asList(monthlyPoints.keySet().toArray()));
        assertEquals(Long.valueOf(220L), monthlyPoints.get("2026-Mar"));
        assertEquals(Long.valueOf(49L), monthlyPoints.get("2026-Feb"));
        assertEquals(Long.valueOf(25L), monthlyPoints.get("2026-Jan"));
    }
}
