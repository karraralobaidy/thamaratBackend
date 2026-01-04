package com.earn.earnmoney.util;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DailyRewardStatus {
    private boolean canClaim;
    private LocalDateTime lastClaimDate;
    private Integer streakCount;
    private Long totalRewards;
}