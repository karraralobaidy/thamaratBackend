package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.*;
import com.earn.earnmoney.model.Ads;
import com.earn.earnmoney.model.CardPurchase;
import com.earn.earnmoney.model.Image;
import com.earn.earnmoney.repo.CardProductRepo;
import com.earn.earnmoney.repo.CardPurchaseRepo;
import com.earn.earnmoney.repo.CounterRepo;
import com.earn.earnmoney.util.ImageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/count")
@RequiredArgsConstructor
public class CountDashboard {
    private final AdsService adsService;
    // private final PaymentService paymentService;
    private final WithdrawService withdrawService;
    // private final SubscriberService subscriberService;
    private final UserAuthService userAuthService;
    private final CounterRepo counterRepo;
    private final CardProductRepo cardProductRepo;
    private final CardPurchaseRepo cardPurchaseRepo;

    @GetMapping("/v1/count")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> adsAll() {
        try {
            Map<String, Long> count = new HashMap<>();
            long ads = adsService.getAllAds().size();
            long withdraw = withdrawService.getAllWithdrawObject().size();
            long users = userAuthService.getAllUsersWithOutPage().size();
            long counters = counterRepo.count();

            // Marketplace stats
            long pendingProducts = cardProductRepo.findAll().stream()
                    .filter(p -> "PENDING".equals(p.getApprovalStatus()))
                    .count();
            long activeProducts = cardProductRepo.findAll().stream()
                    .filter(p -> "APPROVED".equals(p.getApprovalStatus()) && p.isAvailable())
                    .count();
            long waitingOrders = cardPurchaseRepo.findAll().stream()
                    .filter(p -> {
                        if (p.getStatus() == CardPurchase.PurchaseStatus.WAITING_DELIVERY) {
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
