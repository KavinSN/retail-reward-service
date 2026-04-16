package com.retailrewards.repository;

import com.retailrewards.model.Transaction;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Retrieves transactions for a customer within the provided inclusive date range.
     *
     * @param customerId customer identifier to filter by
     * @param startDate inclusive range start date
     * @param endDate inclusive range end date
     * @return matching transactions
     */
    List<Transaction> findByCustomerIdIgnoreCaseAndTransactionDateBetweenOrderByTransactionDateAsc(String customerId,
            LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves the latest transaction date available for the requested customer.
     *
     * @param customerId customer identifier to filter by
     * @return latest transaction date when available
     */
    @Query("select max(transaction.transactionDate) from Transaction transaction "
            + "where lower(transaction.customerId) = lower(:customerId)")
    Optional<LocalDate> findLatestTransactionDateByCustomerId(@Param("customerId") String customerId);
}
