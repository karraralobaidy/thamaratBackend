package com.earn.earnmoney.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCounterSubscriptionDTO {
    private Long id;
    private String counterName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer dailyPoints;
    private Long counterId;
    private String status; // ACTIVE, EXPIRED
    private boolean isPaid;
}
