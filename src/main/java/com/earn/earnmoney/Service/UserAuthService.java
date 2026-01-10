package com.earn.earnmoney.Service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import com.earn.earnmoney.model.UserAuth;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface UserAuthService {

    Optional<UserAuth> findByUsername(String username);

    UserAuth findById(Long id);

    Boolean existsByUsername(String username);

    void save(UserAuth user);

    UserAuth saveUser(UserAuth user);

    List<Object[]> findUsersWithoutPassword();

    void delete(UserAuth user);

    void softDelete(UserAuth user);

    Boolean isUserActive(String username);

    Boolean isUserBand(String username);

    Boolean existsByReferral_code(String referralCode);

    Page<UserAuth> getAllUsers(int page, int size, String search);

    List<Object[]> getAllUsersWithOutPage();

    Optional<UserAuth> getUserById(Long id);

    UserAuth updateUser(UserAuth user);

    Optional<UserAuth> getUserByRefferalCode(String refferalCodeFreind);

    Boolean checkByUsernameAndPassword(String username, String password);

    // add new
    UserAuth updateProfileImage(Long userId, MultipartFile file) throws IOException;

    Page<UserAuth> findByReferralCodeFriend(String referralCodeFriend,
            org.springframework.data.domain.Pageable pageable);

    List<UserAuth> findAllByReferralCodeFriend(String referralCodeFriend);

}
