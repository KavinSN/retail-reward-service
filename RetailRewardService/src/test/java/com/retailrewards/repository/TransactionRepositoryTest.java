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
    void shouldReturnTransactionsForCustomerWithinDateRange() {
        List<Transaction> transactions = transactionRepository.findTransactionsByCustomerIdAndDateRange("C1002",
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31));

        assertEquals(4, transactions.size());
        assertTrue(transactions.stream().allMatch(transaction -> "C1002".equals(transaction.getCustomerId())));
        assertEquals("T20003", transactions.get(0).getTransactionId());
        assertEquals("T20006", transactions.get(transactions.size() - 1).getTransactionId());
    }

    @Test
    void shouldReturnLatestTransactionDateForCustomer() {
        Optional<LocalDate> latestTransactionDate = transactionRepository.findLatestTransactionDateByCustomerId("C1001");

        assertTrue(latestTransactionDate.isPresent());
        assertEquals(LocalDate.of(2026, 3, 22), latestTransactionDate.get());
    }

    @Test
    void shouldExcludeTransactionsAfterEndDate() {
        List<Transaction> transactions = transactionRepository.findTransactionsByCustomerIdAndDateRange("C1001",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));

        assertEquals(4, transactions.size());
        assertEquals("T10004", transactions.get(transactions.size() - 1).getTransactionId());
        assertTrue(transactions.stream()
                .allMatch(transaction -> !transaction.getTransactionDate().isAfter(LocalDate.of(2026, 2, 28))));
    }
}
