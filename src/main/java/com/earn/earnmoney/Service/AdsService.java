package com.earn.earnmoney.Service;
import com.earn.earnmoney.model.Ads;
import org.springframework.data.domain.Page;


import java.util.List;
import java.util.Optional;

public interface AdsService {
    Ads saveAds(Ads ads);

    Page<Ads> getAllAdsPage(int page , int size);


    Optional<Ads> getAdsById(Long id);
    List<Ads> getAllAds();

    List<Object[]> getAllAdsObject();



    void deleteAds(Long id);


    Ads updateAds(Ads ads);

//    List<Object[]> findAdsWithoutImages();

}
