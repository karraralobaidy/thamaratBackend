package com.earn.earnmoney.Service;

import com.earn.earnmoney.model.LogTransaction;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.model.UserWheelSpin;
import com.earn.earnmoney.model.WheelPrize;
import com.earn.earnmoney.repo.LogTransactionRepo;
import com.earn.earnmoney.repo.UserAuthRepo;
import com.earn.earnmoney.repo.UserWheelSpinRepo;
import com.earn.earnmoney.repo.WheelPrizeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GameService {

    private final WheelPrizeRepo wheelPrizeRepo;
    private final UserWheelSpinRepo userWheelSpinRepo;
    private final UserAuthRepo userRepo;
    private final LogTransactionRepo logRepo;

    // Default configuration cost
    private static final int SPIN_COST = 50;

    @PostConstruct
    public void init() {
        if (wheelPrizeRepo.count() == 0) {
            initializeDefaultPrizes();
        }
    }

    private void initializeDefaultPrizes() {
        List<WheelPrize> prizes = new ArrayList<>();
        // REFERENCE IMAGE STYLE - Purple/Orange alternating

        // 0 Points: 28.65% - Purple segment
        prizes.add(new WheelPrize(null, "حظ أوفر", 0, 28.65, "#7B2D8E", "sad-outline"));
        // 25 Points: 34% - Orange segment
        prizes.add(new WheelPrize(null, "25 نقطة", 25, 34.0, "#E8702A", "gift-outline"));
        // 50 Points: 20% - Purple segment
        prizes.add(new WheelPrize(null, "50 نقطة", 50, 20.0, "#7B2D8E", "star-outline"));
        // 75 Points: 10% - Orange segment
        prizes.add(new WheelPrize(null, "75 نقطة", 75, 10.0, "#E8702A", "flame-outline"));
        // 100 Points: 4% - Purple segment
        prizes.add(new WheelPrize(null, "100 نقطة", 100, 4.0, "#7B2D8E", "diamond-outline"));
        // 200 Points: 1% - Orange segment
        prizes.add(new WheelPrize(null, "200 نقطة!", 200, 1.0, "#E8702A", "trophy-outline"));
        // 1000 Points: 0.3% - Gold segment (legendary)
        prizes.add(new WheelPrize(null, "1000 نقطة!!", 1000, 0.3, "#FFD700", "medal-outline"));
        // 5000 Points: 0.05% - Pink segment (mythical)
        prizes.add(new WheelPrize(null, "5000 نقطة!!!", 5000, 0.05, "#FF1493", "ribbon-outline"));

        wheelPrizeRepo.saveAll(prizes);
    }

    public List<WheelPrize> getWheelPrizes() {
        return wheelPrizeRepo.findAll();
    }

    @Transactional
    public Map<String, Object> spinWheel(UserAuth user) {
        // 1. Check daily limit / Free Spin eligibility
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        boolean hasSpunToday = userWheelSpinRepo.existsByUserAndSpinDateAfter(user, startOfDay);

        boolean isFreeSpin = !hasSpunToday;

        // 2. Transact Cost if not free
        if (!isFreeSpin) {
            if (user.getPoints() < SPIN_COST) {
                throw new RuntimeException("رصيدك غير كافٍ. تكلفة التدويرة " + SPIN_COST + " نقاط.");
            }
            user.setPoints(user.getPoints() - SPIN_COST);
        }

        // 3. Determine Prize
        WheelPrize prize = calculateWinningPrize();

        // Store previous balance for logging
        double previousBalance = user.getPoints();

        // 4. Award Prize
        if (prize.getValue() > 0) {
            user.setPoints(user.getPoints() + prize.getValue());
        }

        userRepo.save(user);

        // 5. Log Transaction
        LogTransaction log = new LogTransaction();
        log.setUserId(user.getId());
        log.setUsername(user.getUsername());
        log.setFullName(user.getFull_name());
        log.setType("WHEEL_SPIN");

        if (prize.getValue() > 0) {
            log.setDescription("عجلة الحظ: +" + prize.getValue() + " نقطة");
        } else {
            log.setDescription("عجلة الحظ: حظ أوفر المرة القادمة");
        }

        log.setTransactionDate(LocalDateTime.now(ZoneId.of("Asia/Baghdad")));
        log.setPreviousBalance((double) previousBalance);
        log.setNewBalance((double) user.getPoints());
        logRepo.save(log);

        // 6. Save Spin Record
        UserWheelSpin spin = new UserWheelSpin();
        spin.setUser(user);
        spin.setSpinDate(LocalDateTime.now());
        spin.setPrizeName(prize.getName());
        spin.setPointsWon(prize.getValue());
        spin.setFreeSpin(isFreeSpin);
        userWheelSpinRepo.save(spin);

        // 6. Return Result
        Map<String, Object> result = new HashMap<>();
        result.put("prize", prize);
        result.put("pointsWon", prize.getValue());
        result.put("cost", isFreeSpin ? 0 : SPIN_COST);
        result.put("remainingPoints", user.getPoints());
        result.put("isFreeSpin", isFreeSpin);

        return result;
    }

    private WheelPrize calculateWinningPrize() {
        List<WheelPrize> prizes = wheelPrizeRepo.findAll();
        double totalWeight = prizes.stream().mapToDouble(WheelPrize::getProbability).sum();
        double randomRef = Math.random() * totalWeight;

        double currentWeight = 0;
        for (WheelPrize prize : prizes) {
            currentWeight += prize.getProbability();
            if (randomRef <= currentWeight) {
                return prize;
            }
        }
        // Fallback (shouldn't happen)
        return prizes.get(0);
    }

    public SpinStatus checkStatus(UserAuth user) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        boolean hasSpunToday = userWheelSpinRepo.existsByUserAndSpinDateAfter(user, startOfDay);
        return new SpinStatus(!hasSpunToday, SPIN_COST);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class SpinStatus {
        private boolean canSpinFree;
        private int cost;
    }
}
