package com.retailrewards.repository;

import com.retailrewards.model.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    /**
     * Looks up a customer by identifier.
     *
     * @param customerId customer identifier to search for
     * @return matching customer when present
     */
    Optional<Customer> findByCustomerIdIgnoreCase(String customerId);
}
