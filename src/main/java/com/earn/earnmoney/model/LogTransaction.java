package com.earn.earnmoney.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogTransaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;
    @Column(nullable = true)
    private String fullName;
    @Column(nullable = true)
    private String username;

    @Column(nullable = false)
    private String type; // DAILY_REWARD, WITHDRAWAL, DEPOSIT, etc.

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(length = 500)
    private String description;

    @Column(name = "previous_balance")
    private Double previousBalance;

    @Column(name = "new_balance")
    private Double newBalance;
}