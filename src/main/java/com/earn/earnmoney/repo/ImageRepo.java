package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.Ads;
import com.earn.earnmoney.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ImageRepo extends JpaRepository<Image,Long> {

    Image findImageById(Long id);

}
