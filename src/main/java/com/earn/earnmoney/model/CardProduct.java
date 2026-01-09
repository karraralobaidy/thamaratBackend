package com.earn.earnmoney.model;

import lombok.Data;
import javax.persistence.*;

@Entity
@Data
@Table(name = "card_products")
public class CardProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long price; // سعر الشراء بالنقاط

    private String category; // فئة الرصيد (آسيا، زين...)

    // New: URL for external images (fallback if Image entity is missing)
    private String imageUrl;

    @Column(name = "contact_phone")
    private String contactPhone;

    private boolean available = true;

    // Marketplace fields
    @Column(name = "seller_id")
    private Long sellerId; // ID of the user who added this listing

    @Column(name = "total_quantity")
    private Integer totalQuantity = 1; // Total items listed

    @Column(name = "sold_quantity")
    private Integer soldQuantity = 0; // Items sold

    @Column(name = "tax_rate")
    private Double taxRate = 0.03; // Changed from 0.05 to 0.03 (3%)

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private ProductType productType = ProductType.DIGITAL; // Default to Digital

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image image;

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum ProductType {
        DIGITAL,
        PHYSICAL
    }

    // Check if available for purchase
    @Transient
    public boolean isAvailableForPurchase() {
        return available && approvalStatus == ApprovalStatus.APPROVED && (soldQuantity < totalQuantity);
    }

    @Transient
    public Integer getRemainingQuantity() {
        return totalQuantity - soldQuantity;
    }

    @Transient
    private String sellerName;

    @Transient
    private String sellerEmail;
}
