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
public class StoreService {

    private final CardProductRepo cardProductRepo;
    private final CardPurchaseRepo cardPurchaseRepo;
    private final UserAuthRepo userRepo;
    private final LogTransactionRepo logRepo;
    private final ImageRepo imageRepo;
    private final OrderReportRepo reportRepo;

    public StoreService(CardProductRepo cardProductRepo,
            CardPurchaseRepo cardPurchaseRepo,
            UserAuthRepo userRepo,
            LogTransactionRepo logRepo,
            ImageRepo imageRepo,
            OrderReportRepo reportRepo) {
        this.cardProductRepo = cardProductRepo;
        this.cardPurchaseRepo = cardPurchaseRepo;
        this.userRepo = userRepo;
        this.logRepo = logRepo;
        this.imageRepo = imageRepo;
        this.reportRepo = reportRepo;
    }

    // Get only APPROVED cards for marketplace
    public List<CardProduct> getAllAvailableCards() {
        return cardProductRepo.findByApprovalStatusAndAvailableTrue(CardProduct.ApprovalStatus.APPROVED)
                .stream()
                .filter(card -> card.getTotalQuantity() > card.getSoldQuantity())
                .peek(p -> {
                    if (p.getSellerId() != null) {
                        userRepo.findById(p.getSellerId()).ifPresent(user -> {
                            p.setSellerName(user.getFull_name() != null ? user.getFull_name() : user.getUsername());
                        });
                    }
                })
                .collect(Collectors.toList());
    }

    public List<CardProduct> getAllCards() {
        return cardProductRepo.findAll().stream()
                .peek(p -> {
                    if (p.getSellerId() != null) {
                        userRepo.findById(p.getSellerId()).ifPresent(user -> {
                            p.setSellerName(user.getFull_name() != null ? user.getFull_name() : user.getUsername());
                            p.setSellerEmail(user.getUsername()); // Username acts as email/identifier often
                            // contactPhone is already on CardProduct if saved
                        });
                    }
                })
                .collect(Collectors.toList());
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
            // ESCROW: Status is PENDING_APPROVAL. Seller must accept first.
            purchase.setStatus(CardPurchase.PurchaseStatus.PENDING_APPROVAL);
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

        // 5. Notify Seller about new order
        if (card.getSellerId() != null) {
            userRepo.findById(card.getSellerId()).ifPresent(seller -> {
                LogTransaction sellerLog = new LogTransaction();
                sellerLog.setUserId(seller.getId());
                sellerLog.setUsername(seller.getUsername());
                sellerLog.setFullName(seller.getFull_name());
                sellerLog.setTransactionDate(LocalDateTime.now());
                sellerLog.setType("NEW_ORDER");
                sellerLog.setDescription(
                        "طلب جديد على منتجك: " + card.getName() + " من المشتري: " + user.getFull_name());
                sellerLog.setPreviousBalance((double) seller.getPoints());
                sellerLog.setNewBalance((double) seller.getPoints());
                logRepo.save(sellerLog);
            });
        }
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
        purchase.setFundsReleased(true);
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
            String contactPhone, String typeStr,
            MultipartFile file) throws IOException {

        // Validate contact phone
        if (contactPhone == null || contactPhone.trim().isEmpty()) {
            throw new RuntimeException("رقم الهاتف للتواصل مطلوب");
        }

        CardProduct card = new CardProduct();
        card.setName(name);
        card.setPrice(price);
        card.setCategory(category);
        card.setAvailable(true);
        card.setSellerId(user.getId());
        card.setTotalQuantity(quantity);
        card.setSoldQuantity(0);
        card.setContactPhone(contactPhone.trim());
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
            try {
                if (p.getCardProduct() != null) {
                    p.setCardName(p.getCardProduct().getName());
                    p.setCardPrice(p.getCardProduct().getPrice());
                }

                if (p.getUser() != null) {
                    p.setUsername(p.getUser().getUsername());
                    p.setUserFullName(p.getUser().getFull_name());
                }
            } catch (Exception e) {
                // Determine what to do, for now safe ignore to show list
            }
        });

        return orders;
    }

    // Seller updates their own listing
    @Transactional
    public CardProduct updateUserListing(UserAuth user, Long cardId, String name, Long price,
            String category, Integer quantity, String contactPhone, MultipartFile file) throws IOException {
        if (cardId == null) {
            throw new IllegalArgumentException("cardId cannot be null");
        }

        // Validate all required fields
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("اسم المنتج مطلوب");
        }
        if (price == null || price < 0) {
            throw new RuntimeException("سعر المنتج مطلوب ويجب أن يكون موجباً");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new RuntimeException("فئة المنتج مطلوبة");
        }
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("الكمية مطلوبة ويجب أن تكون أكبر من صفر");
        }
        if (contactPhone == null || contactPhone.trim().isEmpty()) {
            throw new RuntimeException("رقم الهاتف للتواصل مطلوب");
        }
        // Image is optional for updates - user can keep existing image

        CardProduct card = cardProductRepo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("المنتج غير موجود"));

        if (!card.getSellerId().equals(user.getId())) {
            throw new RuntimeException("لا تملك صلاحية تعديل هذا المنتج");
        }

        // Cannot edit approved products that have been sold
        if (card.getSoldQuantity() > 0) {
            // Only allow quantity increase
            if (quantity < card.getTotalQuantity()) {
                throw new RuntimeException("لا يمكن تقليل الكمية لمنتج تم بيع جزء منه");
            }
        }

        // Update all fields (all are required now)
        card.setName(name.trim());
        card.setPrice(price);
        card.setCategory(category.trim());
        card.setTotalQuantity(quantity);
        card.setContactPhone(contactPhone.trim());

        // Update image only if provided
        if (file != null && !file.isEmpty()) {
            Image image = card.getImage();
            if (image == null) {
                image = new Image();
            }
            image.setName(file.getOriginalFilename());
            image.setType(file.getContentType());
            image.setImage(ImageUtilities.compressImage(file.getBytes()));
            card.setImage(image);
        }

        // Reset approval status to PENDING for any edit to require admin re-approval
        card.setApprovalStatus(CardProduct.ApprovalStatus.PENDING);
        card.setRejectionReason(null);

        return cardProductRepo.save(card);
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
                            || p.getStatus() == CardPurchase.PurchaseStatus.ON_DELIVERY)
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
        product.setAvailable(true); // Make sure it's available when approved
        product.setRejectionReason(null); // Clear any previous rejection reason
        cardProductRepo.save(product);

        // Notify seller about approval
        if (product.getSellerId() != null) {
            userRepo.findById(product.getSellerId()).ifPresent(seller -> {
                LogTransaction log = new LogTransaction();
                log.setUserId(seller.getId());
                log.setUsername(seller.getUsername());
                log.setFullName(seller.getFull_name());
                log.setTransactionDate(LocalDateTime.now());
                log.setType("PRODUCT_APPROVED");
                log.setDescription("تمت الموافقة على منتجك: " + product.getName());
                log.setPreviousBalance((double) seller.getPoints());
                log.setNewBalance((double) seller.getPoints());
                logRepo.save(log);
            });
        }
    }

    // Admin: Reject Product
    public void rejectProduct(Long productId, String reason) {
        CardProduct product = cardProductRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.setApprovalStatus(CardProduct.ApprovalStatus.REJECTED);
        product.setRejectionReason(reason);
        cardProductRepo.save(product);

        // Notify seller about rejection
        if (product.getSellerId() != null) {
            userRepo.findById(product.getSellerId()).ifPresent(seller -> {
                LogTransaction log = new LogTransaction();
                log.setUserId(seller.getId());
                log.setUsername(seller.getUsername());
                log.setFullName(seller.getFull_name());
                log.setTransactionDate(LocalDateTime.now());
                log.setType("PRODUCT_REJECTED");
                log.setDescription("تم رفض منتجك: " + product.getName() + " - السبب: " + reason);
                log.setPreviousBalance((double) seller.getPoints());
                log.setNewBalance((double) seller.getPoints());
                logRepo.save(log);
            });
        }
    }

    // Admin: Get Orders Waiting for Delivery Confirmation (to release points)
    public List<CardPurchase> getWaitingOrders() {
        return cardPurchaseRepo.findAll().stream()
                .filter(p -> {
                    // Physical products waiting for delivery confirmation
                    if (p.getStatus() == CardPurchase.PurchaseStatus.ON_DELIVERY) {
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

        // إشعار المشتري بأن الطلب تم شحنه
        UserAuth buyer = purchase.getUser();
        if (buyer != null) {
            LogTransaction log = new LogTransaction();
            log.setUserId(buyer.getId());
            log.setUsername(buyer.getUsername());
            log.setFullName(buyer.getFull_name());
            log.setTransactionDate(LocalDateTime.now());
            log.setType("ORDER_SHIPPED");
            log.setDescription("تم شحن طلبك: " + purchase.getCardProduct().getName());
            log.setPreviousBalance((double) buyer.getPoints());
            log.setNewBalance((double) buyer.getPoints());
            logRepo.save(log);
        }
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

    // Seller rejects an order (Refunds buyer)
    @Transactional
    public void sellerRejectOrder(UserAuth user, Long purchaseId, String reason) {
        CardPurchase purchase = cardPurchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (!purchase.getCardProduct().getSellerId().equals(user.getId())) {
            throw new RuntimeException("لا تملك صلاحية تعديل هذا الطلب");
        }

        CardPurchase.PurchaseStatus current = purchase.getStatus();
        // Allow rejection at any active stage before completion
        if (current == CardPurchase.PurchaseStatus.PENDING_APPROVAL ||
                current == CardPurchase.PurchaseStatus.PROCESSING ||
                current == CardPurchase.PurchaseStatus.ON_DELIVERY) {

            // Reuse reject logic (refunds points)
            rejectPurchase(purchaseId, "Rejected by Seller: " + reason);
        } else {
            throw new RuntimeException("لا يمكن إلغاء الطلب في حالته الحالية: " + current);
        }
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
            try {
                if (p.getCardProduct() != null) {
                    p.setCardName(p.getCardProduct().getName());
                    p.setCardPrice(p.getCardProduct().getPrice());
                }
            } catch (Exception e) {
                // Ignore transient errors
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
        card.setAvailable(true);
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
    public List<CardProduct> getAllProductsAdmin(String search) {
        return cardProductRepo.findAll().stream()
                .filter(p -> p.isAvailable()) // Only show available products (filter soft-deleted)
                .filter(p -> {
                    if (search == null || search.trim().isEmpty())
                        return true;
                    return p.getName().toLowerCase().contains(search.toLowerCase());
                })
                .peek(p -> {
                    if (p.getSellerId() != null) {
                        userRepo.findById(p.getSellerId()).ifPresent(user -> {
                            p.setSellerName(user.getFull_name() != null ? user.getFull_name() : user.getUsername());
                        });
                    }
                })
                .collect(Collectors.toList());
    }

    // Seller updates order status
    @Transactional
    public void updateOrderStatus(UserAuth user, Long purchaseId, CardPurchase.PurchaseStatus newStatus) {
        CardPurchase purchase = cardPurchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (!purchase.getCardProduct().getSellerId().equals(user.getId())) {
            throw new RuntimeException("لا تملك صلاحية تعديل هذا الطلب");
        }

        // Validate transitions
        CardPurchase.PurchaseStatus current = purchase.getStatus();

        // 1. Pending Approval -> Processing
        if (current == CardPurchase.PurchaseStatus.PENDING_APPROVAL
                && newStatus == CardPurchase.PurchaseStatus.PROCESSING) {
            purchase.setStatus(newStatus);
        }
        // 2. Processing -> On Delivery
        else if (current == CardPurchase.PurchaseStatus.PROCESSING
                && newStatus == CardPurchase.PurchaseStatus.ON_DELIVERY) {
            purchase.setStatus(newStatus);
        }
        // 3. On Delivery -> Delivered
        else if (current == CardPurchase.PurchaseStatus.ON_DELIVERY
                && newStatus == CardPurchase.PurchaseStatus.DELIVERED) {
            purchase.setStatus(newStatus);
        } else {
            throw new RuntimeException("تغيير الحالة غير مسموح به من " + current + " إلى " + newStatus);
        }

        cardPurchaseRepo.save(purchase);
    }

    // Buyer files a report
    @Transactional
    public void fileReport(UserAuth user, Long purchaseId, String reason, String description) {
        CardPurchase purchase = cardPurchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("هذا الطلب لا يخصك");
        }

        if (purchase.getStatus() == CardPurchase.PurchaseStatus.COMPLETED || purchase.isFundsReleased()) {
            throw new RuntimeException("لا يمكن الإبلاغ عن طلب مكتمل وتم صرف نقاطه");
        }

        OrderReport report = new OrderReport();
        report.setOrder(purchase);
        report.setReporter(user);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus(OrderReport.ReportStatus.PENDING);
        reportRepo.save(report);

        // Calculate Purchase Status to REPORTED to freeze flows
        purchase.setStatus(CardPurchase.PurchaseStatus.REPORTED);
        cardPurchaseRepo.save(purchase);
    }

    // Admin resolves a report
    @Transactional
    public void resolveReport(Long reportId, String decision, String adminComment) {
        OrderReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("البلاغ غير موجود"));

        CardPurchase purchase = report.getOrder();

        if ("RELEASE_FUNDS".equalsIgnoreCase(decision)) {
            // Admin sides with Seller -> Release Funds
            adminReleasePoints(purchase.getId()); // Re-use existing logic

            report.setStatus(OrderReport.ReportStatus.RESOLVED_RELEASE);
            report.setAdminComment(adminComment);
            reportRepo.save(report);

            // إشعار البائع والمشتري بقرار الإدارة
            UserAuth buyer = purchase.getUser();
            UserAuth seller = purchase.getCardProduct().getSellerId() != null
                    ? userRepo.findById(purchase.getCardProduct().getSellerId()).orElse(null)
                    : null;

            if (buyer != null) {
                LogTransaction buyerLog = new LogTransaction();
                buyerLog.setUserId(buyer.getId());
                buyerLog.setUsername(buyer.getUsername());
                buyerLog.setFullName(buyer.getFull_name());
                buyerLog.setTransactionDate(LocalDateTime.now());
                buyerLog.setType("DISPUTE_RESOLVED");
                buyerLog.setDescription("تم حل البلاغ - تحرير الأموال للبائع. ملاحظة الإدارة: " + adminComment);
                buyerLog.setPreviousBalance((double) buyer.getPoints());
                buyerLog.setNewBalance((double) buyer.getPoints());
                logRepo.save(buyerLog);
            }

            if (seller != null) {
                LogTransaction sellerLog = new LogTransaction();
                sellerLog.setUserId(seller.getId());
                sellerLog.setUsername(seller.getUsername());
                sellerLog.setFullName(seller.getFull_name());
                sellerLog.setTransactionDate(LocalDateTime.now());
                sellerLog.setType("DISPUTE_RESOLVED");
                sellerLog.setDescription("تم حل البلاغ - تحرير الأموال لك. ملاحظة الإدارة: " + adminComment);
                sellerLog.setPreviousBalance((double) seller.getPoints());
                sellerLog.setNewBalance((double) seller.getPoints());
                logRepo.save(sellerLog);
            }

        } else if ("REFUND_BUYER".equalsIgnoreCase(decision)) {
            // Admin sides with Buyer -> Refund
            rejectPurchase(purchase.getId(), "Report Resolved: " + adminComment); // Re-use logic

            // RejectPurchase sets status to REJECTED/CANCELLED.
            // We should ensure status is CANCELLED for semantics if needed, but REJECTED is
            // fine.

            report.setStatus(OrderReport.ReportStatus.RESOLVED_REFUND);
            report.setAdminComment(adminComment);
            reportRepo.save(report);

            // إشعار البائع والمشتري بقرار الإدارة
            UserAuth buyer = purchase.getUser();
            UserAuth seller = purchase.getCardProduct().getSellerId() != null
                    ? userRepo.findById(purchase.getCardProduct().getSellerId()).orElse(null)
                    : null;

            if (buyer != null) {
                LogTransaction buyerLog = new LogTransaction();
                buyerLog.setUserId(buyer.getId());
                buyerLog.setUsername(buyer.getUsername());
                buyerLog.setFullName(buyer.getFull_name());
                buyerLog.setTransactionDate(LocalDateTime.now());
                buyerLog.setType("DISPUTE_RESOLVED");
                buyerLog.setDescription("تم حل البلاغ - إعادة النقاط لك. ملاحظة الإدارة: " + adminComment);
                buyerLog.setPreviousBalance((double) buyer.getPoints());
                buyerLog.setNewBalance((double) buyer.getPoints());
                logRepo.save(buyerLog);
            }

            if (seller != null) {
                LogTransaction sellerLog = new LogTransaction();
                sellerLog.setUserId(seller.getId());
                sellerLog.setUsername(seller.getUsername());
                sellerLog.setFullName(seller.getFull_name());
                sellerLog.setTransactionDate(LocalDateTime.now());
                sellerLog.setType("DISPUTE_RESOLVED");
                sellerLog.setDescription("تم حل البلاغ - إعادة النقاط للمشتري. ملاحظة الإدارة: " + adminComment);
                sellerLog.setPreviousBalance((double) seller.getPoints());
                sellerLog.setNewBalance((double) seller.getPoints());
                logRepo.save(sellerLog);
            }

        } else {
            throw new IllegalArgumentException("قرار غير معروف: " + decision);
        }
    }

    // Admin: Get Reports
    @Transactional(readOnly = true)
    public List<OrderReport> getReports(String status) {
        List<OrderReport> reports;
        if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            try {
                reports = reportRepo.findByStatus(OrderReport.ReportStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                reports = List.of();
            }
        } else {
            reports = reportRepo.findAll();
        }

        // Populate transient fields
        reports.forEach(report -> {
            try {
                // Buyer Details
                if (report.getOrder() != null && report.getOrder().getUser() != null) {
                    report.setBuyerName(report.getOrder().getUser().getFull_name());
                    // UserAuth doesn't have email field, using username as fallback
                    report.setBuyerEmail(report.getOrder().getUser().getUsername());
                }

                // Seller & Product Details
                if (report.getOrder() != null && report.getOrder().getCardProduct() != null) {
                    CardProduct card = report.getOrder().getCardProduct();
                    // Populate transient product details on the order
                    report.getOrder().setCardName(card.getName());
                    report.getOrder().setCardPrice(card.getPrice());

                    Long sellerId = card.getSellerId();
                    if (sellerId != null) {
                        userRepo.findById(sellerId).ifPresent(seller -> {
                            report.setSellerName(seller.getFull_name());
                            report.setSellerEmail(seller.getUsername());
                        });
                    }
                }
            } catch (Exception e) {
                // Log error but don't fail, just leave details null
                e.printStackTrace();
            }
        });

        return reports;
    }
}
