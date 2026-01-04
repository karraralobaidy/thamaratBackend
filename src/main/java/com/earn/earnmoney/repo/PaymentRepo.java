package com.earn.earnmoney.repo;
// package com.earn.earnmoney.repo;

// import com.earn.earnmoney.model.Payment;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.util.List;
// import java.util.Optional;
// @Repository
// public interface PaymentRepo extends JpaRepository<Payment, Long> {

//     @Query("SELECT r.id,r.name,r.income,r.duration,r.kindWallet,r.wallet,r.date,r.user,r.paymentImage.id ,r.oldIncome FROM Payment r where r.stateSubscriber = false and r.user LIKE %:query% ")
//     Page<Payment> findPaymentPage(@Param("query") String query, Pageable pageable);
//     @Query("SELECT r.id,r.name,r.income,r.duration,r.kindWallet,r.wallet,r.date,r.user,r.paymentImage.id FROM Payment r where r.stateSubscriber = false ")
//     List<Object[]> findAllList();

//     List<Payment> findAll();

//     Optional<Payment> findByUser(String name);

// }
