package com.retailrewards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.retailrewards.dto.response.CustomerRewardDetails;
import com.retailrewards.dto.response.CustomerTransactionResponse;
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
    void shouldReturnRewardsForAllCustomersUsingDefaultThreeMonths() {
        Customer customerOne = new Customer("C1001", "Kavin", "Gold", "kavin@example.com");
        Customer customerTwo = new Customer("C1002", "Prabhu", "Silver", "prabhu@example.com");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T1", "C1001", LocalDate.of(2026, 1, 5), new BigDecimal("120.00"), "January order"),
                new Transaction("T2", "C1001", LocalDate.of(2026, 3, 10), new BigDecimal("75.00"), "March order"),
                new Transaction("T3", "C1002", LocalDate.of(2026, 2, 11), new BigDecimal("140.00"), "February order"),
                new Transaction("T4", "C1002", LocalDate.of(2026, 3, 14), new BigDecimal("49.00"), "Ignored points"),
                new Transaction("T5", "C9999", LocalDate.of(2026, 3, 15), new BigDecimal("200.00"), "Other customer"));

        when(customerRepository.findAll()).thenReturn(Arrays.asList(customerOne, customerTwo));
        when(transactionRepository.findAllTransactionsAsync())
                .thenReturn(CompletableFuture.completedFuture(transactions));

        List<CustomerRewardDetails> response = rewardService.calculateRewards(null, null, null, null);

        assertEquals(2, response.size());
        assertEquals("C1001", response.get(0).getCustomerId());
        assertEquals("Kavin", response.get(0).getCustomerName());
        assertEquals(Long.valueOf(90L), response.get(0).getMonthlyPoints().get("JANUARY"));
        assertEquals(Long.valueOf(25L), response.get(0).getMonthlyPoints().get("MARCH"));
        assertEquals(115L, response.get(0).getTotalPoints());

        assertEquals("C1002", response.get(1).getCustomerId());
        assertEquals("Prabhu", response.get(1).getCustomerName());
        assertEquals(Long.valueOf(130L), response.get(1).getMonthlyPoints().get("FEBRUARY"));
        assertEquals(Long.valueOf(0L), response.get(1).getMonthlyPoints().get("MARCH"));
        assertEquals(130L, response.get(1).getTotalPoints());
    }

    @Test
    void shouldReturnSingleCustomerRewardsForCustomDateRange() {
        Customer customer = new Customer("C1002", "Prabhu", "Silver", "prabhu@example.com");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T1", "C1002", LocalDate.of(2026, 1, 5), new BigDecimal("120.00"), "Outside range"),
                new Transaction("T2", "C1002", LocalDate.of(2026, 2, 10), new BigDecimal("99.00"), "Inside range"),
                new Transaction("T3", "C1002", LocalDate.of(2026, 3, 2), new BigDecimal("55.00"), "Inside range"));

        when(customerRepository.findById("C1002")).thenReturn(Optional.of(customer));
        when(transactionRepository.findAllTransactionsAsync())
                .thenReturn(CompletableFuture.completedFuture(transactions));

        List<CustomerRewardDetails> response = rewardService.calculateRewards("C1002", null,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31));

        assertEquals(1, response.size());
        assertEquals("C1002", response.get(0).getCustomerId());
        assertEquals("Prabhu", response.get(0).getCustomerName());
        assertEquals(2, response.get(0).getMonthlyPoints().size());
        assertEquals(Long.valueOf(49L), response.get(0).getMonthlyPoints().get("FEBRUARY"));
        assertEquals(Long.valueOf(5L), response.get(0).getMonthlyPoints().get("MARCH"));
        assertEquals(54L, response.get(0).getTotalPoints());
    }

    @Test
    void shouldReturnAllCustomersWhenCustomerIdIsBlank() {
        Customer customer = new Customer("C1001", "Kavin", "Gold", "kavin@example.com");
        List<Transaction> transactions = Collections.singletonList(
                new Transaction("T1", "C1001", LocalDate.of(2026, 3, 10), new BigDecimal("75.00"), "March order"));

        when(customerRepository.findAll()).thenReturn(Collections.singletonList(customer));
        when(transactionRepository.findAllTransactionsAsync())
                .thenReturn(CompletableFuture.completedFuture(transactions));

        List<CustomerRewardDetails> response = rewardService.calculateRewards("   ", 1, null, null);

        assertEquals(1, response.size());
        assertEquals("C1001", response.get(0).getCustomerId());
        assertEquals(25L, response.get(0).getTotalPoints());
    }

    @Test
    void shouldReturnAllCustomersWhenCustomerIdsContainOnlyCommasAndSpaces() {
        Customer customer = new Customer("C1001", "Kavin", "Gold", "kavin@example.com");
        List<Transaction> transactions = Collections.singletonList(
                new Transaction("T1", "C1001", LocalDate.of(2026, 3, 10), new BigDecimal("75.00"), "March order"));

        when(customerRepository.findAll()).thenReturn(Collections.singletonList(customer));
        when(transactionRepository.findAllTransactionsAsync())
                .thenReturn(CompletableFuture.completedFuture(transactions));

        List<CustomerRewardDetails> response = rewardService.calculateRewards(" , , ", 1, null, null);

        assertEquals(1, response.size());
        assertEquals("C1001", response.get(0).getCustomerId());
    }

    @Test
    void shouldReturnCustomerTransactionsForRollingMonths() {
        Customer customer = new Customer("C1002", "Prabhu", "Silver", "prabhu@example.com");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T1", "C1002", LocalDate.of(2026, 1, 10), new BigDecimal("101.50"), "January"),
                new Transaction("T2", "C1002", LocalDate.of(2026, 2, 1), new BigDecimal("55.00"), "February"),
                new Transaction("T3", "C1002", LocalDate.of(2026, 3, 20), new BigDecimal("120.00"), "March"),
                new Transaction("T4", "C1002", LocalDate.of(2026, 4, 10), new BigDecimal("80.00"), "April"));

        when(customerRepository.findById("C1002")).thenReturn(Optional.of(customer));
        when(transactionRepository.findAllTransactionsAsync())
                .thenReturn(CompletableFuture.completedFuture(transactions));

        List<CustomerTransactionResponse> response = rewardService.getCustomerTransactions("C1002", 2, null, null);

        assertEquals(1, response.size());
        assertEquals("C1002", response.get(0).getCustomerId());
        assertEquals("Prabhu", response.get(0).getCustomerName());
        assertEquals(95L, response.get(0).getTotalPoints());
        assertEquals(2, response.get(0).getTransactions().size());
        assertEquals("T2", response.get(0).getTransactions().get(0).getTransactionId());
        assertEquals("55.00", response.get(0).getTransactions().get(0).getAmount());
        assertEquals(5L, response.get(0).getTransactions().get(0).getRewardPoints());
        assertEquals("T3", response.get(0).getTransactions().get(1).getTransactionId());
        assertEquals(90L, response.get(0).getTransactions().get(1).getRewardPoints());
    }

    @Test
    void shouldReturnTransactionsForMultipleCustomers() {
        Customer firstCustomer = new Customer("C1001", "Kavin", "Gold", "kavin@example.com");
        Customer secondCustomer = new Customer("C1002", "Prabhu", "Silver", "prabhu@example.com");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T1", "C1001", LocalDate.of(2026, 3, 10), new BigDecimal("75.00"), "March one"),
                new Transaction("T2", "C1002", LocalDate.of(2026, 3, 11), new BigDecimal("120.00"), "March two"));

        when(customerRepository.findById("C1001")).thenReturn(Optional.of(firstCustomer));
        when(customerRepository.findById("C1002")).thenReturn(Optional.of(secondCustomer));
        when(transactionRepository.findAllTransactionsAsync())
                .thenReturn(CompletableFuture.completedFuture(transactions));

        List<CustomerTransactionResponse> response = rewardService.getCustomerTransactions("C1001,C1002", 1, null,
                null);

        assertEquals(2, response.size());
        assertEquals("C1001", response.get(0).getCustomerId());
        assertEquals(25L, response.get(0).getTotalPoints());
        assertEquals(1, response.get(0).getTransactions().size());
        assertEquals("C1002", response.get(1).getCustomerId());
        assertEquals(90L, response.get(1).getTotalPoints());
        assertEquals(1, response.get(1).getTransactions().size());
    }

    @Test
    void shouldRemoveDuplicateCustomerIdsWhenFetchingTransactions() {
        Customer customer = new Customer("C1002", "Prabhu", "Silver", "prabhu@example.com");
        List<Transaction> transactions = Collections.singletonList(
                new Transaction("T1", "C1002", LocalDate.of(2026, 3, 20), new BigDecimal("120.00"), "March"));

        when(customerRepository.findById("C1002")).thenReturn(Optional.of(customer));
        when(transactionRepository.findAllTransactionsAsync())
                .thenReturn(CompletableFuture.completedFuture(transactions));

        List<CustomerTransactionResponse> response = rewardService.getCustomerTransactions("C1002,C1002", 1, null, null);

        assertEquals(1, response.size());
        assertEquals("C1002", response.get(0).getCustomerId());
        assertEquals(90L, response.get(0).getTotalPoints());
    }

    @Test
    void shouldThrowWhenCustomerTransactionsRequestedForUnknownCustomer() {
        when(transactionRepository.findAllTransactionsAsync())
                .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(
                        new Transaction("T1", "C1001", LocalDate.of(2026, 3, 10), new BigDecimal("75.00"), "Order"))));
        when(customerRepository.findById("C9999")).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,
                () -> rewardService.getCustomerTransactions("C9999", null, null, null));

        assertTrue(exception.getMessage().contains("C9999"));
    }

    @Test
    void shouldThrowWhenRewardSummaryRequestedForUnknownCustomer() {
        when(customerRepository.findById("C9999")).thenReturn(Optional.empty());
        when(transactionRepository.findAllTransactionsAsync())
                .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(
                        new Transaction("T1", "C1001", LocalDate.of(2026, 3, 10), new BigDecimal("75.00"), "Order"))));

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,
                () -> rewardService.calculateRewards("C9999", null, null, null));

        assertTrue(exception.getMessage().contains("C9999"));
    }

    @Test
    void shouldUseMonthsWhenMonthsAndDateRangeAreProvided() {
        Customer customer = new Customer("C1001", "Kavin", "Gold", "kavin@example.com");
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T1", "C1001", LocalDate.of(2026, 1, 10), new BigDecimal("120.00"), "January"),
                new Transaction("T2", "C1001", LocalDate.of(2026, 2, 10), new BigDecimal("75.00"), "February"),
                new Transaction("T3", "C1001", LocalDate.of(2026, 3, 10), new BigDecimal("130.00"), "March"));

        when(customerRepository.findAll()).thenReturn(Collections.singletonList(customer));
        when(transactionRepository.findAllTransactionsAsync())
                .thenReturn(CompletableFuture.completedFuture(transactions));

        List<CustomerRewardDetails> response = rewardService.calculateRewards(null, 2,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1, response.size());
        assertEquals("C1001", response.get(0).getCustomerId());
        assertEquals(135L, response.get(0).getTotalPoints());
        assertTrue(response.get(0).getMonthlyPoints().containsKey("FEBRUARY"));
        assertTrue(response.get(0).getMonthlyPoints().containsKey("MARCH"));
        assertTrue(!response.get(0).getMonthlyPoints().containsKey("JANUARY"));
    }

    @Test
    void shouldThrowWhenNoTransactionsExistForDateResolution() {
        when(transactionRepository.findAllTransactionsAsync())
                .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> rewardService.calculateRewards(null, null, null, null));

        assertEquals("No transaction data available", exception.getMessage());
    }
}
