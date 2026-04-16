package com.retailrewards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.retailrewards.dto.response.CustomerRewardResponse;
import com.retailrewards.exception.RewardException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        rewardService = new RewardService(transactionRepository, customerRepository);
    }

    @Test
    void shouldReturnCombinedRewardsForSpecificCustomerUsingDefaultThreeMonths() {
        Customer customer = new Customer("C1001", "Kavin");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T1", "C1001", LocalDate.of(2026, 1, 5), new BigDecimal("120.00"), "January order"),
                new Transaction("T2", "C1001", LocalDate.of(2026, 3, 10), new BigDecimal("75.00"), "March order"));

        when(customerRepository.findByCustomerIdIgnoreCase("C1001")).thenReturn(Optional.of(customer));
        mockLatestTransactionDate("C1001", LocalDate.of(2026, 3, 10));
        mockTransactions("C1001", transactions);

        CustomerRewardResponse response = rewardService.getCustomerRewards("C1001", null, null, null);

        assertEquals("C1001", response.getCustomerId());
        assertEquals("Kavin", response.getCustomerName());
        assertEquals("2026-01-01", response.getStartDate());
        assertEquals("2026-03-31", response.getEndDate());
        assertEquals(2, response.getMonthlyPoints().size());
        assertEquals(2026, response.getMonthlyPoints().get(0).getYear());
        assertEquals("March", response.getMonthlyPoints().get(0).getMonth());
        assertEquals(25L, response.getMonthlyPoints().get(0).getRewardPoints());
        assertEquals(2026, response.getMonthlyPoints().get(1).getYear());
        assertEquals("January", response.getMonthlyPoints().get(1).getMonth());
        assertEquals(90L, response.getMonthlyPoints().get(1).getRewardPoints());
        assertEquals(115L, response.getTotalPoints());
        assertEquals(2, response.getTransactions().size());
    }

    @Test
    void shouldCalculateTieredPointsAndRoundAmountsDownInsideServiceResponse() {
        Customer customer = new Customer("C1001", "Kavin");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T1", "C1001", LocalDate.of(2026, 3, 10), new BigDecimal("50.99"), "No points"),
                new Transaction("T2", "C1001", LocalDate.of(2026, 3, 11), new BigDecimal("100.99"), "Single tier"),
                new Transaction("T3", "C1001", LocalDate.of(2026, 3, 12), new BigDecimal("120.99"), "Double tier"));

        when(customerRepository.findByCustomerIdIgnoreCase("C1001")).thenReturn(Optional.of(customer));
        mockTransactions("C1001", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), transactions);

        CustomerRewardResponse response = rewardService.getCustomerRewards("C1001", null,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertEquals(140L, response.getTotalPoints());
        assertEquals(1, response.getMonthlyPoints().size());
        assertEquals(140L, response.getMonthlyPoints().get(0).getRewardPoints());
        assertEquals(0L, response.getTransactions().get(0).getRewardPoints());
        assertEquals(50L, response.getTransactions().get(1).getRewardPoints());
        assertEquals(90L, response.getTransactions().get(2).getRewardPoints());
    }

    @Test
    void shouldReturnCombinedRewardsForCustomDateRange() {
        Customer customer = new Customer("C1002", "Prabhu");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T2", "C1002", LocalDate.of(2026, 2, 10), new BigDecimal("99.00"), "Inside range"),
                new Transaction("T3", "C1002", LocalDate.of(2026, 3, 2), new BigDecimal("55.00"), "Inside range"));

        when(customerRepository.findByCustomerIdIgnoreCase("C1002")).thenReturn(Optional.of(customer));
        mockTransactions("C1002", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31), transactions);

        CustomerRewardResponse response = rewardService.getCustomerRewards("C1002", null,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31));

        assertEquals("C1002", response.getCustomerId());
        assertEquals("Prabhu", response.getCustomerName());
        assertEquals("2026-02-01", response.getStartDate());
        assertEquals("2026-03-31", response.getEndDate());
        assertEquals(2, response.getMonthlyPoints().size());
        assertEquals(2026, response.getMonthlyPoints().get(0).getYear());
        assertEquals("March", response.getMonthlyPoints().get(0).getMonth());
        assertEquals(5L, response.getMonthlyPoints().get(0).getRewardPoints());
        assertEquals(2026, response.getMonthlyPoints().get(1).getYear());
        assertEquals("February", response.getMonthlyPoints().get(1).getMonth());
        assertEquals(49L, response.getMonthlyPoints().get(1).getRewardPoints());
        assertEquals(54L, response.getTotalPoints());
        assertEquals(2, response.getTransactions().size());
    }

    @Test
    void shouldResolveThreeMonthWindowWhenOnlyStartDateIsProvided() {
        Customer customer = new Customer("C1001", "Kavin");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T1", "C1001", LocalDate.of(2026, 2, 4), new BigDecimal("45.00"), "Inside range"),
                new Transaction("T2", "C1001", LocalDate.of(2026, 3, 12), new BigDecimal("210.00"), "Inside range"));

        when(customerRepository.findByCustomerIdIgnoreCase("C1001")).thenReturn(Optional.of(customer));
        mockTransactions("C1001", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 4, 30), transactions);

        CustomerRewardResponse response = rewardService.getCustomerRewards("C1001", null,
                LocalDate.of(2026, 2, 1), null);

        assertEquals("2026-02-01", response.getStartDate());
        assertEquals("2026-04-30", response.getEndDate());
        assertEquals(2, response.getTransactions().size());
    }

    @Test
    void shouldResolveThreeMonthWindowWhenOnlyEndDateIsProvided() {
        Customer customer = new Customer("C1001", "Kavin");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T1", "C1001", LocalDate.of(2026, 1, 21), new BigDecimal("75.00"), "Inside range"),
                new Transaction("T2", "C1001", LocalDate.of(2026, 3, 22), new BigDecimal("51.25"), "Inside range"));

        when(customerRepository.findByCustomerIdIgnoreCase("C1001")).thenReturn(Optional.of(customer));
        mockTransactions("C1001", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), transactions);

        CustomerRewardResponse response = rewardService.getCustomerRewards("C1001", null,
                null, LocalDate.of(2026, 3, 31));

        assertEquals("2026-01-01", response.getStartDate());
        assertEquals("2026-03-31", response.getEndDate());
        assertEquals(2, response.getTransactions().size());
    }

    @Test
    void shouldThrowWhenCustomerIdIsBlank() {
        RewardException exception = assertThrows(RewardException.class,
                () -> rewardService.getCustomerRewards("   ", null, null, null));

        assertEquals("customerId is required", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldThrowWhenRequestedForUnknownCustomer() {
        when(customerRepository.findByCustomerIdIgnoreCase("C9999")).thenReturn(Optional.empty());

        RewardException exception = assertThrows(RewardException.class,
                () -> rewardService.getCustomerRewards("C9999", null, null, null));

        assertTrue(exception.getMessage().contains("C9999"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void shouldReturnZeroPointsWhenRequestedRangeHasNoData() {
        Customer customer = new Customer("C1001", "Kavin");

        when(customerRepository.findByCustomerIdIgnoreCase("C1001")).thenReturn(Optional.of(customer));
        mockTransactions("C1001", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), Collections.emptyList());

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
        when(customerRepository.findByCustomerIdIgnoreCase("C1001"))
                .thenReturn(Optional.of(new Customer("C1001", "Kavin")));
        mockNoLatestTransactionDate("C1001");

        RewardException exception = assertThrows(RewardException.class,
                () -> rewardService.getCustomerRewards("C1001", null, null, null));

        assertEquals("No transaction data available", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldThrowWhenMonthsAndDateRangeAreProvidedTogether() {
        when(customerRepository.findByCustomerIdIgnoreCase("C1001"))
                .thenReturn(Optional.of(new Customer("C1001", "Kavin")));

        RewardException exception = assertThrows(RewardException.class,
                () -> rewardService.getCustomerRewards("C1001", 2, LocalDate.of(2026, 2, 1), null));

        assertEquals("Provide either months or a startDate/endDate range, not both", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldThrowWhenStartDateIsAfterEndDate() {
        when(customerRepository.findByCustomerIdIgnoreCase("C1001"))
                .thenReturn(Optional.of(new Customer("C1001", "Kavin")));

        RewardException exception = assertThrows(RewardException.class,
                () -> rewardService.getCustomerRewards("C1001", null, LocalDate.of(2026, 3, 31),
                        LocalDate.of(2026, 1, 1)));

        assertEquals("Provide a valid date range with startDate on or before endDate", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldPropagateFailureWhenLatestTransactionLookupFails() {
        when(customerRepository.findByCustomerIdIgnoreCase("C1001"))
                .thenReturn(Optional.of(new Customer("C1001", "Kavin")));
        mockLatestTransactionFailure("C1001", new IllegalStateException("lookup failed"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> rewardService.getCustomerRewards("C1001", null, null, null));

        assertEquals("lookup failed", exception.getMessage());
    }

    @Test
    void shouldPropagateFailureWhenTransactionLookupFails() {
        when(customerRepository.findByCustomerIdIgnoreCase("C1001"))
                .thenReturn(Optional.of(new Customer("C1001", "Kavin")));
        mockTransactionFailure("C1001", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31),
                new IllegalStateException("transaction fetch failed"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> rewardService.getCustomerRewards("C1001", null, LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 3, 31)));

        assertEquals("transaction fetch failed", exception.getMessage());
    }

    private void mockLatestTransactionDate(String customerId, LocalDate transactionDate) {
        when(transactionRepository.findLatestTransactionDateByCustomerId(customerId)).thenReturn(Optional.of(transactionDate));
    }

    private void mockNoLatestTransactionDate(String customerId) {
        when(transactionRepository.findLatestTransactionDateByCustomerId(customerId)).thenReturn(Optional.empty());
    }

    private void mockTransactions(String customerId, List<Transaction> transactions) {
        when(transactionRepository.findByCustomerIdIgnoreCaseAndTransactionDateBetweenOrderByTransactionDateAsc(
                customerId, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31))).thenReturn(transactions);
    }

    private void mockTransactions(String customerId, LocalDate startDate, LocalDate endDate,
            List<Transaction> transactions) {
        when(transactionRepository.findByCustomerIdIgnoreCaseAndTransactionDateBetweenOrderByTransactionDateAsc(
                customerId, startDate, endDate)).thenReturn(transactions);
    }

    private void mockLatestTransactionFailure(String customerId, Throwable throwable) {
        when(transactionRepository.findLatestTransactionDateByCustomerId(customerId))
                .thenThrow(throwable);
    }

    private void mockTransactionFailure(String customerId, LocalDate startDate, LocalDate endDate,
            Throwable throwable) {
        when(transactionRepository.findByCustomerIdIgnoreCaseAndTransactionDateBetweenOrderByTransactionDateAsc(
                customerId, startDate, endDate)).thenThrow(throwable);
    }
}
