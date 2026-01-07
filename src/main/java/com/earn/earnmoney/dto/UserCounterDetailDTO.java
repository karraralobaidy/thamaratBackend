package com.earn.earnmoney.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCounterDetailDTO {
    private Long id;
    private String name;
    private LocalDateTime expireAt;
    private Long pointsPerClick;
    private double price;
}
