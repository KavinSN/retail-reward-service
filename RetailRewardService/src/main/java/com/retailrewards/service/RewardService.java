package com.retailrewards.service;

import com.retailrewards.dto.response.CustomerRewardDetails;
import com.retailrewards.dto.response.CustomerTransactionResponse;
import com.retailrewards.dto.response.TransactionRewardDetails;
import com.retailrewards.exception.CustomerNotFoundException;
import com.retailrewards.model.Customer;
import com.retailrewards.model.DateRange;
import com.retailrewards.model.RewardTransaction;
import com.retailrewards.model.Transaction;
import com.retailrewards.exception.InvalidRequestException;
import com.retailrewards.repository.CustomerRepository;
import com.retailrewards.repository.TransactionRepository;
import com.retailrewards.util.ApplicationConstants;
import com.retailrewards.util.ValidationUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RewardService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final RewardCalculator rewardCalculator;

    public List<CustomerRewardDetails> calculateRewards(String customerId, Integer months, LocalDate startDate,
            LocalDate endDate) {
        List<Transaction> allTransactions = getAllTransactions();
        DateRange dateRange = resolveDateRange(allTransactions, months, startDate, endDate);
        List<Customer> customers = getCustomers(customerId);

        log.info("Calculating rewards using range {} to {}", dateRange.getStartDate(), dateRange.getEndDate());

        return customers.stream()
                .map(customer -> buildCustomerRewardDetails(customer, allTransactions, dateRange))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<CustomerTransactionResponse> getCustomerTransactions(String customerId, Integer months, LocalDate startDate,
            LocalDate endDate) {
        List<Transaction> allTransactions = getAllTransactions();
        DateRange dateRange = resolveDateRange(allTransactions, months, startDate, endDate);
        List<Customer> customers = getCustomers(customerId);

        return customers.stream()
                .map(customer -> buildCustomerTransactionResponse(customer, allTransactions, dateRange))
                .collect(Collectors.toList());
    }

    private CustomerTransactionResponse buildCustomerTransactionResponse(Customer customer, List<Transaction> transactions,
            DateRange dateRange) {
        List<Transaction> filteredTransactions = getFilteredTransactions(transactions, customer,
                dateRange.getStartDate(), dateRange.getEndDate());
        List<RewardTransaction> rewardTransactions = filteredTransactions.stream()
                .map(transaction -> new RewardTransaction(transaction.getAmount(), transaction.getTransactionDate()))
                .collect(Collectors.toList());

        return new CustomerTransactionResponse(
                customer.getCustomerId(),
                customer.getCustomerName(),
                rewardCalculator.calculateTotalPoints(rewardTransactions),
                filteredTransactions.stream()
                        .map(this::toTransactionRewardDetails)
                        .collect(Collectors.toList()));
    }

    private CustomerRewardDetails buildCustomerRewardDetails(Customer customer, List<Transaction> transactions,
            DateRange dateRange) {
        List<Transaction> filteredTransactions = getFilteredTransactions(transactions, customer,
                dateRange.getStartDate(), dateRange.getEndDate());

        List<RewardTransaction> rewardTransactions = filteredTransactions.stream()
                .map(transaction -> new RewardTransaction(transaction.getAmount(), transaction.getTransactionDate()))
                .collect(Collectors.toList());

        Map<String, Long> monthlyPoints = rewardCalculator.calculateMonthlyPoints(rewardTransactions);
        long totalPoints = rewardCalculator.calculateTotalPoints(rewardTransactions);

        return new CustomerRewardDetails(customer.getCustomerId(), customer.getCustomerName(), monthlyPoints,
                totalPoints);
    }

    private DateRange resolveDateRange(List<Transaction> allTransactions, Integer months, LocalDate startDate,
            LocalDate endDate) {
        ValidationUtils.validateRequest(months, startDate, endDate);
        if (months != null) {
            YearMonth currentMonth = YearMonth.from(LocalDate.now());
            return new DateRange(currentMonth.minusMonths(months - 1L).atDay(1), currentMonth.atEndOfMonth());
        }

        if (startDate != null && endDate != null) {
            return new DateRange(startDate, endDate);
        }

        LocalDate latestTransactionDate = allTransactions.stream()
                .map(Transaction::getTransactionDate)
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new InvalidRequestException(ApplicationConstants.MESSAGE_NO_TRANSACTIONS));

        int monthCount = months == null ? ApplicationConstants.DEFAULT_MONTH_COUNT : months;
        YearMonth endingMonth = YearMonth.from(latestTransactionDate);
        return new DateRange(endingMonth.minusMonths(monthCount - 1L).atDay(1), endingMonth.atEndOfMonth());
    }

    private List<Transaction> getAllTransactions() {
        CompletableFuture<List<Transaction>> transactionFuture = transactionRepository.findAllTransactionsAsync();
        return transactionFuture.join();
    }

    private List<Transaction> getFilteredTransactions(List<Transaction> transactions, Customer customer, LocalDate startDate,
            LocalDate endDate) {
        return transactions.stream()
                .filter(transaction -> customer.getCustomerId().equalsIgnoreCase(transaction.getCustomerId()))
                .filter(transaction -> !transaction.getTransactionDate().isBefore(startDate))
                .filter(transaction -> !transaction.getTransactionDate().isAfter(endDate))
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .collect(Collectors.toList());
    }

    private TransactionRewardDetails toTransactionRewardDetails(Transaction transaction) {
        return new TransactionRewardDetails(transaction.getTransactionId(),
                transaction.getTransactionDate().format(DATE_FORMATTER), transaction.getDescription(),
                formatAmount(transaction.getAmount()), rewardCalculator.calculatePoints(transaction.getAmount()));
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private List<Customer> getCustomers(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return customerRepository.findAll();
        }

        Set<String> customerIds = java.util.Arrays.stream(customerId.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (customerIds.isEmpty()) {
            return customerRepository.findAll();
        }

        return customerIds.stream()
                .map(this::findCustomerById)
                .collect(Collectors.toList());
    }

    private Customer findCustomerById(String customerId) {
        ValidationUtils.validateCustomerId(customerId);
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
}
