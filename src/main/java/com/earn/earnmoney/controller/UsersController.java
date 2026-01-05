package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.RoleServiceImpl;
import com.earn.earnmoney.Service.UserAuthNotFoundException;
import com.earn.earnmoney.Service.UserAuthServiceImpl;
import com.earn.earnmoney.globalMethod.BucketMethod;
import com.earn.earnmoney.model.ERole;
import com.earn.earnmoney.model.Role;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.payload.request.SignupRequest;
import com.earn.earnmoney.payload.response.MessageResponse;
import com.earn.earnmoney.dto.UserProfileResponse;
import com.earn.earnmoney.security.jwt.JwtUtils;
import com.earn.earnmoney.security.services.UserDetailsImpl;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    UserAuthServiceImpl userService;

    RoleServiceImpl roleService;

    PasswordEncoder encoder;

    private String active_code = null;

    JwtUtils jwtUtils;
    // @Autowired
    // private SubscriberService subscriberService;

    private final com.earn.earnmoney.Service.CounterService counterService;

    public UsersController(UserAuthServiceImpl userService, RoleServiceImpl roleService, PasswordEncoder encoder,
            com.earn.earnmoney.Service.CounterService counterService) {
        this.userService = userService;
        this.roleService = roleService;
        this.encoder = encoder;
        this.counterService = counterService;
        addDefaultUser();

    }

    public void initializeRoles() {
        if (roleService.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleService.save(new Role(ERole.ROLE_ADMIN));
        }
        if (roleService.findByName(ERole.ROLE_USER).isEmpty()) {
            roleService.save(new Role(ERole.ROLE_USER));
        }

    }

    public void addDefaultUser() {
        initializeRoles();
        if (!userService.existsByUsername("a@gmail.com")) {
            UserAuth user = new UserAuth("a@gmail.com",
                    encoder.encode("123"));

            Role adminRole = roleService.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            user.setReferralCode("HalalDefault2025");
            user.setFull_name("Admin User");
            user.setRoles(roles);
            user.setActive(true);
            user.setBand(false);
            userService.save(user);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile(getCurrentUser()));
    }

    private UserAuth getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userService.findById(userDetails.getId());
    }

    @GetMapping("/recipient-info/{referralCode}")
    public ResponseEntity<?> getRecipientInfo(@PathVariable String referralCode) {
        return userService.getUserByRefferalCode(referralCode)
                .<ResponseEntity<?>>map(user -> {
                    Map<String, String> info = new HashMap<>();
                    info.put("fullName", user.getFull_name());
                    return ResponseEntity.ok(info);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("المستخدم غير موجود")));
    }

    @GetMapping("/v1/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> users(@RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "query", defaultValue = "") String search) {
        Map<String, Object> response = new HashMap<>();
        Page<UserAuth> s = userService.getAllUsers(pageNumber, size, search);

        List<com.earn.earnmoney.dto.UserSummaryResponse> userSummaries = s.getContent().stream().map(user -> {
            Set<String> roles = new HashSet<>();
            user.getRoles().forEach(r -> roles.add(r.getName().name()));

            return new com.earn.earnmoney.dto.UserSummaryResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getFull_name(),
                    user.isActive(),
                    user.isBand(),
                    user.getPoints(),
                    user.getReferralCode(),
                    user.getNumberOfReferral(),
                    roles);
        }).collect(java.util.stream.Collectors.toList());

        response.put("user", userSummaries);
        response.put("pageNumber", s.getNumber());
        response.put("totalPage", s.getTotalPages());
        response.put("totalUser", s.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/all2")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> users2() {
        return userService.getAllUsersWithOutPage();
    }

    @DeleteMapping("/deleteuser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable Long id) {
        UserAuth user = userService.findById(id);
        userService.delete(user);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/adduseradmin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addUserOrAdmin(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userService.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("المستخدم مسجل مسبقاً"));
        }

        // Create new user's account
        UserAuth user = new UserAuth(signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleService.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleService.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    default:
                        Role userRole = roleService.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        user.setFull_name(signUpRequest.getFull_name());
        user = userService.saveUser(user);
        counterService.assignFreeCounterToUser(user);

        return ResponseEntity.ok(new MessageResponse("تمت الأضافة بنجاح"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            if (userService.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("البريد الألكتروني مسجل مسبقاً"));
            }
            if (!userService.existsByReferral_code(signUpRequest.getReferral_code_friend())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("رمز ألإحالة غير صحيح"));
            }
            if (!Objects.equals(signUpRequest.getActive_code(), getActive_code())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("رمز التفعيل خطأ"));
            }

            // Create new user's account
            UserAuth user = new UserAuth(signUpRequest.getUsername(),
                    encoder.encode(signUpRequest.getPassword()), signUpRequest.getFull_name(), 0L,
                    signUpRequest.getReferral_code_friend());

            Set<String> strRoles = signUpRequest.getRole();
            Set<Role> roles = new HashSet<>();

            if (strRoles == null) {
                Role userRole = roleService.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    Role userRole = roleService.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role user is not found."));
                    roles.add(userRole);
                });
            }
            // تحويل اسم المستخدم الى حروف صغيرة
            user.setUsername(signUpRequest.getUsername().toLowerCase());
            user.setRoles(roles);
            user.setBand(false);
            user.setActive(true);
            user.setDate(LocalDate.now());
            user.setNumberOfReferral(0);
            user.setPoints(0L);

            int min = 10724; // Minimum value of range
            int max = 904152469; // Maximum value of range
            int random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);

            if (userService.getUserByRefferalCode(String.valueOf(random_int)).isPresent()) {
                int random_int_new = (int) Math.floor(Math.random() * (max - min + 1) + min);
                user.setReferralCode(String.valueOf(random_int_new));
            } else {
                user.setReferralCode(String.valueOf(random_int));
            }
            user = userService.saveUser(user);

            // Increment friend's referral count
            userService.getUserByRefferalCode(signUpRequest.getReferral_code_friend()).ifPresent(friend -> {
                friend.setNumberOfReferral(friend.getNumberOfReferral() + 1);
                userService.saveUser(friend);
            });

            counterService.assignFreeCounterToUser(user);

            return ResponseEntity.ok(new MessageResponse("تم انشاء حسابك بنجاح"));

        } catch (Exception e) {
            return ResponseEntity
                    .ok(new MessageResponse("يوجد خطأ في التسجيل يرجى تسجيل مرة اخرى او تواصل مع الأدارة"));

        }
    }

    @GetMapping("/upadteactive/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String userUpdateActive(@RequestParam String active, @PathVariable Long id)
            throws MessagingException, UnsupportedEncodingException {
        UserAuth user = userService.getUserById(id).orElse(null);
        if (user != null) {
            user.setActive(!Boolean.parseBoolean(active));
            if (user.isActive()) {
                sendEmail(user.getUsername(),
                        " Thamarat - تم تفعيل حسابك بنجاح",
                        " مرحبا " + user.getFull_name()
                                + " , تم تفعيل حسابك بنجاح يمكنك الآن الدخول الى حسابك -  تطبيق Thamarat ");
            }
            userService.updateUser(user);
            return "تم التفعيل او الغاء تفعيل بنجاح";
        } else
            return " المستخدم هذا غير موجود";

    }

    @PostMapping("/updatepoints/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePoints(@PathVariable Long id, @RequestParam Long amount) {
        UserAuth user = userService.getUserById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("المستخدم غير موجود"));
        }

        long currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        long newPoints = currentPoints + amount;

        if (newPoints < 0) {
            return ResponseEntity.badRequest().body(new MessageResponse("لا يمكن أن يكون الرصيد سالب"));
        }

        user.setPoints(newPoints);
        userService.saveUser(user);

        return ResponseEntity.ok(new MessageResponse("تم تعديل النقاط بنجاح. الرصيد الحالي: " + newPoints));
    }

    @PostMapping("/update-referral-code/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateReferralCode(@PathVariable Long id, @RequestParam String newReferralCode) {
        // Validate input
        if (newReferralCode == null || newReferralCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("رمز الإحالة لا يمكن أن يكون فارغاً"));
        }

        // Check if user exists
        UserAuth user = userService.getUserById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("المستخدم غير موجود"));
        }

        // Check if new referral code is already in use by another user
        if (userService.existsByReferral_code(newReferralCode)) {
            // Make sure it's not the same user's current code
            if (!newReferralCode.equals(user.getReferralCode())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("رمز الإحالة مستخدم بالفعل. يرجى اختيار رمز آخر"));
            }
        }

        // Update referral code
        user.setReferralCode(newReferralCode);
        userService.saveUser(user);

        return ResponseEntity.ok(new MessageResponse("تم تحديث رمز الإحالة بنجاح إلى: " + newReferralCode));
    }

    public void sendEmail(String recipientEmail, String title, String body)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("thamaratconfirm@gmail.com", "Thamarat");
        helper.setTo(recipientEmail);
        String subject = title;
        String content = "<p>مرحبا,</p></br>" + body;

        helper.setSubject(subject);

        helper.setText(content, true);

        mailSender.send(message);
    }

    @GetMapping("/upadteband/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String userUpdateBand(@RequestParam String band, @PathVariable Long id) {
        UserAuth user = userService.getUserById(id).orElse(null);
        if (user != null) {
            user.setBand(!Boolean.parseBoolean(band));
            userService.updateUser(user);
            return "تم حظر او الغاء حظر بنجاح";
        } else
            return " المستخدم هذا غير موجود";
    }

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String recipientEmail, String link)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("thamaratconfirm@gmail.com", "Thamarat Support");
        helper.setTo(recipientEmail);
        String subject = "🔐 إعادة تعيين كلمة المرور - ثمرات";

        String content = "<div dir='rtl' style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; padding: 30px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'>"
                + "<h2 style='color: #2c3e50; text-align: center; margin-bottom: 20px;'>إعادة تعيين كلمة المرور</h2>"
                + "<p style='color: #555; font-size: 16px; line-height: 1.6;'>مرحباً،</p>"
                + "<p style='color: #555; font-size: 16px; line-height: 1.6;'>لقد تلقينا طلباً لإعادة تعيين كلمة المرور الخاصة بحسابك في تطبيق <b>ثمرات</b>.</p>"
                + "<p style='color: #555; font-size: 16px; line-height: 1.6;'>استخدم الرمز أدناه لإكمال العملية:</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<span style='display: inline-block; background-color: #e74c3c; color: #ffffff; font-size: 24px; font-weight: bold; padding: 15px 30px; border-radius: 5px; letter-spacing: 2px;'>"
                + link + "</span>"
                + "</div>"
                + "<p style='color: #7f8c8d; font-size: 14px; text-align: center;'>إذا لم تقم بطلب هذا التغيير، يرجى تجاهل هذا البريد الإلكتروني.</p>"
                + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>"
                + "<p style='color: #aaa; font-size: 12px; text-align: center;'>فريق دعم ثمرات &copy; 2025</p>"
                + "</div></div>";

        helper.setSubject(subject);

        helper.setText(content, true);

        mailSender.send(message);
    }

    public void sendEmailActiveCode(String recipientEmail, String activecode)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("thamaratConfirm@gmail.com", "Thamarat Team");
        helper.setTo(recipientEmail);

        String subject = "✨ رمز تفعيل حسابك - ثمرات";

        String content = "<div dir='rtl' style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; padding: 30px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'>"
                + "<h2 style='color: #27ae60; text-align: center; margin-bottom: 20px;'>مرحباً بك في ثمرات!</h2>"
                + "<p style='color: #555; font-size: 16px; line-height: 1.6; text-align: center;'>شكراً لتسجيلك معنا. لتفعيل حسابك والبدء في كسب الأرباح، يرجى استخدام الرمز التالي:</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<span style='display: inline-block; background-color: #27ae60; color: #ffffff; font-size: 28px; font-weight: bold; padding: 15px 40px; border-radius: 8px; letter-spacing: 5px;'>"
                + activecode + "</span>"
                + "</div>"
                + "<p style='color: #7f8c8d; font-size: 14px; text-align: center;'>نتمنى لك تجربة ممتعة ومربحة!</p>"
                + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>"
                + "<p style='color: #aaa; font-size: 12px; text-align: center;'>فريق ثمرات &copy; 2025</p>"
                + "</div></div>";

        helper.setSubject(subject);

        helper.setText(content, true);

        mailSender.send(message);
    }

    // @PostMapping("/activecode")
    // public String codeActiveSendByEmail(HttpServletRequest request, Model model)
    // {
    // String email = request.getParameter("email");
    // String message = null;
    //
    // try {
    //
    // int countSend = 5;
    // if(countSend >= 0){
    // setActive_code(RandomString.make(5).toLowerCase());
    // sendEmailActiveCode(email, getActive_code());
    // countSend= countSend - 1;
    // message = model.addAttribute("message", "We have sent a active code to your
    // email. number : " + countSend).toString();
    // }else {
    // message = model.addAttribute("message", "لقد تجاوزت الحد
    // المسموح").toString();
    // }
    //
    // } catch (Exception ex) {
    // model.addAttribute("error", ex.getMessage());
    // }
    // return message;
    // }

    private BucketMethod bucketMethod = new BucketMethod();
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    @PostMapping("/activecode")
    public ResponseEntity<?> codeActiveSendByEmail(HttpServletRequest request, Model model) {
        String clientIP = bucketMethod.getClientIP(request);
        System.out.println("Received activecode request from IP: " + clientIP);

        Bucket bucket = ipBuckets.computeIfAbsent(clientIP, ip -> {
            Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1)));
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        });

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            long minutesToWait = TimeUnit.NANOSECONDS.toMinutes(probe.getNanosToWaitForRefill());
            long secondsToWait = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) % 60;
            System.out.println("Rate limit exceeded for IP: " + clientIP);
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("لقد تجاوزت الحد المسموح. يرجى المحاولة بعد: "
                            + minutesToWait + " دقيقة و " + secondsToWait + " ثانية"));
        }

        try {
            String email = request.getParameter("email");
            System.out.println("Processing activecode for email: " + email);

            if (email == null || email.trim().isEmpty()) {
                System.out.println("Error: Email parameter is missing or empty");
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("البريد الإلكتروني مطلوب"));
            }

            // Generate a 6-digit random code
            Random rand = new Random();
            int randomNumber = 100000 + rand.nextInt(900000);
            setActive_code(String.valueOf(randomNumber));

            System.out.println("Generated code: " + getActive_code() + " for email: " + email);

            sendEmailActiveCode(email, getActive_code());

            System.out.println("Email sent successfully to: " + email);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new MessageResponse("تم ارسال رمز التفعيل"));

        } catch (MessagingException e) {
            System.err.println("MessagingException while sending email: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("فشل في إرسال البريد الإلكتروني. يرجى التحقق من صحة الإيميل"));
        } catch (UnsupportedEncodingException e) {
            System.err.println("UnsupportedEncodingException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("خطأ في ترميز البريد الإلكتروني"));
        } catch (Exception e) {
            System.err.println("Unexpected error in activecode endpoint: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("حدث خطأ غير متوقع. يرجى المحاولة لاحقاً"));
        }
    }

    @PostMapping("/forgot_password")
    public String processForgotPassword(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String token = RandomString.make(8);

        try {
            userService.updateResetPasswordToken(token, email);
            // String resetPasswordLink = Utility.getSiteURL(request) + "/forgotpassword/" +
            // token;
            sendEmail(email, token);
            model.addAttribute("message", "We have sent a reset password link to your email. Please check.");
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
        } catch (UserAuthNotFoundException e) {
            throw new RuntimeException(e);
        }

        return "تم الأرسال رابط اعادة كلمة المرور بنجاح";
    }

    @GetMapping("/reset_password")
    public String showResetPasswordForm(@Param(value = "token") String token) {
        UserAuth user = userService.getByResetPasswordToken(token);
        if (user == null) {
            return "false";
        }
        return "true";
    }

    @PostMapping("/reset_password")
    public String processResetPassword(@Param(value = "token") String token,
            @Param(value = "password") String password) {

        UserAuth user = userService.getByResetPasswordToken(token);

        if (user == null) {
            return "خطأ لا يمكنك تغير كلمة المرور";
        } else {
            userService.updatePassword(user, password);
        }
        return "تم تغير كلمة المرور بنجاح";
    }

    // @GetMapping("/getuserbyusername/{username}")
    // @PreAuthorize("hasRole('ADMIN')")
    // public @NotBlank @Size(max = 120) String getuserByUsername(@PathVariable
    // String username) {
    // UserAuth userAuth = userService.findByUsername(username).orElse(null);
    // assert userAuth != null;
    // return userAuth.getFull_name();
    // }
    public String getActive_code() {
        return active_code;
    }

    public void setActive_code(String active_code) {
        this.active_code = active_code;
    }

}
