package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.Counter;
import com.earn.earnmoney.model.CounterPackage;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.model.UserCounter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounterPackageRepo extends JpaRepository<CounterPackage, Long> {

        Optional<CounterPackage> findByCounterAndLevel(Counter counter, int level);

        void deleteByCounter(Counter counter);

}
