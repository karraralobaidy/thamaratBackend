package com.earn.earnmoney.repo;
// package com.earn.earnmoney.repo;

// import com.earn.earnmoney.model.Packages;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// @Repository
// public interface PackagesRep extends JpaRepository<Packages, Long> {
//     @Query("SELECT r FROM Packages r WHERE r.name LIKE %:query%")
//     Page<Packages> findByQuery(@Param("query") String query, Pageable pageable);

//     Packages findByIncome(String income);
// }
