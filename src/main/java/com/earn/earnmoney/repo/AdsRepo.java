package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.Ads;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AdsRepo extends JpaRepository<Ads, Long> {

    @Query("SELECT r.id, r.name, r.urlAds,r .date,r.adsImage.id FROM Ads r")
    Page<Ads> findAdsWithoutImages(Pageable pageable);
    @Query("SELECT r.id, r.name, r.urlAds,r .date,r.adsImage.id FROM Ads r")
    List<Object[]> findAllList();


    List<Ads> findAll();

}
