package com.earn.earnmoney.Service;

import com.earn.earnmoney.model.*;
import com.earn.earnmoney.repo.*;
import com.earn.earnmoney.util.ImageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final CardProductRepo cardProductRepo;
    private final CardPurchaseRepo cardPurchaseRepo;
    private final UserAuthRepo userRepo;
    private final LogTransactionRepo logRepo;
    private final ImageRepo imageRepo;

    // Get only APPROVED cards for marketplace
    public List<CardProduct> getAllAvailableCards() {
        return cardProductRepo.findByApprovalStatusAndAvailableTrue(CardProduct.ApprovalStatus.APPROVED)
                .stream()
                .filter(card -> card.getTotalQuantity() > card.getSoldQuantity())
                .collect(Collectors.toList());
    }

    public List<CardProduct> getAllCards() {
        return cardProductRepo.findAll();
    }

    public CardProduct findCardById(Long id) {
        if (id == null)
            return null;
        return cardProductRepo.findById(id).orElse(null);
    }

    @Transactional
    public void buyCard(UserAuth user, Long cardId, String phone, String address, String notes) {
        if (cardId == null)
            throw new RuntimeException("معرف البطاقة غير صالح");
        CardProduct card = cardProductRepo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("البطاقة غير موجودة"));

        if (!card.isAvailableForPurchase()) {
            throw new RuntimeException("البطاقة غير متاحة حالياً");
        }

        if (user.getPoints() < card.getPrice()) {
            throw new RuntimeException("رصيد النقاط غير كافي");
        }

        // Validate physical product requirements
        if (card.getProductType() == CardProduct.ProductType.PHYSICAL) {
            if (phone == null || phone.isEmpty() || address == null || address.isEmpty()) {
                throw new RuntimeException("يجب إدخال رقم الهاتف والعنوان للمنتجات الملموسة");
            }
        }

        // Check if I am buying my own product
        boolean isSelfPurchase = card.getSellerId() != null && card.getSellerId().equals(user.getId());

        // Calculate tax (3%) and seller amount (97%)
        long totalPrice = card.getPrice();
        double taxRate = 0.03;
        long taxAmount = Math.round(totalPrice * taxRate);
        long sellerAmount = totalPrice - taxAmount;

        // 1. Deduct points from buyer
        user.setPoints(user.getPoints() - totalPrice);
        userRepo.save(user);

        // 2. Create Purchase Record
        CardPurchase purchase = new CardPurchase();
        purchase.setUser(user);
        purchase.setCardProduct(card);
        purchase.setPurchaseDate(LocalDateTime.now());

        // Save Delivery Details
        purchase.setBuyerPhone(phone);
        purchase.setBuyerAddress(address);
        purchase.setBuyerNotes(notes);

        if (card.getProductType() == CardProduct.ProductType.PHYSICAL) {
            // ESCROW: Status is WAITING_DELIVERY. Seller gets NOTHING yet.
            purchase.setStatus(CardPurchase.PurchaseStatus.WAITING_DELIVERY);
            purchase.setCardCode("PENDING_DELIVERY"); // Placeholder
        } else {
            // DIGITAL: Hold funds until admin releases (same as physical)
            // Seller must deliver code, then admin approves and releases payment
            purchase.setStatus(CardPurchase.PurchaseStatus.PENDING);
            // Note: No immediate credit to seller - will be released by admin after code
            // delivery
        }

        cardPurchaseRepo.save(purchase);

        // 3. Increment sold quantity
        card.setSoldQuantity(card.getSoldQuantity() + 1);
        cardProductRepo.save(card);

        // 4. Log Buyer Transaction
        LogTransaction log = new LogTransaction();
        log.setUserId(user.getId());
        log.setFullName(user.getFull_name());
        log.setUsername(user.getUsername());
        log.setType("BUY_CARD");
        log.setDescription("شراء: " + card.getName());
        log.setPreviousBalance((double) (user.getPoints() + totalPrice));
        log.setNewBalance((double) user.getPoints());
        log.setTransactionDate(LocalDateTime.now());
        logRepo.save(log);
    }

    // Admin releases points for Physical products AFTER delivery confirmation
    @Transactional
    public void adminReleasePoints(Long purchaseId) {
        if (purchaseId == null) {
            throw new IllegalArgumentException("purchaseId cannot be null");
        }
        CardPurchase purchase = cardPurchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (purchase.getStatus() == CardPurchase.PurchaseStatus.COMPLETED) {
            throw new RuntimeException("تم تحويل النقاط مسبقاً");
        }

        CardProduct card = purchase.getCardProduct();
        Long sellerId = card.getSellerId();
        if (sellerId == null) {
            throw new RuntimeException("هذا المنتج لا يتبع لبائع (منتج إداري)");
        }

        UserAuth seller = userRepo.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("البائع غير موجود"));

        // Calculate Amount (97%)
        long totalPrice = card.getPrice();
        double taxRate = 0.03;
        long taxAmount = Math.round(totalPrice * taxRate);
        long sellerAmount = totalPrice - taxAmount;

        // Credit Seller
        seller.setPoints(seller.getPoints() + sellerAmount);
        userRepo.save(seller);

        // Update Purchase Status
        purchase.setStatus(CardPurchase.PurchaseStatus.COMPLETED); // Money released
        cardPurchaseRepo.save(purchase);

        // Log Transaction
        LogTransaction sellerLog = new LogTransaction();
        sellerLog.setUserId(seller.getId());
        sellerLog.setFullName(seller.getFull_name());
        sellerLog.setUsername(seller.getUsername());
        sellerLog.setType("MARKETPLACE_SALE_RELEASE"); // New type
        sellerLog.setDescription("إيداع أرباح بيع (ملموس): " + card.getName());
        sellerLog.setPreviousBalance((double) (seller.getPoints() - sellerAmount));
        sellerLog.setNewBalance((double) seller.getPoints());
        sellerLog.setTransactionDate(LocalDateTime.now());
        logRepo.save(sellerLog);
    }

    // User adds new listing (marketplace)
    @Transactional
    public CardProduct addUserListing(UserAuth user, String name, Long price, String category, Integer quantity,
            String typeStr,
            MultipartFile file) throws IOException {
        CardProduct card = new CardProduct();
        card.setName(name);
        card.setPrice(price);
        card.setCategory(category);
        card.setAvailable(true);
        card.setSellerId(user.getId());
        card.setTotalQuantity(quantity);
        card.setSoldQuantity(0);
        card.setTaxRate(0.03); // 3% tax
        card.setApprovalStatus(CardProduct.ApprovalStatus.PENDING);

        // Parse Type
        try {
            if (typeStr != null) {
                card.setProductType(CardProduct.ProductType.valueOf(typeStr.toUpperCase()));
            } else {
                card.setProductType(CardProduct.ProductType.DIGITAL);
            }
        } catch (Exception e) {
            card.setProductType(CardProduct.ProductType.DIGITAL);
        }

        if (file != null && !file.isEmpty()) {
            Image image = new Image();
            image.setName(file.getOriginalFilename());
            image.setType(file.getContentType());
            image.setImage(ImageUtilities.compressImage(file.getBytes()));
            card.setImage(image);
        }

        return cardProductRepo.save(card);
    }

    // Get Orders for a Seller
    @Transactional(readOnly = true)
    public List<CardPurchase> getSellerOrders(Long sellerId) {
        // Find purchases where the product's sellerId == sellerId
        // This requires a custom query in Repo or filtering
        List<CardPurchase> orders = cardPurchaseRepo.findByCardProductSellerIdOrderByPurchaseDateDesc(sellerId);

        // Populate transient fields for frontend
        orders.forEach(p -> {
            if (p.getCardProduct() != null) {
                p.setCardName(p.getCardProduct().getName());
                p.setCardPrice(p.getCardProduct().getPrice());
            }
        });

        return orders;
    }

    // Seller deletes their own listing
    @Transactional
    public void deleteUserListing(UserAuth user, Long cardId) {
        if (cardId == null) {
            throw new IllegalArgumentException("cardId cannot be null");
        }
        CardProduct card = cardProductRepo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("المنتج غير موجود"));

        if (!card.getSellerId().equals(user.getId())) {
            throw new RuntimeException("لا تملك صلاحية حذف هذا المنتج");
        }

        // Check for sales to prevent data inconsistency
        if (card.getSoldQuantity() > 0) {
            // Option: Soft delete (hide from marketplace) and maybe hide from user's list?
            // For now, let's just mark as unavailable to prevent further sales.
            // If user really wants to 'delete', we might need an 'isDeleted' flag.
            // Let's assume standard behavior: Cannot delete active history.
            // But to satisfy "Delete", we could set available = false and maybe move to
            // "Archived"?
            // Let's throw for now to see.
            throw new RuntimeException("لا يمكن حذف منتج تم بيع جزء منه. يمكنك تعديل الكمية لإيقاف البيع.");
        }

        cardProductRepo.delete(card);
    }

    @Transactional
    public CardProduct addCard(String name, Long price, String category, MultipartFile file) throws IOException {
        CardProduct card = new CardProduct();
        card.setName(name);
        card.setPrice(price);
        card.setCategory(category);
        card.setAvailable(true);
        card.setApprovalStatus(CardProduct.ApprovalStatus.APPROVED); // Admin cards auto-approved

        // Admin cards default to Digital usually?
        card.setProductType(CardProduct.ProductType.DIGITAL);

        if (file != null && !file.isEmpty()) {
            Image image = new Image();
            image.setName(file.getOriginalFilename());
            image.setType(file.getContentType());
            image.setImage(ImageUtilities.compressImage(file.getBytes()));
            card.setImage(image);
        }

        return cardProductRepo.save(card);
    }

    // Admin: Get All Purchases (changed from pending only for debugging)
    @Transactional(readOnly = true)
    public List<CardPurchase> getPendingPurchases() {
        try {
            List<CardPurchase> allPurchases = cardPurchaseRepo.findAll();

            return allPurchases.stream()
                    // Filter: Pending OR Waiting Delivery
                    .filter(p -> p.getStatus() == CardPurchase.PurchaseStatus.PENDING
                            || p.getStatus() == CardPurchase.PurchaseStatus.WAITING_DELIVERY)
                    .peek(p -> {
                        // Populate transient fields for JSON
                        try {
                            if (p.getUser() != null) {
                                p.setUsername(p.getUser().getUsername());
                                p.setUserFullName(p.getUser().getFull_name());
                                p.setReferralCode(p.getUser().getReferralCode());
                            }
                        } catch (Exception e) {
                        }

                        if (p.getCardProduct() != null) {
                            try {
                                p.setCardName(p.getCardProduct().getName());
                                p.setCardPrice(p.getCardProduct().getPrice());
                            } catch (Exception e) {
                            }
                        }
                    })
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("فشل تحميل طلبات الشراء: " + e.getMessage());
        }
    }

    // Admin: Get all Pending Products
    public List<CardProduct> getPendingProducts() {
        return cardProductRepo.findAll().stream()
                .filter(p -> p.getApprovalStatus() == CardProduct.ApprovalStatus.PENDING)
                .peek(p -> {
                    if (p.getSellerId() != null) {
                        userRepo.findById(p.getSellerId()).ifPresent(user -> {
                            p.setSellerName(user.getFull_name() != null ? user.getFull_name() : user.getUsername());
                        });
                    }
                })
                .collect(Collectors.toList());
        // A custom query findByApprovalStatus would be better but this works for now
    }

    // Admin: Approve Product
    @Transactional
    public void approveProduct(Long productId) {
        CardProduct product = cardProductRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.setApprovalStatus(CardProduct.ApprovalStatus.APPROVED);
        cardProductRepo.save(product);
    }

    // Admin: Reject Product
    public void rejectProduct(Long productId, String reason) {
        CardProduct product = cardProductRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.setApprovalStatus(CardProduct.ApprovalStatus.REJECTED);
        product.setRejectionReason(reason);
        cardProductRepo.save(product);
    }

    // Admin: Get Orders Waiting for Delivery Confirmation (to release points)
    public List<CardPurchase> getWaitingOrders() {
        return cardPurchaseRepo.findAll().stream()
                .filter(p -> {
                    // Physical products waiting for delivery confirmation
                    if (p.getStatus() == CardPurchase.PurchaseStatus.WAITING_DELIVERY) {
                        return true;
                    }
                    // Digital products that have been delivered but not completed (admin needs to
                    // release funds)
                    if (p.getStatus() == CardPurchase.PurchaseStatus.DELIVERED) {
                        // Only include if it has a seller (not admin products)
                        CardProduct card = p.getCardProduct();
                        if (card != null && card.getSellerId() != null) {
                            return true;
                        }
                    }
                    return false;
                })
                .peek(p -> {
                    // Populate transient fields for admin display
                    try {
                        // Product details
                        if (p.getCardProduct() != null) {
                            CardProduct card = p.getCardProduct();
                            p.setCardName(card.getName());
                            p.setCardPrice(card.getPrice());

                            // Seller name
                            if (card.getSellerId() != null) {
                                userRepo.findById(card.getSellerId()).ifPresent(seller -> {
                                    p.setSellerName(seller.getFull_name() != null ? seller.getFull_name()
                                            : seller.getUsername());
                                });
                            }
                        }

                        // Buyer details
                        if (p.getUser() != null) {
                            p.setUsername(p.getUser().getUsername());
                            p.setUserFullName(p.getUser().getFull_name());
                            p.setReferralCode(p.getUser().getReferralCode());
                        }
                    } catch (Exception e) {
                        // Ignore errors in populating transient fields
                    }
                })
                .collect(Collectors.toList());
    }

    // Seller delivers order (sends code/tracking)
    @Transactional
    public void deliverOrder(UserAuth user, Long purchaseId, String code) {
        CardPurchase purchase = cardPurchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (!purchase.getCardProduct().getSellerId().equals(user.getId())) {
            throw new RuntimeException("لا تملك صلاحية تعديل هذا الطلب");
        }

        purchase.setStatus(CardPurchase.PurchaseStatus.DELIVERED);
        purchase.setCardCode(code);
        cardPurchaseRepo.save(purchase);
    }

    // Admin: Complete Purchase (Send Code)
    @Transactional
    public void completePurchase(Long purchaseId, String code) {
        CardPurchase purchase = cardPurchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        purchase.setStatus(CardPurchase.PurchaseStatus.DELIVERED);
        purchase.setCardCode(code);
        cardPurchaseRepo.save(purchase);
    }

    // Admin: Reject Purchase
    @Transactional
    public void rejectPurchase(Long purchaseId, String reason) {
        CardPurchase purchase = cardPurchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        purchase.setStatus(CardPurchase.PurchaseStatus.REJECTED);
        purchase.setRejectionReason(reason);

        // Return points to user
        if (purchase.getUser() != null && purchase.getCardProduct() != null) {
            UserAuth user = purchase.getUser();
            long oldPoints = user.getPoints();
            long refundAmount = purchase.getCardProduct().getPrice();
            long newPoints = oldPoints + refundAmount;

            user.setPoints(newPoints);
            userRepo.save(user);

            // Log Transaction
            LogTransaction log = new LogTransaction();
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
            log.setFullName(user.getFull_name());
            log.setTransactionDate(LocalDateTime.now());
            log.setType("REFUND"); // نوع العملية استرجاع
            log.setDescription("استرجاع نقاط: " + purchase.getCardProduct().getName() + " - سبب: " + reason);
            log.setPreviousBalance((double) oldPoints);
            log.setNewBalance((double) newPoints);
            logRepo.save(log);
        }

        cardPurchaseRepo.save(purchase);
    }

    // Delete Card
    @Transactional
    public void deleteCard(Long cardId) {
        CardProduct card = cardProductRepo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("البطاقة غير موجودة"));

        // Check if it has been sold
        if (card.getSoldQuantity() != null && card.getSoldQuantity() > 0) {
            // Soft delete: mark as unavailable instead of deleting
            card.setAvailable(false);
            card.setTotalQuantity(card.getSoldQuantity()); // Stop further sales
            cardProductRepo.save(card);
        } else {
            // Hard delete: no sales history
            cardProductRepo.delete(card);
        }
    }

    // Find image by ID
    public Image findImageById(Long imageId) {
        if (imageId == null)
            throw new IllegalArgumentException("imageId cannot be null");
        return imageRepo.findById(imageId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CardPurchase> getMyPurchases(UserAuth user) {
        List<CardPurchase> purchases = cardPurchaseRepo.findByUserOrderByPurchaseDateDesc(user);
        // Populate transient fields for frontend
        purchases.forEach(p -> {
            if (p.getCardProduct() != null) {
                p.setCardName(p.getCardProduct().getName());
                p.setCardPrice(p.getCardProduct().getPrice());
            }
        });
        return purchases;
    }

    // Get user's own listings
    @Transactional(readOnly = true)
    public List<CardProduct> getUserListings(UserAuth user) {
        return cardProductRepo.findBySellerIdOrderByIdDesc(user.getId());
    }

    // Get pending listings for admin
    @Transactional(readOnly = true)
    public List<CardProduct> getPendingListings() {
        return cardProductRepo.findByApprovalStatus(CardProduct.ApprovalStatus.PENDING);
    }

    // Admin approves listing
    @Transactional
    public CardProduct approveListing(Long cardId) {
        CardProduct card = cardProductRepo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("السلعة غير موجودة"));
        card.setApprovalStatus(CardProduct.ApprovalStatus.APPROVED);
        card.setRejectionReason(null);
        return cardProductRepo.save(card);
    }

    // Admin rejects listing
    @Transactional
    public CardProduct rejectListing(Long cardId, String reason) {
        CardProduct card = cardProductRepo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("السلعة غير موجودة"));
        card.setApprovalStatus(CardProduct.ApprovalStatus.REJECTED);
        card.setRejectionReason(reason);
        return cardProductRepo.save(card);
    }

    @Transactional
    public CardProduct saveCard(CardProduct card) {
        return cardProductRepo.save(card);
    }

    // Admin: Get All Products with Seller Details
    @Transactional(readOnly = true)
    public List<CardProduct> getAllProductsAdmin() {
        return cardProductRepo.findAll().stream()
                .filter(p -> p.isAvailable()) // Only show available products (filter soft-deleted)
                .peek(p -> {
                    if (p.getSellerId() != null) {
                        userRepo.findById(p.getSellerId()).ifPresent(user -> {
                            p.setSellerName(user.getFull_name() != null ? user.getFull_name() : user.getUsername());
                        });
                    }
                })
                .collect(Collectors.toList());
    }
}
