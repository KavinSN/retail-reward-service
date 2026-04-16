package com.retailrewards.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.retailrewards.model.Customer;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldLoadCustomerSeedDataFromH2Database() {
        assertEquals(3, customerRepository.count());
    }

    @Test
    void shouldFindCustomerByIdIgnoringCase() {
        Optional<Customer> customer = customerRepository.findByCustomerIdIgnoreCase("c1001");

        assertTrue(customer.isPresent());
        assertEquals("C1001", customer.get().getCustomerId());
        assertEquals("Kavin", customer.get().getCustomerName());
    }

    @Test
    void shouldReturnEmptyWhenCustomerDoesNotExist() {
        Optional<Customer> customer = customerRepository.findByCustomerIdIgnoreCase("C9999");

        assertFalse(customer.isPresent());
    }
}
