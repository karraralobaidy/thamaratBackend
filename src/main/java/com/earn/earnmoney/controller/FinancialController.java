package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.FinancialService;
import com.earn.earnmoney.Service.UserAuthServiceImpl;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.payload.request.TransferRequest;
import com.earn.earnmoney.payload.request.WithdrawRequest;
import com.earn.earnmoney.security.services.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/financial")
public class FinancialController {

    private final FinancialService financialService;
    private final UserAuthServiceImpl userService;

    public FinancialController(FinancialService financialService, UserAuthServiceImpl userService) {
        this.financialService = financialService;
        this.userService = userService;
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> requestWithdraw(@RequestBody WithdrawRequest request) {
        financialService.requestWithdraw(getCurrentUser(), request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "تم استلام طلب السحب بنجاح");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferPoints(@RequestBody TransferRequest request) {
        financialService.transferPoints(getCurrentUser(), request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "تم تحويل النقاط بنجاح");
        return ResponseEntity.ok(response);
    }

    private UserAuth getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userService.findById(userDetails.getId());
    }
}
