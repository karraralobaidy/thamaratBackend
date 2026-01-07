package com.earn.earnmoney.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawDetailDTO {
    private Long id;
    private double amount;
    private boolean processed;
    private String status; // PENDING, APPROVED, REJECTED
    private String reason;
    private LocalDate requestDate;
    private String paymentMethod;
    private String wallet;
}
