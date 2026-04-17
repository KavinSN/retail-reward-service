package com.retailrewards.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reward_transactions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @Column(name = "transaction_id", nullable = false, length = 20)
    private String transactionId;

    @Column(name = "customer_id", nullable = false, length = 20)
    private String customerId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "description", nullable = false, length = 150)
    private String description;
}
