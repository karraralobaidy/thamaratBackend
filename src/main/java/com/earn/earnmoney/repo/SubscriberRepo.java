package com.earn.earnmoney.repo;
// package com.earn.earnmoney.repo;

// import com.earn.earnmoney.dto.SubscriberFullDetailDTO;
// import com.earn.earnmoney.model.Subscriber;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.time.LocalDate;
// import java.util.List;
// import java.util.Optional;
// @Repository
// public interface SubscriberRepo extends JpaRepository<Subscriber, Long> {

//     @Query("SELECT SUM(s.income) FROM Subscriber s")
//     Long getTotalIncome();


//     //with dont dto
//     @Query("SELECT r.id,r.dateStart,r.dateEnd,r.point,r.profit,r.id ,r.duration,r.income ,r.name,r.wallet,r.user.full_name,r.user.points ,r.user.numberOfReferral,r.user.username,r.user.id,r.user.active,r.user.band  FROM Subscriber r WHERE r.dateEnd >= :dateEnd and r.name != 'اشتراك مجاني'  and (r.user.full_name LIKE %:query%  or r.user.username LIKE %:query%) ")
//     Page<Subscriber> findSubscriberPages(@Param("query") String query, @Param("dateEnd") LocalDate date, Pageable pageable);

//     // with dto

//     @Query("SELECT new com.earn.earnmoney.dto.SubscriberFullDetailDTO(" + // Pointing to your DTO
//             "r.id, r.dateStart, r.dateEnd, r.point, r.profit, r.duration, r.income, " + // Removed the duplicate 'r.id'
//             "r.name, r.wallet, r.user.full_name, r.user.points, r.user.numberOfReferral, " +
//             "r.user.username, r.user.id, r.user.active, r.user.band) " +
//             "FROM Subscriber r WHERE r.dateEnd >= :dateEnd AND r.name != 'اشتراك مجاني' " +
//             "AND (r.user.full_name LIKE %:query% OR r.user.username LIKE %:query%)")
//     Page<SubscriberFullDetailDTO> findSubscriberDetailsProjection(@Param("query") String query, @Param("dateEnd") LocalDate date, Pageable pageable);


//     //expire
//     @Query("SELECT new com.earn.earnmoney.dto.SubscriberFullDetailDTO(" + // Pointing to your DTO
//             "r.id, r.dateStart, r.dateEnd, r.point, r.profit, r.duration, r.income, " + // Removed the duplicate 'r.id'
//             "r.name, r.wallet, r.user.full_name, r.user.points, r.user.numberOfReferral, " +
//             "r.user.username, r.user.id, r.user.active, r.user.band) " +
//             "FROM Subscriber r WHERE r.dateEnd <= :dateEnd AND r.name != 'اشتراك مجاني' " +
//             "AND (r.user.full_name LIKE %:query% OR r.user.username LIKE %:query%)")
//     Page<SubscriberFullDetailDTO> findSubscriberDetailsProjectionExpire(@Param("query") String query, @Param("dateEnd") LocalDate date, Pageable pageable);



//     //
//     @Query("SELECT r.id,r.dateStart,r.dateEnd,r.point,r.profit " +
//             ",r.duration,r.income ,r.name,r.wallet" +
//             ",r.user.full_name,r.user.points ,r.user.numberOfReferral" +
//             ",r.user.username,r.user.id,r.user.active,r.user.band " +
//             "FROM Subscriber r WHERE r.dateEnd >= :dateEnd and r.name != 'اشتراك مجاني' and r.cumulative = true and (r.user.full_name LIKE %:query%  or r.user.username LIKE %:query%) ")
//     Page<Subscriber> findCumulativeSubscriberPages(@Param("query") String query, @Param("dateEnd") LocalDate date, Pageable pageable);

//     @Query("SELECT r.id,r.dateStart,r.dateEnd,r.point,r.profit,r.id ,r.duration,r.income ,r.name,r.wallet,r.user.full_name,r.user.points ,r.user.numberOfReferral,r.user.username,r.user.id,r.user.active,r.user.band FROM Subscriber r WHERE r.dateEnd <= :dateEnd and r.name != 'اشتراك مجاني'  and (r.user.full_name LIKE %:query%  or r.user.username LIKE %:query%) ")
//     Page<Subscriber> findSubscriberPagesExpire(@Param("query") String query, @Param("dateEnd") LocalDate date, Pageable pageable);

//     @Query("SELECT r.id,r.dateStart,r.dateEnd,r.point,r.profit,r.id ,r.duration,r.income ,r.name,r.wallet,r.user.full_name,r.user.points ,r.user.numberOfReferral FROM Subscriber r WHERE r.dateEnd <= :dateEnd  ")
//     List<Object[]> findAllSubscriberExpire( @Param("dateEnd") LocalDate date);

//     @Query("SELECT r.id,r.dateStart,r.dateEnd,r.point,r.profit,r.id ,r.duration,r.income ,r.name,r.wallet,r.user.full_name,r.user.points ,r.user.numberOfReferral FROM Subscriber r WHERE r.dateEnd >= :dateEnd ")
//     List<Object[]> findAllSubscriberActiveForCount( @Param("dateEnd") LocalDate date);
//     @Query("SELECT r.id,r.dateStart,r.dateEnd,r.point,r.profit,r.id ,r.duration,r.income ,r.name,r.wallet,r.user.full_name,r.user.points ,r.user.numberOfReferral FROM Subscriber r")
//     List<Object[]> findAllList();

//     @Query("SELECT r.id FROM Subscriber r where r.dateEnd <= current_date and r.user.username = :user")
//     Object findSubscriberExpireByUsername(@Param("user") String username);

//     @Query("SELECT r.id,r.profit FROM Subscriber r where r.duration = :duration and r.dateEnd >= :dateEnd")
//     List<Object[]> findAllByPaymentDuration(@Param("duration") int duration,@Param("dateEnd") LocalDate date);

//     @Query("SELECT r.id,r.profit FROM Subscriber r where r.income = :income and r.dateEnd >= :dateEnd")
//     List<Object[]> findAllByIncome(@Param("income") double income,@Param("dateEnd") LocalDate date);

// //    Optional<Subscriber> findByPaymentId(Long id);
//     Subscriber findByUserUsername(String username);

//     @Query("SELECT r FROM Subscriber r WHERE r.user.username = :username AND r.name != 'اشتراك مجاني'")
//     Subscriber findSubscriberByUsernameNotFree(String username);

//     Optional<Subscriber> findByUserId(Long id);
//     List<Subscriber> findAll();

//     @Query("SELECT r.name FROM Subscriber r group by r.name ")
//     List<String> findActiveNamePackges();
// }
