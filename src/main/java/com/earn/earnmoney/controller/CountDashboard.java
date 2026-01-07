package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.AdsService;
import com.earn.earnmoney.Service.UserAuthService;
import com.earn.earnmoney.model.CardPurchase;
import com.earn.earnmoney.model.WithdrawStatus;
import com.earn.earnmoney.repo.CardProductRepo;
import com.earn.earnmoney.repo.CardPurchaseRepo;
import com.earn.earnmoney.repo.CounterRepo;
import com.earn.earnmoney.repo.WithdrawRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/count")
@RequiredArgsConstructor
public class CountDashboard {
    private final AdsService adsService;
    private final UserAuthService userAuthService;
    private final CounterRepo counterRepo;
    private final CardProductRepo cardProductRepo;
    private final CardPurchaseRepo cardPurchaseRepo;
    private final WithdrawRepo withdrawRepo;

    @GetMapping("/v1/count")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> adsAll() {
        try {
            Map<String, Long> count = new HashMap<>();
            long ads = adsService.getAllAds().size();
            long withdraw = withdrawRepo.countByStatus(WithdrawStatus.PENDING);
            long users = userAuthService.getAllUsersWithOutPage().size();
            long counters = counterRepo.count();

            // Marketplace stats
            // Marketplace stats
            long pendingProducts = cardProductRepo.findAll().stream()
                    .filter(p -> com.earn.earnmoney.model.CardProduct.ApprovalStatus.PENDING
                            .equals(p.getApprovalStatus()))
                    .count();
            long activeProducts = cardProductRepo.findAll().stream()
                    .filter(p -> com.earn.earnmoney.model.CardProduct.ApprovalStatus.APPROVED
                            .equals(p.getApprovalStatus()) && p.isAvailable())
                    .count();
            long waitingOrders = cardPurchaseRepo.findAll().stream()
                    .filter(p -> {
                        if (p.getStatus() == CardPurchase.PurchaseStatus.ON_DELIVERY) {
                            return true;
                        }
                        if (p.getStatus() == CardPurchase.PurchaseStatus.DELIVERED) {
                            return p.getCardProduct() != null && p.getCardProduct().getSellerId() != null;
                        }
                        return false;
                    })
                    .count();

            count.put("Users", users);
            count.put("withdraw", withdraw);
            count.put("ads", ads);
            count.put("counters", counters);
            count.put("pendingProducts", pendingProducts);
            count.put("activeProducts", activeProducts);
            count.put("waitingOrders", waitingOrders);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while fetching ads count");
        }

    }

}
