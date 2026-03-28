package com.retailrewards.repository;

import com.retailrewards.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class TransactionRepository {

    private final ExecutorService rewardsTaskExecutor = Executors.newFixedThreadPool(2);

    /**
     * Retrieves transactions for a customer within the provided inclusive date range.
     *
     * @param customerId customer identifier to filter by
     * @param startDate inclusive range start date
     * @param endDate inclusive range end date
     * @return future containing matching transactions
     */
    public CompletableFuture<List<Transaction>> findTransactionsByCustomerIdAndDateRangeAsync(String customerId,
            LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay();
            log.info("Transactions fetched for customer {} between {} and {}", customerId, startDate, endDate);
            return buildTransactions().stream()
                    .filter(transaction -> transaction.getCustomerId().equalsIgnoreCase(customerId))
                    .filter(transaction -> !transaction.getTransactionDate().isBefore(startDate))
                    .filter(transaction -> !transaction.getTransactionDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }, rewardsTaskExecutor);
    }

    /**
     * Retrieves the latest transaction date available for the requested customer.
     *
     * @param customerId customer identifier to filter by
     * @return future containing the latest transaction date when available
     */
    public CompletableFuture<Optional<LocalDate>> findLatestTransactionDateByCustomerIdAsync(String customerId) {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay();
            return buildTransactions().stream()
                    .filter(transaction -> transaction.getCustomerId().equalsIgnoreCase(customerId))
                    .map(Transaction::getTransactionDate)
                    .max(LocalDate::compareTo);
        }, rewardsTaskExecutor);
    }

    @PreDestroy
    void shutdownExecutor() {
        rewardsTaskExecutor.shutdown();
    }

    private void simulateDelay() {
        try {
            Thread.sleep(150L);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Transaction retrieval was interrupted", interruptedException);
        }
    }

    private List<Transaction> buildTransactions() {
        return Arrays.asList(
                new Transaction("T10001", "C1001", LocalDate.of(2026, 1, 5), new BigDecimal("120.00"),
                        "Grocery order"),
                new Transaction("T10002", "C1001", LocalDate.of(2026, 1, 21), new BigDecimal("75.00"),
                        "Home supplies"),
                new Transaction("T10003", "C1001", LocalDate.of(2026, 2, 4), new BigDecimal("45.00"),
                        "Pharmacy purchase"),
                new Transaction("T10004", "C1001", LocalDate.of(2026, 2, 16), new BigDecimal("130.00"),
                        "Electronics accessories"),
                new Transaction("T10005", "C1001", LocalDate.of(2026, 3, 12), new BigDecimal("210.00"),
                        "Appliance order"),
                new Transaction("T10006", "C1001", LocalDate.of(2026, 3, 22), new BigDecimal("51.25"),
                        "Pet supplies"),
                new Transaction("T20001", "C1002", LocalDate.of(2026, 1, 8), new BigDecimal("49.99"),
                        "Books"),
                new Transaction("T20002", "C1002", LocalDate.of(2026, 1, 28), new BigDecimal("101.00"),
                        "Weekly shopping"),
                new Transaction("T20003", "C1002", LocalDate.of(2026, 2, 10), new BigDecimal("99.00"),
                        "Department store"),
                new Transaction("T20004", "C1002", LocalDate.of(2026, 2, 23), new BigDecimal("140.00"),
                        "Furniture deposit"),
                new Transaction("T20005", "C1002", LocalDate.of(2026, 3, 2), new BigDecimal("55.00"),
                        "Garden supplies"),
                new Transaction("T20006", "C1002", LocalDate.of(2026, 3, 19), new BigDecimal("320.75"),
                        "Renovation materials"),
                new Transaction("T30001", "C1003", LocalDate.of(2025, 12, 30), new BigDecimal("88.00"),
                        "Year-end shopping"),
                new Transaction("T30002", "C1003", LocalDate.of(2026, 1, 14), new BigDecimal("150.00"),
                        "Winter apparel"),
                new Transaction("T30003", "C1003", LocalDate.of(2026, 2, 8), new BigDecimal("20.00"),
                        "Coffee shop"),
                new Transaction("T30004", "C1003", LocalDate.of(2026, 2, 28), new BigDecimal("60.00"),
                        "Office supplies"),
                new Transaction("T30005", "C1003", LocalDate.of(2026, 3, 6), new BigDecimal("110.00"),
                        "Grocery refill"),
                new Transaction("T30006", "C1003", LocalDate.of(2026, 3, 18), new BigDecimal("500.00"),
                        "Television purchase"));
    }
}
