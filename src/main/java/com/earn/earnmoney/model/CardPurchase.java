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

    @Column(name = "is_funds_released")
    private boolean isFundsReleased = false; // Funds held in escrow until true

    public enum PurchaseStatus {
        PENDING, // Initial state, waiting for automated process or seller acceptance
        PENDING_APPROVAL, // For Manual/Physical orders: Pending Seller Approval
        PROCESSING, // Seller is preparing the item
        ON_DELIVERY, // Item is on the way
        WAITING_DELIVERY, // Legacy: Kept for backward compatibility
        DELIVERED, // Item delivered, pending Admin confirmation/Payout
        COMPLETED, // Final state: Funds Released
        CANCELLED, // Order cancelled, funds refunded
        REPORTED, // Dispute raised by buyer
        REJECTED // Rejected by Seller or Admin (before delivery)
    }
}
