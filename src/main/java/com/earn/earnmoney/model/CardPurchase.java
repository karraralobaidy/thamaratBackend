package com.earn.earnmoney.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_purchases")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserAuth user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "card_product_id")
    private CardProduct cardProduct;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_status")
    private PurchaseStatus status = PurchaseStatus.PENDING;

    @Column(name = "card_code")
    private String cardCode;

    // Delivery Details (for Physical Products)
    @Column(name = "buyer_phone")
    private String buyerPhone;

    @Column(name = "buyer_address")
    private String buyerAddress;

    @Column(name = "buyer_notes")
    private String buyerNotes;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    // For JSON serialization
    @Transient
    private String username;

    @Transient
    private String userFullName;

    @Transient
    private String cardName;

    @Transient
    private Long cardPrice;

    @Transient
    private String referralCode;

    @Transient
    private String sellerName;

    @PrePersist
    protected void onCreate() {
        purchaseDate = LocalDateTime.now();
    }

    public enum PurchaseStatus {
        PENDING, // Waiting for processing (Digital)
        WAITING_DELIVERY, // Waiting for seller to deliver (Physical)
        APPROVED, // Unused?
        REJECTED,
        DELIVERED, // Completed
        COMPLETED // Funds Released (for Physical)
    }
}
