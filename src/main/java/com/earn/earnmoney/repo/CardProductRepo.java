package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.CardProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardProductRepo extends JpaRepository<CardProduct, Long> {
    List<CardProduct> findByAvailableTrue();

    // Marketplace methods
    List<CardProduct> findByApprovalStatus(CardProduct.ApprovalStatus approvalStatus);

    List<CardProduct> findByApprovalStatusAndAvailableTrue(CardProduct.ApprovalStatus approvalStatus);

    List<CardProduct> findBySellerIdOrderByIdDesc(Long sellerId);
}
