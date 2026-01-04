package com.earn.earnmoney.Service;

import com.earn.earnmoney.model.UserImage;
import com.earn.earnmoney.model.Withdraw;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface UserImageService {
    Page<UserImage> getAllUserImagePage(int page, int size, String search);

    UserImage saveUserImage(UserImage userImage);

    Optional<UserImage> getImageByUserName(String username);
    Optional<UserImage> getImageByUserNameWithIdImage(String username);

//    List<Object[]> findUserImageWithoutImages();

}
