package com.earn.earnmoney.Service;


import com.earn.earnmoney.model.UserImage;
import com.earn.earnmoney.model.Withdraw;
import com.earn.earnmoney.repo.UserImageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserImageImpl implements UserImageService {

    private final UserImageRepo userImageRepo;

    @Override
    public Page<UserImage> getAllUserImagePage(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page,size);
        return userImageRepo.findAllUserImagePage(search,pageable);
    }

    @Override
    public UserImage saveUserImage(UserImage userImage) {
        return userImageRepo.save(userImage);
    }

    @Override
    public Optional<UserImage> getImageByUserName(String username) {
        return userImageRepo.findByUsername(username);
    }

    @Override
    public Optional<UserImage> getImageByUserNameWithIdImage(String username) {
        return userImageRepo.findQuery(username);
    }


}
