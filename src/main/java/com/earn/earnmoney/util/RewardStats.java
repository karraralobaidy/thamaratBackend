package com.earn.earnmoney.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RewardStats {
    private Long totalUsers;         // إجمالي عدد المستخدمين
    private Double averageStreak;    // متوسط سلسلة المكافآت
    private Integer maxStreak;       // أعلى سلسلة مكافآت
    private Long totalRewardsGiven;  // إجمالي المكافآت الممنوحة
}