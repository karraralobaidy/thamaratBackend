package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.UserWheelSpin;
import com.earn.earnmoney.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserWheelSpinRepo extends JpaRepository<UserWheelSpin, Long> {
    // Check if user has spun today
    boolean existsByUserAndSpinDateAfter(UserAuth user, LocalDateTime date);
}
