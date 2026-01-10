package com.earn.earnmoney.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.earn.earnmoney.model.Counter;
import com.earn.earnmoney.model.CounterPackage;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.model.UserCounter;
import com.earn.earnmoney.repo.CounterPackageRepo;
import com.earn.earnmoney.repo.CounterRepo;
import com.earn.earnmoney.repo.UserAuthRepo;
import com.earn.earnmoney.repo.UserCounterRepo;
import com.earn.earnmoney.model.LogTransaction;
import com.earn.earnmoney.repo.LogTransactionRepo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import com.earn.earnmoney.dto.CounterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@RequiredArgsConstructor
public class CounterService {

    private final CounterRepo counterRepo;
    private final CounterPackageRepo packageRepo;
    private final UserCounterRepo userCounterRepo;
    private final UserAuthRepo userRepo;
    private final LogTransactionRepo logRepo;

    // 🏪 متجر العدادات
    public List<CounterDTO> getAvailableCounters(UserAuth user) {
        return counterRepo.findByActiveTrue()
                .stream()
                .filter(c -> c.getPrice() > 0) // Hide Free Counter from store
                .map(this::convertToDTO)
                .sorted((c1, c2) -> Integer.compare(c1.getPointsPerClick(), c2.getPointsPerClick()))
                .toList();
    }

    private CounterDTO convertToDTO(Counter counter) {
        CounterDTO dto = new CounterDTO();
        dto.setId(counter.getId());
        dto.setName(counter.getName());
        dto.setCooldownHours(counter.getCooldownHours());
        dto.setPrice(counter.getPrice());
        dto.setPaid(counter.isPaid());
        dto.setActive(counter.isActive());

        // جلب المستوى الأول
        packageRepo.findByCounterAndLevel(counter, 1).ifPresent(pkg -> {
            dto.setPointsPerClick(pkg.getPointsPerClick());
        });
        dto.setDurationDays(counter.getDurationDays());

        return dto;
    }

    // 🛒 شراء عداد
    @Transactional
    public void buyCounter(Long counterId, UserAuth user) {

        Counter counter = counterRepo.findById(counterId)
                .orElseThrow();

        // 1. (Removed) Deleted code that wiped old counters. Users can now own
        // multiple.

        // 2. التحقق من الرصيد والخصم
        if (user.getPoints() < counter.getPrice())
            throw new RuntimeException("نقاط غير كافية");

        user.setPoints(user.getPoints() - counter.getPrice());

        // 3. إضافة العداد الجديد
        CounterPackage basePackage = packageRepo.findByCounterAndLevel(counter, 1)
                .orElseThrow();

        UserCounter uc = new UserCounter();
        uc.setUser(user);
        uc.setCounter(counter);
        uc.setCurrentPackage(basePackage);
        uc.setCounter(counter);
        uc.setCurrentPackage(basePackage);
        long duration = counter.getDurationDays() != null ? counter.getDurationDays() : 730;
        uc.setExpireAt(LocalDateTime.now().plusDays(duration));
        uc.setSubscribedAt(LocalDateTime.now());

        userRepo.save(user);
        userCounterRepo.save(uc);

        // تسجيل العملية
        logTransaction(user, "BUY_COUNTER",
                "شراء عداد: " + counter.getName(),
                (double) (user.getPoints() + counter.getPrice()),
                (double) user.getPoints());
    }

    @Transactional
    public void assignFreeCounterToUser(UserAuth user) {
        // 1. البحث عن العداد المجاني
        Counter freeCounter = counterRepo.findFirstByPrice(0L);

        // 2. إذا لم يكن موجوداً، نقوم بإنشائه (Initialize)
        if (freeCounter == null) {
            freeCounter = new Counter();
            freeCounter.setName("العداد المجاني");
            freeCounter.setPrice(0L);
            freeCounter.setCooldownHours(24); // يوميا
            freeCounter.setPaid(false);
            freeCounter.setActive(true);
            freeCounter = counterRepo.save(freeCounter);

            // إنشاء الباقة الأساسية لهذا العداد
            CounterPackage pack = new CounterPackage();
            pack.setCounter(freeCounter);
            pack.setLevel(1);
            pack.setPointsPerClick(100); // 100 نقاط يومياً
            pack.setUpgradeCost(500);
            packageRepo.save(pack);
        }

        // 3. التحقق مما إذا كان المستخدم يملكه بالفعل
        if (userCounterRepo.existsByUserAndCounter(user, freeCounter)) {
            return;
        }

        // 4. إسناد العداد للمستخدم
        CounterPackage basePackage = packageRepo.findByCounterAndLevel(freeCounter, 1)
                .orElseThrow(() -> new RuntimeException("Package level 1 missing for free counter"));

        UserCounter uc = new UserCounter();
        uc.setUser(user);
        uc.setCounter(freeCounter);
        uc.setCurrentPackage(basePackage);
        uc.setExpireAt(LocalDateTime.now().plusYears(100)); // العداد المجاني طويل الأمد (أو سنة حسب
        uc.setSubscribedAt(LocalDateTime.now());
        // الرغبة)

        userCounterRepo.save(uc);

        // تسجيل العملية
        logTransaction(user, "FREE_COUNTER",
                "الحصول على العداد المجاني",
                (double) user.getPoints(),
                (double) user.getPoints());
    }

    @Transactional
    public CounterActionResponse handleAction(UserAuth user) {
        List<UserCounter> counters = userCounterRepo.findByUser(user);
        if (counters.isEmpty()) {
            assignFreeCounterToUser(user);
            counters = userCounterRepo.findByUser(user);
        }

        if (counters.isEmpty()) {
            throw new RuntimeException("لا يوجد لديك أي عداد متاح حالياً");
        }

        LocalDateTime now = LocalDateTime.now();
        int totalPointsEarned = 0;
        LocalDateTime soonestNextAvailable = null;
        boolean anyCounterProcessed = false;

        for (UserCounter uc : counters) {
            // Check expiry
            if (uc.getExpireAt() != null && now.isAfter(uc.getExpireAt())) {
                continue; // Skip expired counters
            }

            long hours = 24; // Force 24-hour cooldown
            LocalDateTime nextAvailable = uc.getLastClickedAt() == null ? null : uc.getLastClickedAt().plusHours(hours);

            if (uc.getLastClickedAt() == null) {
                // START Phase
                uc.setLastClickedAt(now);
                userCounterRepo.save(uc);
                anyCounterProcessed = true;

                LocalDateTime itsNext = now.plusHours(hours);
                if (soonestNextAvailable == null || itsNext.isBefore(soonestNextAvailable)) {
                    soonestNextAvailable = itsNext;
                }
            } else if (now.isAfter(nextAvailable) || now.isEqual(nextAvailable)) {
                // CLAIM Phase
                int points = uc.getCurrentPackage().getPointsPerClick();
                totalPointsEarned += points;
                uc.setLastClickedAt(null); // Reset to IDLE after claim
                userCounterRepo.save(uc);
                anyCounterProcessed = true;
            } else {
                // RUNNING Phase (Wait)
                if (soonestNextAvailable == null || nextAvailable.isBefore(soonestNextAvailable)) {
                    soonestNextAvailable = nextAvailable;
                }
            }
        }

        if (!anyCounterProcessed) {
            throw new RuntimeException("العدادات في حالة انتظار حالياً");
        }

        if (totalPointsEarned > 0) {
            user.setPoints(user.getPoints() + totalPointsEarned);
            userRepo.save(user);

            logTransaction(user, "COUNTER_REWARD",
                    "حصد نقاط من كافة العدادات النشطة (+" + totalPointsEarned + ")",
                    (double) (user.getPoints() - totalPointsEarned),
                    (double) user.getPoints());

            // تطبيق مكافأة الإحالة - استثناء العدادات المجانية
            applyReferralReward(user, totalPointsEarned, counters);

            return CounterActionResponse.claimed(totalPointsEarned, user.getPoints().intValue());
        }

        return CounterActionResponse.started(soonestNextAvailable);
    }

    // ▶️ تشغيل / 🔄 إعادة تشغيل
    @Transactional
    public CounterActionResponse handleAction(
            Long userCounterId, UserAuth user) {

        UserCounter uc = userCounterRepo.findByIdAndUser(userCounterId, user)
                .orElseThrow();

        LocalDateTime now = LocalDateTime.now();

        // التحقق من انتهاء الاشتراك
        if (uc.getExpireAt() != null && now.isAfter(uc.getExpireAt())) {
            throw new RuntimeException("انتهى اشتراك هذا العداد، يرجى تجديد الاشتراك");
        }

        long hours = 24; // Force 24-hour cooldown

        if (uc.getLastClickedAt() == null) {
            // START
            uc.setLastClickedAt(now);
            userCounterRepo.save(uc);
            return CounterActionResponse.started(now.plusHours(hours));
        }

        LocalDateTime end = uc.getLastClickedAt().plusHours(hours);

        if (now.isBefore(end))
            throw new RuntimeException("العداد ما زال قيد التعدين");

        // CLAIM
        int points = uc.getCurrentPackage().getPointsPerClick();

        user.setPoints(user.getPoints() + points);
        uc.setLastClickedAt(null); // Reset to IDLE after claim

        userRepo.save(user);
        userCounterRepo.save(uc);

        // تسجيل العملية
        logTransaction(user, "COUNTER_REWARD",
                "حصد نقاط العداد: " + uc.getCounter().getName(),
                (double) (user.getPoints() - points),
                (double) user.getPoints());

        // تطبيق مكافأة الإحالة - التحقق من كون العداد مجاني
        applyReferralReward(user, points, uc.getCounter());

        return CounterActionResponse.claimed(points, user.getPoints().intValue());
    }

    // Method for handling multiple counters (from handleAction with no specific
    // counter)
    private void applyReferralReward(UserAuth invitee, int earnedPoints, List<UserCounter> counters) {
        if (invitee.getReferralCodeFriend() == null || invitee.getReferralCodeFriend().isEmpty()) {
            return;
        }

        // Check if all counters are free - if so, skip referral
        boolean allFree = counters.stream().allMatch(uc -> uc.getCounter().getPrice() == 0);
        if (allFree) {
            return; // العداد المجاني لا يحصل على إحالة
        }

        userRepo.findByReferralCode(invitee.getReferralCodeFriend()).ifPresent(referrer -> {
            // حساب 1% من النقاط
            long reward = Math.round(earnedPoints * 0.01);

            // ضمان 1 نقطة كحد أدنى إذا كان هناك ربح
            if (reward <= 0 && earnedPoints > 0) {
                reward = 1;
            }

            if (reward > 0) {
                Long oldPoints = referrer.getPoints() != null ? referrer.getPoints() : 0L;
                referrer.setPoints(oldPoints + reward);
                userRepo.save(referrer);

                logTransaction(referrer, "REFERRAL",
                        "مكافأة إحالة بنسبة 1% من نشاط المستخدم: " + invitee.getFull_name() + " (+" + reward + " نقطة)",
                        (double) oldPoints,
                        (double) (oldPoints + reward));
            }
        });
    }

    // Method for handling single counter (from handleAction with specific counter)
    private void applyReferralReward(UserAuth invitee, int earnedPoints, Counter counter) {
        if (invitee.getReferralCodeFriend() == null || invitee.getReferralCodeFriend().isEmpty()) {
            return;
        }

        // استثناء العداد المجاني من الإحالة
        if (counter.getPrice() == 0) {
            return; // العداد المجاني لا يحصل على إحالة
        }

        userRepo.findByReferralCode(invitee.getReferralCodeFriend()).ifPresent(referrer -> {
            // حساب 1% من النقاط
            long reward = Math.round(earnedPoints * 0.01);

            // ضمان 1 نقطة كحد أدنى إذا كان هناك ربح
            if (reward <= 0 && earnedPoints > 0) {
                reward = 1;
            }

            if (reward > 0) {
                Long oldPoints = referrer.getPoints() != null ? referrer.getPoints() : 0L;
                referrer.setPoints(oldPoints + reward);
                userRepo.save(referrer);

                logTransaction(referrer, "REFERRAL",
                        "مكافأة إحالة بنسبة 1% من نشاط المستخدم: " + invitee.getFull_name() + " (+" + reward + " نقطة)",
                        (double) oldPoints,
                        (double) (oldPoints + reward));
            }
        });
    }

    // 🔄 تجديد الاشتراك - تم دمج منطقه في الشراء buyCounter
    // @Transactional
    // public void renewCounter(...) { ... }

    // 📈 ترقية العداد - تم الإلغاء
    // @Transactional
    // public void upgradeCounter(...) { ... }
    @Transactional
    public void resetUserCounters(UserAuth user) {
        List<UserCounter> counters = userCounterRepo.findByUser(user);
        for (UserCounter uc : counters) {
            uc.setLastClickedAt(null);
            userCounterRepo.save(uc);
        }
    }

    // --- Admin Methods ---

    public Page<Counter> getAllCounters(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return counterRepo.findAll(pageable);
    }

    public Page<CounterDTO> getAllCountersDetails(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Counter> counters = counterRepo.findAll(pageable);

        return counters.map(counter -> {
            CounterDTO dto = new CounterDTO();
            dto.setId(counter.getId());
            dto.setName(counter.getName());
            dto.setCooldownHours(counter.getCooldownHours());
            dto.setPrice(counter.getPrice());
            dto.setPaid(counter.isPaid());
            dto.setActive(counter.isActive());

            // جلب المستوى الأول
            packageRepo.findByCounterAndLevel(counter, 1).ifPresent(pkg -> {
                dto.setPointsPerClick(pkg.getPointsPerClick());
                // dto.setUpgradeCost(pkg.getUpgradeCost()); // تم الإلغاء
            });
            dto.setDurationDays(counter.getDurationDays());

            return dto;
        });
    }

    public Counter addCounter(com.earn.earnmoney.payload.request.AddCounterRequest request) {
        // 1. حفظ العداد
        Counter counter = new Counter();
        counter.setName(request.getName());
        counter.setCooldownHours(request.getCooldownHours());
        counter.setPrice(request.getPrice());
        counter.setPaid(request.isPaid());
        counter.setPrice(request.getPrice());
        counter.setPaid(request.isPaid());
        counter.setActive(request.isActive());
        counter.setDurationDays(request.getDurationDays() != null ? request.getDurationDays() : 730L);

        Counter savedCounter = counterRepo.save(counter);

        // 2. إنشاء المستوى الأول (Level 1)
        CounterPackage pack = new CounterPackage();
        pack.setCounter(savedCounter);
        pack.setLevel(1);
        pack.setPointsPerClick(request.getPointsPerClick());

        // تكلفة الترقية غير مستعملة، نضع 0
        pack.setUpgradeCost(0);

        packageRepo.save(pack);

        return savedCounter;
    }

    public Counter updateCounter(Long id, com.earn.earnmoney.payload.request.AddCounterRequest request) {
        Counter counter = counterRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("العداد غير موجود"));

        counter.setName(request.getName());
        counter.setCooldownHours(request.getCooldownHours());
        counter.setPrice(request.getPrice());
        counter.setPaid(request.isPaid());
        counter.setPrice(request.getPrice());
        counter.setPaid(request.isPaid());
        counter.setActive(request.isActive());
        if (request.getDurationDays() != null) {
            counter.setDurationDays(request.getDurationDays());
        }

        Counter savedCounter = counterRepo.save(counter);

        // تحديث المستوى الأول (Level 1)
        packageRepo.findByCounterAndLevel(savedCounter, 1).ifPresentOrElse(pack -> {
            pack.setPointsPerClick(request.getPointsPerClick());
            packageRepo.save(pack);
        }, () -> {
            // In case package missing (fallback)
            CounterPackage pack = new CounterPackage();
            pack.setCounter(savedCounter);
            pack.setLevel(1);
            pack.setPointsPerClick(request.getPointsPerClick());
            pack.setUpgradeCost(0);
            packageRepo.save(pack);
        });

        return savedCounter;
    }

    @Transactional
    public void deleteCounter(Long id) {
        Counter counter = counterRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("العداد غير موجود"));

        if (userCounterRepo.existsByCounter(counter)) {
            throw new RuntimeException("لا يمكن حذف العداد لأنه مستخدم من قبل مشتركين");
        }

        // حذف جميع باقات ومستويات هذا العداد أولاً
        packageRepo.deleteByCounter(counter);

        // ثم حذف العداد
        counterRepo.delete(counter);
    }

    private void logTransaction(UserAuth user, String type, String desc, Double prevBalance, Double newBalance) {
        LogTransaction log = new LogTransaction();
        log.setUserId(user.getId());
        log.setFullName(user.getFull_name() != null ? user.getFull_name() : user.getUsername());
        log.setUsername(user.getUsername());
        log.setType(type);
        log.setDescription(desc);
        log.setPreviousBalance(prevBalance);
        log.setNewBalance(newBalance);
        log.setTransactionDate(LocalDateTime.now(ZoneId.of("Asia/Baghdad")));
        logRepo.save(log);
    }

    // Get user's counter subscriptions with details
    public List<com.earn.earnmoney.dto.UserCounterSubscriptionDTO> getUserCounterSubscriptions(UserAuth user) {
        List<UserCounter> userCounters = userCounterRepo.findByUser(user);
        LocalDateTime now = LocalDateTime.now();

        return userCounters.stream().map(uc -> {
            com.earn.earnmoney.dto.UserCounterSubscriptionDTO dto = new com.earn.earnmoney.dto.UserCounterSubscriptionDTO();
            dto.setId(uc.getId());
            dto.setCounterId(uc.getCounter().getId());
            dto.setCounterName(uc.getCounter().getName());

            // Determine start date (use lastClickedAt or expireAt - duration)
            LocalDateTime startDate = uc.getExpireAt();
            if (startDate != null && uc.getCounter().getDurationDays() != null) {
                startDate = uc.getExpireAt().minusDays(uc.getCounter().getDurationDays());
            }
            dto.setStartDate(startDate);
            dto.setEndDate(uc.getExpireAt());

            // Get daily points from package
            Integer dailyPoints = uc.getCurrentPackage() != null ? uc.getCurrentPackage().getPointsPerClick() : 0;
            dto.setDailyPoints(dailyPoints);

            // Determine status
            boolean isExpired = uc.getExpireAt() != null && now.isAfter(uc.getExpireAt());
            dto.setStatus(isExpired ? "EXPIRED" : "ACTIVE");

            // Set if paid counter
            dto.setPaid(uc.getCounter().isPaid());

            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }
}
