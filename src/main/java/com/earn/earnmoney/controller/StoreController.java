package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.StoreService;
import com.earn.earnmoney.Service.UserAuthServiceImpl;
import com.earn.earnmoney.model.CardProduct;
import com.earn.earnmoney.model.CardPurchase;
import com.earn.earnmoney.model.Image;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.security.services.UserDetailsImpl;
import com.earn.earnmoney.util.ImageUtilities;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marketplace")
public class StoreController {

    // Controller for Marketplace operations

    private final StoreService storeService;
    private final UserAuthServiceImpl userService;

    public StoreController(StoreService storeService, UserAuthServiceImpl userService) {
        this.storeService = storeService;
        this.userService = userService;
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardProduct>> getAllCards() {
        List<CardProduct> cards = storeService.getAllAvailableCards();
        // Clear binary image data to reduce response size, frontend uses /getimage/{id}
        // or imageUrl
        cards.forEach(c -> {
            if (c.getImage() != null)
                c.getImage().setImage(null);
        });
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/getimage/{cardId}")
    public ResponseEntity<byte[]> getImage(@PathVariable("cardId") Long cardId) {
        CardProduct card = storeService.findCardById(cardId);

        if (card != null && card.getImage() != null && card.getImage().getImage() != null) {
            Image image = card.getImage();
            byte[] imageData = ImageUtilities.decompressImage(image.getImage());
            String contentType = image.getType();
            MediaType mediaType = MediaType.IMAGE_JPEG;
            if (contentType != null) {
                try {
                    MediaType parsed = MediaType.parseMediaType(contentType);
                    if (parsed != null) {
                        mediaType = parsed;
                    }
                } catch (Exception e) {
                    mediaType = MediaType.IMAGE_JPEG;
                }
            }
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(imageData);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/buy/{cardId}")
    public ResponseEntity<?> buyCard(
            @PathVariable Long cardId,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String notes) {

        try {
            storeService.buyCard(getCurrentUser(), cardId, phone, address, notes);
            Map<String, String> response = new HashMap<>();
            response.put("message", "تم عملية الشراء بنجاح. يرجى متابعة حالة الطلب.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mypurchases")
    public ResponseEntity<List<CardPurchase>> getMyPurchases() {
        return ResponseEntity.ok(storeService.getMyPurchases(getCurrentUser()));
    }

    // Marketplace: Add new listing
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addListing(
            @RequestParam("name") String name,
            @RequestParam("price") Long price,
            @RequestParam("category") String category, // Description
            @RequestParam(value = "sectionName", required = false) String sectionName, // Section
            @RequestParam("quantity") Integer quantity,
            @RequestParam("contactPhone") String contactPhone,
            @RequestParam(value = "type", required = false, defaultValue = "DIGITAL") String type, // DIGITAL or
                                                                                                   // PHYSICAL
            @RequestParam(value = "image", required = false) org.springframework.web.multipart.MultipartFile image) {
        try {
            CardProduct listing = storeService.addCard(getCurrentUser(), name, price, category, sectionName, quantity,
                    contactPhone, type,
                    image);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "تم إضافة سلعتك بنجاح! في انتظار موافقة الإدارة.");
            response.put("listing", listing);
            if (listing.getImage() != null) {
                listing.getImage().setImage(null);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "فشل إضافة السلعة: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Get my listings
    @GetMapping("/my-listings")
    public ResponseEntity<List<CardProduct>> getMyListings() {
        List<CardProduct> listings = storeService.getUserListings(getCurrentUser());
        // Clear image data for performance
        listings.forEach(l -> {
            if (l.getImage() != null)
                l.getImage().setImage(null);
        });
        return ResponseEntity.ok(listings);
    }

    // Get orders coming to me (as a seller)
    @GetMapping("/seller/orders")
    public ResponseEntity<?> getSellerOrders() {
        try {
            List<CardPurchase> orders = storeService.getSellerOrders(getCurrentUser().getId());

            // Hide buyer data logic could go here if we wanted to restrict viewing until
            // some status
            // But plan says seller needs to view details to fulfill.

            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
            return ResponseEntity.status(500).body(java.util.Collections.singletonMap("error", errorMsg));
        }
    }

    // Seller delivers order (sends code/tracking)
    @PostMapping("/seller/deliver/{purchaseId}")
    public ResponseEntity<?> deliverOrder(@PathVariable Long purchaseId, @RequestParam String code) {
        try {
            storeService.deliverOrder(getCurrentUser(), purchaseId, code);
            return ResponseEntity.ok(Map.of("message", "تم تسليم الطلب بنجاح"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Seller updates order status
    @PostMapping("/seller/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            storeService.updateOrderStatus(getCurrentUser(), id,
                    CardPurchase.PurchaseStatus.valueOf(status.toUpperCase()));
            return ResponseEntity.ok(Map.of("message", "تم تحديث حالة الطلب"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "الحالة غير صالحة"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Seller rejects/returns an order
    @PostMapping("/seller/orders/{id}/reject")
    public ResponseEntity<?> rejectOrder(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Cancelled by seller") String reason) {
        try {
            storeService.sellerRejectOrder(getCurrentUser(), id, reason);
            return ResponseEntity.ok(Map.of("message", "تم إلغاء الطلب بنجاح"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Buyer reports an order
    @PostMapping("/report/{purchaseId}")
    public ResponseEntity<?> fileReport(
            @PathVariable Long purchaseId,
            @RequestParam String reason,
            @RequestParam String description) {
        try {
            storeService.fileReport(getCurrentUser(), purchaseId, reason, description);
            return ResponseEntity.ok(Map.of("message", "تم تقديم البلاغ بنجاح"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Buyer cancels order (only if PENDING_APPROVAL)
    @PostMapping("/buyer/cancel/{purchaseId}")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long purchaseId,
            @RequestParam String reason) {
        try {
            storeService.buyerCancelOrder(getCurrentUser(), purchaseId, reason);
            return ResponseEntity.ok(Map.of("message", "تم إلغاء الطلب بنجاح"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Admin: Release Points (Escrow)
    @PostMapping("/admin/release-points/{purchaseId}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<?> releasePoints(@PathVariable Long purchaseId) {
        try {
            storeService.adminReleasePoints(purchaseId);
            return ResponseEntity.ok(Map.of("message", "تم تحرير النقاط للبائع بنجاح"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Admin: Get all Pending Products
    @GetMapping("/admin/pending-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardProduct>> getPendingProducts() {
        List<CardProduct> pending = storeService.getPendingProducts();
        pending.forEach(p -> {
            if (p.getImage() != null)
                p.getImage().setImage(null);
        });
        return ResponseEntity.ok(pending);
    }

    // Admin: Approve Product
    @PostMapping("/admin/approve-product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveProduct(@PathVariable Long productId) {
        try {
            storeService.approveProduct(productId);
            return ResponseEntity.ok(Map.of("message", "Product approved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Admin: Reject Product
    @PostMapping("/admin/reject-product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectProduct(@PathVariable Long productId, @RequestParam String reason) {
        try {
            storeService.rejectProduct(productId, reason);
            return ResponseEntity.ok(Map.of("message", "Product rejected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Admin: Get Waiting Orders (Physical)
    @GetMapping("/admin/waiting-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardPurchase>> getWaitingOrders() {
        return ResponseEntity.ok(storeService.getWaitingOrders());
    }

    // Admin: Get All Products (for management)
    @GetMapping("/admin/all-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardProduct>> getAllProductsAdmin(@RequestParam(required = false) String search) {
        List<CardProduct> products = storeService.getAllProductsAdmin(search);
        products.forEach(p -> {
            if (p.getImage() != null)
                p.getImage().setImage(null);
        });
        return ResponseEntity.ok(products);
    }

    // Admin: Delete Product
    @DeleteMapping("/admin/delete-product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProductAdmin(@PathVariable Long id) {
        try {
            storeService.deleteCard(id);
            return ResponseEntity.ok(Map.of("message", "Product deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Admin: Reject Purchase (Delete Order)
    @PostMapping("/admin/store/purchases/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectPurchaseAdmin(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Admin Rejected");
        try {
            storeService.rejectPurchase(id, reason);
            return ResponseEntity.ok(Map.of("message", "Purchase rejected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Seller updates their own listing
    @PutMapping(value = "/my-listings/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMyListing(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "price", required = false) Long price,
            @RequestParam(value = "category", required = false) String category, // Description
            @RequestParam(value = "sectionName", required = false) String sectionName, // Section
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "contactPhone", required = false) String contactPhone,
            @RequestParam(value = "image", required = false) org.springframework.web.multipart.MultipartFile image) {
        try {
            CardProduct updated = storeService.updateUserListing(getCurrentUser(), id, name, price,
                    category, sectionName, quantity, contactPhone, image);
            if (updated.getImage() != null) {
                updated.getImage().setImage(null); // Clear binary data
            }
            Map<String, Object> response = new HashMap<>();
            response.put("message", "تم تحديث المنتج بنجاح");
            response.put("listing", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Seller deletes their own listing
    @DeleteMapping("/my-listings/{id}")
    public ResponseEntity<?> deleteMyListing(@PathVariable Long id) {
        try {
            storeService.deleteUserListing(getCurrentUser(), id);
            return ResponseEntity.ok(Map.of("message", "تم حذف المنتج بنجاح"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private UserAuth getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("User not authenticated");
        }
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            return userService.findById(userDetails.getId());
        } catch (ClassCastException e) {
            throw new RuntimeException("Invalid user principal");
        }
    }
}
