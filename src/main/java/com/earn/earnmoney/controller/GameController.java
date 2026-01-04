package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.GameService;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.repo.UserAuthRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserAuthRepo userRepo;

    @GetMapping("/wheel/config")
    public ResponseEntity<?> getWheelConfig(@RequestParam Long userId) {
        if (userId == null)
            return ResponseEntity.badRequest().build();
        UserAuth user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("prizes", gameService.getWheelPrizes());
        response.put("status", gameService.checkStatus(user));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/wheel/spin")
    public ResponseEntity<?> spinWheel(@RequestParam Long userId) {
        if (userId == null)
            return ResponseEntity.badRequest().build();
        UserAuth user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            Map<String, Object> result = gameService.spinWheel(user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new HashMap<>() {
                {
                    put("message", e.getMessage());
                }
            });
        }
    }
}
