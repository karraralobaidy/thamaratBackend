package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.StoreCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreCardRepo extends JpaRepository<StoreCard, Long> {

    List<StoreCard> findByActiveTrue();

    List<StoreCard> findByCategory(String category);

    Page<StoreCard> findAll(Pageable pageable);

    Page<StoreCard> findByActiveTrue(Pageable pageable);
}
