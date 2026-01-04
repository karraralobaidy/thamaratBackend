package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.LogTransactionImpl;
import com.earn.earnmoney.model.LogTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogTransactionController {
    private final LogTransactionImpl ogTransactionImpl;

    @GetMapping("/v1/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adsAllPage(@RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "query", defaultValue = "") String search) {
        Map<String, Object> response = new HashMap<>();
        Page<LogTransaction> s = ogTransactionImpl.getAllLog(pageNumber, size, search);
        response.put("log", s.getContent());
        response.put("pageNumber", s.getNumber());
        response.put("totalPage", s.getTotalPages());
        response.put("totalLog", s.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/currentuser")
    public ResponseEntity<Map<String, Object>> adsAllCrrentUser(@RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // get Current username
        Page<LogTransaction> s = ogTransactionImpl.getAllLog(pageNumber, size, authentication.getName());
        response.put("log", s.getContent());
        response.put("pageNumber", s.getNumber());
        response.put("totalPage", s.getTotalPages());
        response.put("totalLog", s.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLogByUserId(@PathVariable Long userId,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> response = new HashMap<>();
        Page<LogTransaction> s = ogTransactionImpl.getLogByUserId(userId, pageNumber, size);
        response.put("log", s.getContent());
        response.put("pageNumber", s.getNumber());
        response.put("totalPage", s.getTotalPages());
        response.put("totalLog", s.getTotalElements());
        return ResponseEntity.ok(response);
    }

}
