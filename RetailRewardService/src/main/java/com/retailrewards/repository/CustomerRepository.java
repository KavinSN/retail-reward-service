package com.retailrewards.repository;

import com.retailrewards.model.Customer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepository {

    private final List<Customer> customers = Arrays.asList(
            new Customer("C1001", "Kavin"),
            new Customer("C1002", "Prabhu"),
            new Customer("C1003", "KP"));

    /**
     * Looks up a customer by identifier from the in-memory dataset.
     *
     * @param customerId customer identifier to search for
     * @return matching customer when present
     */
    public Optional<Customer> findById(String customerId) {
        return customers.stream()
                .filter(customer -> customer.getCustomerId().equalsIgnoreCase(customerId))
                .findFirst();
    }
}
