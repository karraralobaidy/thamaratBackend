package com.earn.earnmoney.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.earn.earnmoney.Service.CounterActionResponse;
import com.earn.earnmoney.Service.CounterService;
import com.earn.earnmoney.Service.UserAuthServiceImpl;
import com.earn.earnmoney.dto.CounterDTO;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.security.services.UserDetailsImpl;

@RestController
@RequestMapping("/api/counters")
public class CounterController {
    private final CounterService counterService;
    private final UserAuthServiceImpl userService;

    // Constructor for dependency injection
    public CounterController(CounterService counterService, UserAuthServiceImpl userService) {
        this.counterService = counterService;
        this.userService = userService;
    }

    // 🏪 متجر العدادات
    @GetMapping("/store")
    public List<CounterDTO> getStore() {
        return counterService.getAvailableCounters(getCurrentUser());
    }

    // 🛒 شراء عداد
    @PostMapping("/{counterId}/buy")
    public void buyCounter(@PathVariable Long counterId) {
        counterService.buyCounter(counterId, getCurrentUser());
    }

    // ▶️ تشغيل / 🔄 إعادة تشغيل العداد
    @PostMapping("/action")
    public CounterActionResponse action() {
        return counterService.handleAction(getCurrentUser());
    }

    // 📋 الحصول على تفاصيل الاشتراكات
    @GetMapping("/my-subscriptions")
    public List<com.earn.earnmoney.dto.UserCounterSubscriptionDTO> getMySubscriptions() {
        return counterService.getUserCounterSubscriptions(getCurrentUser());
    }

    private UserAuth getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userService.findById(userDetails.getId());
    }

}
