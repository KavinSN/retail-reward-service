package com.retailrewards.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.retailrewards.model.Transaction;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TransactionRepositoryTest {

    private final TransactionRepository transactionRepository = new TransactionRepository();

    @Test
    void shouldReturnAllTransactionsFromInMemoryDataset() {
        List<Transaction> transactions = transactionRepository.findAllTransactionsAsync().join();

        assertEquals(18, transactions.size());
        assertEquals("T10001", transactions.get(0).getTransactionId());
        assertEquals("T30006", transactions.get(transactions.size() - 1).getTransactionId());
    }

    @Test
    void shouldReturnTransactionsForCustomerWithinDateRange() {
        List<Transaction> transactions = transactionRepository.findTransactionsByCustomerIdAndDateRangeAsync("C1002",
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31)).join();

        assertEquals(4, transactions.size());
        assertTrue(transactions.stream().allMatch(transaction -> "C1002".equals(transaction.getCustomerId())));
        assertEquals("T20003", transactions.get(0).getTransactionId());
        assertEquals("T20006", transactions.get(transactions.size() - 1).getTransactionId());
    }

    @Test
    void shouldReturnLatestTransactionDateForCustomer() {
        Optional<LocalDate> latestTransactionDate = transactionRepository
                .findLatestTransactionDateByCustomerIdAsync("C1001").join();

        assertTrue(latestTransactionDate.isPresent());
        assertEquals(LocalDate.of(2026, 3, 22), latestTransactionDate.get());
    }

    @Test
    void shouldShutdownExecutorWithoutErrors() {
        transactionRepository.shutdownExecutor();

        assertTrue(true);
    }
}
