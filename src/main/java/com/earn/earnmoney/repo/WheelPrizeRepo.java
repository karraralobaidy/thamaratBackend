package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.WheelPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WheelPrizeRepo extends JpaRepository<WheelPrize, Long> {
}
