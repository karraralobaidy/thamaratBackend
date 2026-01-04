package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.LogTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogTransactionRepo extends JpaRepository<LogTransaction, Long> {
    List<LogTransaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    @Query("SELECT l FROM LogTransaction l  where  l.username LIKE %:query% order by l.id desc ")
    Page<LogTransaction> findAllPage(@Param("query") String query, Pageable pageable);

    @Query("SELECT l FROM LogTransaction l WHERE l.userId = :userId ORDER BY l.transactionDate DESC")
    Page<LogTransaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(t) FROM LogTransaction t WHERE t.userId = :userId AND t.type = :type AND t.transactionDate >= :startDate")
    long countTransactionsByUserAndTypeAfterDate(Long userId, String type, LocalDateTime startDate);
}
