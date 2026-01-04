package com.earn.earnmoney.dto;

import lombok.Data;

@Data
public class CounterDTO {
    private Long id;
    private String name;
    private long cooldownHours;
    private Long price;
    private boolean paid;
    private boolean active;

    // معلومات المستوى الأول
    private int pointsPerClick;
    // private int upgradeCost; // تم الإلغاء
}
