package com.retailrewards.service;

import com.retailrewards.calculator.RewardCalculator;
import com.retailrewards.dto.response.CustomerRewardResponse;
import com.retailrewards.dto.response.MonthlyRewardPoints;
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
import java.util.List;
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

    /**
     * Builds the reward response for a single customer using either a rolling month filter or a date range.
     *
     * @param customerId customer identifier to evaluate
     * @param months optional rolling month count
     * @param startDate optional range start date
     * @param endDate optional range end date
     * @return reward response including monthly points, total points, and transaction details
     */
    public CustomerRewardResponse getCustomerRewards(String customerId, Integer months, LocalDate startDate,
            LocalDate endDate) {
        ValidationUtils.validateCustomerId(customerId);
        Customer customer = findCustomerById(customerId);
        DateRange dateRange = resolveCustomerDateRange(customerId, months, startDate, endDate);
        List<Transaction> transactions = getTransactionsByCustomerAndDateRange(customerId, dateRange);

        log.info("Processing customer {} using range {} to {}", customerId, dateRange.getStartDate(),
                dateRange.getEndDate());

        return buildCustomerRewardResponse(customer, dateRange, transactions);
    }

    private Customer findCustomerById(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    private DateRange resolveCustomerDateRange(String customerId, Integer months, LocalDate startDate,
            LocalDate endDate) {
        ValidationUtils.validateRequest(months, startDate, endDate);
        if (months != null) {
            return resolveRollingDateRange(customerId, months);
        }
        if (startDate != null && endDate != null) {
            return new DateRange(startDate, endDate);
        }
        if (startDate != null) {
            return new DateRange(startDate,
                    startDate.plusMonths(ApplicationConstants.DEFAULT_MONTH_COUNT).minusDays(1));
        }
        if (endDate != null) {
            return new DateRange(endDate.minusMonths(ApplicationConstants.DEFAULT_MONTH_COUNT).plusDays(1), endDate);
        }

        return resolveRollingDateRange(customerId, ApplicationConstants.DEFAULT_MONTH_COUNT);
    }

    private DateRange resolveRollingDateRange(String customerId, int monthCount) {
        LocalDate latestTransactionDate = transactionRepository.findLatestTransactionDateByCustomerIdAsync(customerId)
                .join()
                .orElseThrow(() -> new InvalidRequestException(ApplicationConstants.MESSAGE_NO_TRANSACTIONS));

        YearMonth endingMonth = YearMonth.from(latestTransactionDate);
        return new DateRange(endingMonth.minusMonths(monthCount - 1L).atDay(1), endingMonth.atEndOfMonth());
    }

    private List<Transaction> getTransactionsByCustomerAndDateRange(String customerId, DateRange dateRange) {
        return transactionRepository.findTransactionsByCustomerIdAndDateRangeAsync(customerId, dateRange.getStartDate(),
                dateRange.getEndDate()).join().stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .collect(Collectors.toList());
    }

    private CustomerRewardResponse buildCustomerRewardResponse(Customer customer, DateRange dateRange,
            List<Transaction> transactions) {
        List<RewardTransaction> rewardTransactions = transactions.stream()
                .map(transaction -> new RewardTransaction(transaction.getAmount(), transaction.getTransactionDate()))
                .collect(Collectors.toList());

        List<MonthlyRewardPoints> monthlyPoints = rewardCalculator.calculateMonthlyPoints(rewardTransactions);
        long totalPoints = rewardCalculator.calculateTotalPoints(rewardTransactions);
        List<TransactionRewardDetails> transactionRewardDetails = transactions.stream()
                .map(this::toTransactionRewardDetails)
                .collect(Collectors.toList());

        return new CustomerRewardResponse(customer.getCustomerId(), customer.getCustomerName(),
                dateRange.getStartDate().format(DATE_FORMATTER), dateRange.getEndDate().format(DATE_FORMATTER),
                monthlyPoints, totalPoints, transactionRewardDetails);
    }

    private TransactionRewardDetails toTransactionRewardDetails(Transaction transaction) {
        return new TransactionRewardDetails(transaction.getTransactionId(),
                transaction.getTransactionDate().format(DATE_FORMATTER), transaction.getDescription(),
                formatAmount(transaction.getAmount()), rewardCalculator.calculatePoints(transaction.getAmount()));
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
