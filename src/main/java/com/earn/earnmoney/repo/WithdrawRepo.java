package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.Withdraw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WithdrawRepo extends JpaRepository<Withdraw, Long> {

    Page<Withdraw> findByStatus(com.earn.earnmoney.model.WithdrawStatus status, Pageable pageable);

    @Query("SELECT r.id , r.amount,r.kindWallet,r.kindWithdraw,r.user,r.date,r.userFullName,r.wallet,r.withdrawImage.id FROM Withdraw r  where r.stateWithdraw = false and r.user LIKE %:query% ")
    Page<Withdraw> findWithdrawPage(@Param("query") String query, Pageable pageable);

    @Query("SELECT r FROM Withdraw r where r.stateWithdraw = false")
    List<Object[]> findAllList();

    Page<Withdraw> findByStateWithdrawFalse(Pageable pageable);

    List<Withdraw> findAll();

    @Query("SELECT r FROM Withdraw r WHERE r.user = :user AND r.status <> com.earn.earnmoney.model.WithdrawStatus.REJECTED")
    Page<Withdraw> findWithdrawPageByUser(@Param("user") String user, Pageable pageable);

    List<Withdraw> findAllByUser(String name);

    Optional<Withdraw> findByUser(String name);

    @Query("SELECT  COALESCE(SUM(r.amount), 0) FROM Withdraw r where r.userId = :userId AND r.status <> com.earn.earnmoney.model.WithdrawStatus.REJECTED")
    Double sumAmountByUser(@Param("userId") Long userId);

    long countByUserIdAndStatus(Long userId, com.earn.earnmoney.model.WithdrawStatus status);

    long countByStateWithdrawFalse();

    long countByStatus(com.earn.earnmoney.model.WithdrawStatus status);

}
