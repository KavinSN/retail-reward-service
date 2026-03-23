package com.retailrewards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CustomerTest {

    @Test
    void shouldExposeCustomerFields() {
        Customer customer = new Customer("C1001", "Kavin", "Gold", "kavin@example.com");

        assertEquals("C1001", customer.getCustomerId());
        assertEquals("Kavin", customer.getCustomerName());
        assertEquals("Gold", customer.getTier());
        assertEquals("kavin@example.com", customer.getEmail());
    }
}
