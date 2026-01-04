package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounterRepo extends JpaRepository<Counter, Long> {

        List<Counter> findByActiveTrue();

        Counter findFirstByPrice(Long price);

}
