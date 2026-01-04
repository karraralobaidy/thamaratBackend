package com.earn.earnmoney.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryResponse {
    private Long id;
    private String username;
    private String full_name;
    private boolean active;
    private boolean band;
    private Long points;
    private String referralCode;
    private int numberOfReferral;
    private Set<String> roles;
}
