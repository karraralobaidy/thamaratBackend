package com.earn.earnmoney.util;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class DailyRewardResult {
    private Integer streakCount;
    private Long totalRewards;
    private Long amount;
}
