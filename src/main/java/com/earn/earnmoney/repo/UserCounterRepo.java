package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.Counter;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.model.UserCounter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCounterRepo extends JpaRepository<UserCounter, Long> {

        Optional<UserCounter> findByCounterAndLevel(
                        Counter counter, int level);

        List<UserCounter> findByCounterOrderByLevelAsc(
                        Counter counter);

        List<UserCounter> findByUser(UserAuth user);

        boolean existsByUserAndCounter(UserAuth user, Counter counter);

        Optional<UserCounter> findByIdAndUser(Long id, UserAuth user);

        List<UserCounter> findByUserAndCounter(UserAuth user, Counter counter);

        boolean existsByCounter(Counter counter);

}
