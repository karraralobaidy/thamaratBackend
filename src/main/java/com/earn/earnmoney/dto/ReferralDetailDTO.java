package com.earn.earnmoney.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferralDetailDTO {
    private Long id;
    private String username;
    private String fullName;
    private LocalDate joinDate;
    private String activeCounterName;
}
