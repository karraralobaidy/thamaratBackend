package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    // Additional query methods if needed
}
