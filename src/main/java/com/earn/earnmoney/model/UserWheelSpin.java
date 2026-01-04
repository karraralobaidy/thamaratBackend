package com.earn.earnmoney.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWheelSpin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAuth user;

    private LocalDateTime spinDate;

    // Optional: store the prize won details for history
    private String prizeName;
    private int pointsWon;

    // To differentiate between free and paid spins if needed later
    private boolean isFreeSpin;
}
