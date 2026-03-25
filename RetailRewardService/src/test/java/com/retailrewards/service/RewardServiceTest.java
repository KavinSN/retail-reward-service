package com.retailrewards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.retailrewards.dto.response.CustomerRewardResponse;
import com.retailrewards.exception.CustomerNotFoundException;
import com.retailrewards.exception.InvalidRequestException;
import com.retailrewards.model.Customer;
import com.retailrewards.model.Transaction;
import com.retailrewards.repository.CustomerRepository;
import com.retailrewards.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        rewardService = new RewardService(transactionRepository, customerRepository, new RewardCalculator());
    }

    @Test
    void shouldReturnCombinedRewardsForSpecificCustomerUsingDefaultThreeMonths() {
        Customer customer = new Customer("C1001", "Kavin");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T1", "C1001", LocalDate.of(2026, 1, 5), new BigDecimal("120.00"), "January order"),
                new Transaction("T2", "C1001", LocalDate.of(2026, 3, 10), new BigDecimal("75.00"), "March order"));

        when(customerRepository.findById("C1001")).thenReturn(Optional.of(customer));
        when(transactionRepository.findLatestTransactionDateByCustomerIdAsync("C1001"))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(LocalDate.of(2026, 3, 10))));
        when(transactionRepository.findTransactionsByCustomerIdAndDateRangeAsync(eq("C1001"), any(LocalDate.class),
                any(LocalDate.class))).thenReturn(CompletableFuture.completedFuture(transactions));

        CustomerRewardResponse response = rewardService.getCustomerRewards("C1001", null, null, null);

        assertEquals("C1001", response.getCustomerId());
        assertEquals("Kavin", response.getCustomerName());
        assertEquals("2026-01-01", response.getStartDate());
        assertEquals("2026-03-31", response.getEndDate());
        assertEquals(Long.valueOf(90L), response.getMonthlyPoints().get("2026-Jan"));
        assertEquals(Long.valueOf(25L), response.getMonthlyPoints().get("2026-Mar"));
        assertEquals(115L, response.getTotalPoints());
        assertEquals(2, response.getTransactions().size());
    }

    @Test
    void shouldReturnCombinedRewardsForCustomDateRange() {
        Customer customer = new Customer("C1002", "Prabhu");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T2", "C1002", LocalDate.of(2026, 2, 10), new BigDecimal("99.00"), "Inside range"),
                new Transaction("T3", "C1002", LocalDate.of(2026, 3, 2), new BigDecimal("55.00"), "Inside range"));

        when(customerRepository.findById("C1002")).thenReturn(Optional.of(customer));
        when(transactionRepository.findTransactionsByCustomerIdAndDateRangeAsync("C1002",
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn(CompletableFuture.completedFuture(transactions));

        CustomerRewardResponse response = rewardService.getCustomerRewards("C1002", null,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31));

        assertEquals("C1002", response.getCustomerId());
        assertEquals("Prabhu", response.getCustomerName());
        assertEquals("2026-02-01", response.getStartDate());
        assertEquals("2026-03-31", response.getEndDate());
        assertEquals(2, response.getMonthlyPoints().size());
        assertEquals(Long.valueOf(49L), response.getMonthlyPoints().get("2026-Feb"));
        assertEquals(Long.valueOf(5L), response.getMonthlyPoints().get("2026-Mar"));
        assertEquals(54L, response.getTotalPoints());
        assertEquals(2, response.getTransactions().size());
    }

    @Test
    void shouldThrowWhenCustomerIdIsBlank() {
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> rewardService.getCustomerRewards("   ", null, null, null));

        assertEquals("customerId is required", exception.getMessage());
    }

    @Test
    void shouldThrowWhenRequestedForUnknownCustomer() {
        when(customerRepository.findById("C9999")).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,
                () -> rewardService.getCustomerRewards("C9999", null, null, null));

        assertTrue(exception.getMessage().contains("C9999"));
    }

    @Test
    void shouldReturnZeroPointsWhenRequestedRangeHasNoData() {
        Customer customer = new Customer("C1001", "Kavin");

        when(customerRepository.findById("C1001")).thenReturn(Optional.of(customer));
        when(transactionRepository.findTransactionsByCustomerIdAndDateRangeAsync("C1001",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        CustomerRewardResponse response = rewardService.getCustomerRewards("C1001", null,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        assertEquals("2026-04-01", response.getStartDate());
        assertEquals("2026-04-30", response.getEndDate());
        assertEquals(0L, response.getTotalPoints());
        assertTrue(response.getMonthlyPoints().isEmpty());
        assertTrue(response.getTransactions().isEmpty());
    }

    @Test
    void shouldThrowWhenNoTransactionsExistForCustomerDateResolution() {
        when(customerRepository.findById("C1001"))
                .thenReturn(Optional.of(new Customer("C1001", "Kavin")));
        when(transactionRepository.findLatestTransactionDateByCustomerIdAsync("C1001"))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> rewardService.getCustomerRewards("C1001", null, null, null));

        assertEquals("No transaction data available", exception.getMessage());
    }
}
