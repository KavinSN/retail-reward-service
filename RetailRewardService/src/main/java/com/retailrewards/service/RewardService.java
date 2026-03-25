package com.retailrewards.service;

import com.retailrewards.dto.response.CustomerRewardResponse;
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
import java.util.Map;
import java.util.function.Function;
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

    public CustomerRewardResponse getCustomerRewards(String customerId, Integer months, LocalDate startDate,
            LocalDate endDate) {
        return processCustomerTransactions(customerId, months, startDate, endDate, this::buildCustomerRewardResponse);
    }

    private CustomerRewardResponse buildCustomerRewardResponse(CustomerProcessingContext context) {
        List<RewardTransaction> rewardTransactions = context.getTransactions().stream()
                .map(transaction -> new RewardTransaction(transaction.getAmount(), transaction.getTransactionDate()))
                .collect(Collectors.toList());

        Map<String, Long> monthlyPoints = rewardCalculator.calculateMonthlyPoints(rewardTransactions);
        long totalPoints = rewardCalculator.calculateTotalPoints(rewardTransactions);
        List<TransactionRewardDetails> transactions = context.getTransactions().stream()
                .map(this::toTransactionRewardDetails)
                .collect(Collectors.toList());

        return new CustomerRewardResponse(context.getCustomer().getCustomerId(),
                context.getCustomer().getCustomerName(),
                context.getDateRange().getStartDate().format(DATE_FORMATTER),
                context.getDateRange().getEndDate().format(DATE_FORMATTER), monthlyPoints, totalPoints, transactions);
    }

    private <T> T processCustomerTransactions(String customerId, Integer months, LocalDate startDate,
            LocalDate endDate, Function<CustomerProcessingContext, T> responseBuilder) {
        ValidationUtils.validateCustomerId(customerId);
        Customer customer = findCustomerById(customerId);
        DateRange dateRange = resolveCustomerDateRange(customerId, months, startDate, endDate);
        List<Transaction> transactions = getTransactionsByCustomerAndDateRange(customerId, dateRange);

        log.info("Processing customer {} using range {} to {}", customerId, dateRange.getStartDate(),
                dateRange.getEndDate());

        return responseBuilder.apply(new CustomerProcessingContext(customer, dateRange, transactions));
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

    private TransactionRewardDetails toTransactionRewardDetails(Transaction transaction) {
        return new TransactionRewardDetails(transaction.getTransactionId(),
                transaction.getTransactionDate().format(DATE_FORMATTER), transaction.getDescription(),
                formatAmount(transaction.getAmount()), rewardCalculator.calculatePoints(transaction.getAmount()));
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private Customer findCustomerById(String customerId) {
        ValidationUtils.validateCustomerId(customerId);
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    @lombok.Getter
    @lombok.RequiredArgsConstructor
    private static class CustomerProcessingContext {

        private final Customer customer;
        private final DateRange dateRange;
        private final List<Transaction> transactions;
    }
}
