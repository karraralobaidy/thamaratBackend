package com.earn.earnmoney.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SubscriberFullDetailDTO {
    // Subscriber fields
    // Subscriber fields
    private Long subscriberId;
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private Long point;         // Corrected: Based on Subscriber entity (Long)
    private Double profit;
    private Integer duration;   // Corrected: Based on Subscriber entity (int -> Integer)
    private Double income;
    private String name;
    private String wallet;

    // UserAuth fields (from r.user) - Assuming these types and names from UserAuth
    private String userFullName;
    private String userPhone;
    private Integer numberOfReferral;
    private String username;
    private Long userId;
    private Boolean userActive;
    private Boolean userBand;

    // PUBLIC Constructor - Order and types MUST EXACTLY MATCH the SELECT clause
    public SubscriberFullDetailDTO(
            Long subscriberId,
            LocalDate dateStart,
            LocalDate dateEnd,
            Long point,
            Double profit,
            Integer duration,
            Double income,
            String name,
            String wallet,
            String userFullName,
            String userPhone,
            Integer numberOfReferral,
            String username,
            Long userId,
            Boolean userActive,
            Boolean userBand // <--- **THIS IS THE CRITICAL CHANGE**
    ) {
        this.subscriberId = subscriberId;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.point = point;
        this.profit = profit;
        this.duration = duration;
        this.income = income;
        this.name = name;
        this.wallet = wallet;
        this.userFullName = userFullName;
        this.userPhone = userPhone;
        this.numberOfReferral = numberOfReferral;
        this.username = username;
        this.userId = userId;
        this.userActive = userActive;
        this.userBand = userBand;
    }

}
