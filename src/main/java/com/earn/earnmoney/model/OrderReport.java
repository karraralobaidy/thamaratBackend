package com.earn.earnmoney.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_reports")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private CardPurchase order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "password", "roles", "otp", "otpExpiry" })
    private UserAuth reporter;

    @Column(nullable = false)
    private String reason;

    @Column(length = 1000)
    private String description;

    @Column(name = "admin_comment", length = 1000)
    private String adminComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ReportStatus {
        PENDING,
        RESOLVED_REFUND, // Admin decided to refund buyer
        RESOLVED_RELEASE, // Admin decided to release funds to seller
        REJECTED // Report rejected (maybe invalid)
    }

    @Transient
    private String sellerName;
    @Transient
    private String sellerEmail;
    @Transient
    private String buyerName;
    @Transient
    private String buyerEmail;
}
