package com.earn.earnmoney.Service;


import com.earn.earnmoney.Service.AdsService;
import com.earn.earnmoney.model.Ads;
import com.earn.earnmoney.repo.AdsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdsImpl implements AdsService {

    private final AdsRepo adsRepo;

    @Override
    public Ads saveAds(Ads ads) {
        return adsRepo.save(ads);
    }

    @Override
    public Page<Ads> getAllAdsPage(int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        return adsRepo.findAdsWithoutImages(pageable);
    }

    @Override
    public List<Ads> getAllAds() {
        return adsRepo.findAll();
    }

    @Override
    public List<Object[]> getAllAdsObject() {
        return adsRepo.findAllList();
    }


    @Override
    public Optional<Ads> getAdsById(Long id) {
        return adsRepo.findById(id);
    }

    @Override
    public void deleteAds(Long id) {
         adsRepo.deleteById(id);
    }


    @Override
    public Ads updateAds(Ads ads) {
        return adsRepo.save(ads);
    }

}
