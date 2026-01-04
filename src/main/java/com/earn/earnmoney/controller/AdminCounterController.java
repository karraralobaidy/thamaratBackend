package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.CounterService;
import com.earn.earnmoney.dto.CounterDTO;
import com.earn.earnmoney.model.Counter;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/counters")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCounterController {

    private final CounterService counterService;

    public AdminCounterController(CounterService counterService) {
        this.counterService = counterService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCounters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CounterDTO> countersPage = counterService.getAllCountersDetails(page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("counters", countersPage.getContent());
        response.put("currentPage", countersPage.getNumber());
        response.put("totalItems", countersPage.getTotalElements());
        response.put("totalPages", countersPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Counter> addCounter(
            @RequestBody com.earn.earnmoney.payload.request.AddCounterRequest request) {
        Counter newCounter = counterService.addCounter(request);
        return ResponseEntity.ok(newCounter);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Counter> updateCounter(
            @PathVariable Long id,
            @RequestBody com.earn.earnmoney.payload.request.AddCounterRequest request) {
        Counter updatedCounter = counterService.updateCounter(id, request);
        return ResponseEntity.ok(updatedCounter);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteCounter(@PathVariable Long id) {
        counterService.deleteCounter(id);

        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", true);
        return ResponseEntity.ok(response);
    }
}
