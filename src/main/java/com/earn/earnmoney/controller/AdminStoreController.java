package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.StoreService;
import com.earn.earnmoney.model.CardProduct;
import com.earn.earnmoney.model.CardPurchase;
import com.earn.earnmoney.model.OrderReport;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/store")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStoreController {

    private final StoreService storeService;

    public AdminStoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardProduct>> getAllCards() {
        return ResponseEntity.ok(storeService.getAllCards());
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CardProduct> addCard(
            @RequestParam("name") String name,
            @RequestParam("price") Long price,
            @RequestParam("category") String category,
            @RequestParam("image") MultipartFile image) throws IOException {

        CardProduct newCard = storeService.addCard(name, price, category, image);
        // نخفي البيانات الثقيلة في الرد
        if (newCard.getImage() != null)
            newCard.getImage().setImage(null);
        return ResponseEntity.ok(newCard);
    }

    @GetMapping("/purchases")
    public ResponseEntity<List<CardPurchase>> getPendingPurchases() {
        return ResponseEntity.ok(storeService.getPendingPurchases());
    }

    @PostMapping("/purchases/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> completePurchase(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String code = body.get("code");
        storeService.completePurchase(id, code);

        Map<String, String> response = new HashMap<>();
        response.put("message", "تم إرسال الكود بنجاح");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/purchases/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectPurchase(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        storeService.rejectPurchase(id, reason);

        Map<String, String> response = new HashMap<>();
        response.put("message", "تم رفض الطلب");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        storeService.deleteCard(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "تم حذف البطاقة بنجاح");
        return ResponseEntity.ok(response);
    }

    // Marketplace Admin Endpoints
    @GetMapping("/marketplace/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardProduct>> getPendingListings() {
        List<CardProduct> pendingListings = storeService.getPendingListings();
        // Clear image data for performance
        pendingListings.forEach(l -> {
            if (l.getImage() != null)
                l.getImage().setImage(null);
        });
        return ResponseEntity.ok(pendingListings);
    }

    @PostMapping("/marketplace/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveListing(@PathVariable Long id) {
        try {
            CardProduct approved = storeService.approveListing(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "تمت الموافقة على السلعة بنجاح");
            response.put("listing", approved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/marketplace/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectListing(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.get("reason");
            CardProduct rejected = storeService.rejectListing(id, reason);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "تم رفض السلعة");
            response.put("listing", rejected);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get orders waiting for admin approval to release seller funds
    @GetMapping("/waiting-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardPurchase>> getWaitingOrders() {
        List<CardPurchase> orders = storeService.getWaitingOrders();
        // Clear heavy image data
        orders.forEach(o -> {
            if (o.getCardProduct() != null && o.getCardProduct().getImage() != null) {
                o.getCardProduct().getImage().setImage(null);
            }
        });
        return ResponseEntity.ok(orders);
    }

    // Release points to seller after delivery confirmation
    @PostMapping("/release-points/{purchaseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> releasePointsToSeller(@PathVariable Long purchaseId) {
        try {
            storeService.adminReleasePoints(purchaseId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "تم تحرير النقاط للبائع بنجاح");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Admin: Get Reports
    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderReport>> getReports(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(storeService.getReports(status));
    }

    // Admin: Resolve Report
    @PostMapping("/reports/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resolveReport(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String decision = body.get("decision");
        String comment = body.getOrDefault("comment", "");

        try {
            storeService.resolveReport(id, decision, comment);
            return ResponseEntity.ok(Map.of("message", "تم حل البلاغ بنجاح"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
