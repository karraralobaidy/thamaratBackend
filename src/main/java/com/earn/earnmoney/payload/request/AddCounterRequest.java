package com.earn.earnmoney.payload.request;

import lombok.Data;

@Data
public class AddCounterRequest {
    private String name;
    private long cooldownHours;
    private Long price;
    private boolean paid;
    private boolean active;
    private Long durationDays;

    // حقول إضافية لإنشاء المستوى الأول
    private int pointsPerClick;
    // private int upgradeCost; // تم الإلغاء
}
