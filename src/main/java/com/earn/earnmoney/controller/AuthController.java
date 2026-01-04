package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.CounterService;
import com.earn.earnmoney.Service.RoleServiceImpl;
import com.earn.earnmoney.Service.UserAuthServiceImpl;
import com.earn.earnmoney.globalMethod.BucketMethod;
import com.earn.earnmoney.model.LogTransaction;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.payload.request.LoginRequest;
import com.earn.earnmoney.payload.response.JwtResponse;
import com.earn.earnmoney.payload.response.MessageResponse;
import com.earn.earnmoney.security.jwt.JwtUtils;
import com.earn.earnmoney.security.services.UserDetailsImpl;
import io.github.bucket4j.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    AuthenticationManager authenticationManager;

    UserAuthServiceImpl userService;
    CounterService counterService;

    RoleServiceImpl roleService;

    PasswordEncoder encoder;

    JwtUtils jwtUtils;

    @GetMapping("/verify-token") // check token and return Role of User
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String username = jwtUtils.getUserNameFromJwtToken(token);
            UserAuth user = userService.findByUsername(username).orElse(null);
            assert user != null;
            System.out.println(user.getRoles());
            return ResponseEntity.ok(
                    user.getRoles());
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("خطأ في token"));
        }

    }

    // private static final Bucket bucket = Bucket4j.builder()
    // .addLimit(Bandwidth.simple(2, Duration.ofMinutes(1))) // تكوين الحد الأقصى
    // للتحمل: 100 طلب في الدقيقة
    // .build();
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    // private static final Bucket globalBucket = Bucket4j.builder()
    // .addLimit(Bandwidth.classic(10, Refill.greedy(1, Duration.ofHours(5))))
    // .build();

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserAuthServiceImpl userService,
            CounterService counterService, RoleServiceImpl roleService, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.counterService = counterService;
        this.roleService = roleService;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    private BucketMethod bucketMethod = new BucketMethod();

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        String clientIP = bucketMethod.getClientIP(request);

        Bucket bucket = ipBuckets.computeIfAbsent(clientIP, ip -> {
            Bandwidth limit = Bandwidth.classic(15, Refill.intervally(15, Duration.ofHours(1)));
            return Bucket.builder()
                    .addLimit(limit)
                    .build();

        });

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            if (!userService.existsByUsername(loginRequest.getUsername().toLowerCase())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("بريد الالكتروني خطأ أو كلمة المرور"));
            }

            if (!userService.isUserActive(loginRequest.getUsername().toLowerCase())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("الحساب غير مفعل حالياً"));
            }

            if (userService.isUserBand(loginRequest.getUsername().toLowerCase())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("تم حظر حسابك لمخالفته شروط الاستخدام"));
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getUsername().toLowerCase(),
                                loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = jwtUtils.generateJwtToken(authentication);
                String refreshToken = jwtUtils.generateRefreshToken(authentication);

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                List<String> roles = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

                UserAuth auth = userService.findById(userDetails.getId());
                counterService.resetUserCounters(auth); // تصفير العدادات عند الدخول الجديد
                bucket.reset(); // إعادة تعيين الباكيت حتى لا يحصل عليه النظام
                return ResponseEntity.ok(new JwtResponse(jwt,
                        refreshToken,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        roles,
                        auth.getPoints(),
                        auth.getFull_name(),
                        auth.getReferralCode(),
                        auth.getNumberOfReferral()));
            } catch (AuthenticationException e) {
                // Invalid credentials
                return ResponseEntity
                        .internalServerError()
                        .body(new MessageResponse("بريد الالكتروني خطأ أو كلمة المرور"));
            }
        } else {

            return BucketMethod.getTimeOfBucket(bucket);
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<LogTransaction>> getTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // get username is login
        UserAuth userAuth = userService.findByUsername(authentication.getName()).orElse(null);
        assert userAuth != null;
        Long userId = userAuth.getId();
        List<LogTransaction> transactions = userService.getTransactionHistory(userId);

        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        if (jwtUtils.validateJwtToken(refreshToken)) {
            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            Optional<UserAuth> userDetails = userService.findByUsername(username);

            if (userDetails.isPresent()) {
                UserAuth user = userDetails.get();
                List<String> roles = user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList());

                // استخدام UserDetailsImpl بدلاً من String
                UserDetailsImpl userPrincipal = UserDetailsImpl.build(user);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities());

                String newAccessToken = jwtUtils.generateJwtToken(authentication);
                String newRefreshToken = jwtUtils.generateRefreshToken(authentication);

                return ResponseEntity.ok(new JwtResponse(newAccessToken,
                        newRefreshToken,
                        user.getId(),
                        user.getUsername(),
                        roles,
                        user.getPoints(),
                        user.getFull_name(),
                        user.getReferralCode(),
                        user.getNumberOfReferral()));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }

}
