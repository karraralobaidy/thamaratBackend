package com.earn.earnmoney.Service;

import com.earn.earnmoney.model.MediaShare;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface MediaShareService {
    Page<MediaShare> getAllMediaSharePage(int page, int size, String search);
    List<MediaShare> getAll();

    MediaShare saveMediaShare(MediaShare mediaShare);

    Optional<MediaShare> getImageByUserName(String username);
    Optional<MediaShare> getImageByUserNameWithIdImage(String username);
    Optional<MediaShare> getMediaShareById(Long id);
    void deleteMediaShare(Long id);

//    List<Object[]> findMediaShareWithoutImages();

}
