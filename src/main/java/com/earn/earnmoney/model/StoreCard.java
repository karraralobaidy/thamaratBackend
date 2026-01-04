package com.earn.earnmoney.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_cards")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "اسم البطاقة مطلوب")
    private String name;

    @Column(length = 1000)
    private String description;

    @Min(value = 0, message = "السعر يجب أن يكون أكبر من أو يساوي صفر")
    private Long price;

    private String category;

    private String imageUrl;

    @Min(value = 0, message = "المخزون يجب أن يكون أكبر من أو يساوي صفر")
    private Integer stock = 0;

    private boolean active = true;

    // Marketplace fields
    @Column(name = "seller_id")
    private Long sellerId; // ID of the user who listed this item

    @Column(name = "total_quantity")
    @Min(value = 1, message = "الكمية الإجمالية يجب أن تكون أكبر من صفر")
    private Integer totalQuantity = 1; // Total quantity listed

    @Column(name = "sold_quantity")
    private Integer soldQuantity = 0; // Number of items sold

    @Column(name = "tax_rate")
    private Double taxRate = 0.05; // 5% tax rate

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public enum ApprovalStatus {
        PENDING, // Waiting for admin approval
        APPROVED, // Approved and visible in marketplace
        REJECTED // Rejected by admin
    }

    // Helper method to check if item is available for purchase
    @Transient
    public boolean isAvailableForPurchase() {
        return active
                && approvalStatus == ApprovalStatus.APPROVED
                && (soldQuantity < totalQuantity);
    }

    // Helper method to get remaining quantity
    @Transient
    public Integer getRemainingQuantity() {
        return totalQuantity - soldQuantity;
    }
}
