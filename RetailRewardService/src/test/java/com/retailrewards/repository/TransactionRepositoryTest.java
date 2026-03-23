package com.retailrewards.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.retailrewards.model.Transaction;
import java.util.List;
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
    void shouldShutdownExecutorWithoutErrors() {
        transactionRepository.shutdownExecutor();

        assertTrue(true);
    }
}
