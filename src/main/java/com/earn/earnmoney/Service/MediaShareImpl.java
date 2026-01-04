package com.earn.earnmoney.Service;


import com.earn.earnmoney.model.MediaShare;
import com.earn.earnmoney.repo.MediaShareRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MediaShareImpl implements MediaShareService {

    private final MediaShareRepo mediaShareRepo;

    @Override
    public Page<MediaShare> getAllMediaSharePage(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page,size);
        return mediaShareRepo.findAllUserImagePage(search,pageable);
    }

    @Override
    public List<MediaShare> getAll() {
        return mediaShareRepo.findAll();
    }

    @Override
    public MediaShare saveMediaShare(MediaShare mediaShare) {
        return mediaShareRepo.save(mediaShare);
    }

    @Override
    public Optional<MediaShare> getImageByUserName(String username) {
        return mediaShareRepo.findByName(username);
    }

    @Override
    public Optional<MediaShare> getImageByUserNameWithIdImage(String username) {
        return mediaShareRepo.findQuery(username);
    }

    @Override
    public Optional<MediaShare> getMediaShareById(Long id) {
        return mediaShareRepo.findById(id);
    }

    @Override
    public void deleteMediaShare(Long id) {
        mediaShareRepo.deleteById(id);
    }


}
