package com.earn.earnmoney.model;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_counters")
@Getter
@Setter
public class UserCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private UserAuth user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Counter counter;

    @ManyToOne(fetch = FetchType.LAZY)
    private CounterPackage currentPackage;

    private int level = 1;

    private LocalDateTime lastClickedAt;

    private LocalDateTime expireAt; // تاريخ انتهاء الاشتراك (سنة واحدة)

    private LocalDateTime subscribedAt; // تاريخ الاشتراك
}
