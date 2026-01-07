package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.FinancialService;
import com.earn.earnmoney.model.Withdraw;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/financial")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFinancialController {

    private final FinancialService financialService;

    public AdminFinancialController(FinancialService financialService) {
        this.financialService = financialService;
    }

    @GetMapping("/withdrawals")
    public ResponseEntity<Map<String, Object>> getWithdrawals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "PENDING") String status) {

        Page<Withdraw> withdrawals = financialService.getAllWithdrawals(page, size, status);

        Map<String, Object> response = new HashMap<>();
        response.put("withdrawals", withdrawals.getContent());
        response.put("currentPage", withdrawals.getNumber());
        response.put("totalItems", withdrawals.getTotalElements());
        response.put("totalPages", withdrawals.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdrawals/{id}/approve")
    public ResponseEntity<Map<String, String>> approveWithdraw(@PathVariable Long id) {
        String result = financialService.approveWithdrawal(id, "admin");

        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdrawals/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectWithdraw(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        String reason = (body != null && body.containsKey("reason")) ? body.get("reason") : "تم الرفض من قبل الادارة";
        String result = financialService.rejectWithdrawal(id, reason, "admin");

        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }
}
