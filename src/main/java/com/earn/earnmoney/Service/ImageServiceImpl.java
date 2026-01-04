package com.earn.earnmoney.Service;

import com.earn.earnmoney.model.Image;
import com.earn.earnmoney.repo.ImageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepo imageRepo;

    @Override
    public Image findImageById(Long id) {
        return imageRepo.findImageById(id);
    }
}
