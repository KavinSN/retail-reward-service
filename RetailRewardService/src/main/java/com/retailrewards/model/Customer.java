package com.retailrewards.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Customer {

    private final String customerId;
    private final String customerName;
    private final String tier;
    private final String email;
}
