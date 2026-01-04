package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.CardPurchase;
import com.earn.earnmoney.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CardPurchaseRepo extends JpaRepository<CardPurchase, Long> {
    List<CardPurchase> findByUserOrderByPurchaseDateDesc(UserAuth user);

    // Find all purchases where the card's sellerId matches the given id
    List<CardPurchase> findByCardProductSellerIdOrderByPurchaseDateDesc(Long sellerId);

    // Count purchases by status
    long countByStatus(CardPurchase.PurchaseStatus status);
}
