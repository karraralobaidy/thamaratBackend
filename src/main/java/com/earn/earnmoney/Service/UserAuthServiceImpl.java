package com.earn.earnmoney.Service;

import com.earn.earnmoney.dto.UserProfileResponse;
import com.earn.earnmoney.model.LogTransaction;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.model.UserCounter;
import com.earn.earnmoney.repo.LogTransactionRepo;
import com.earn.earnmoney.repo.UserAuthRepo;

import com.earn.earnmoney.model.Image;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.earn.earnmoney.util.ImageUtilities;
import com.earn.earnmoney.repo.UserCounterRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {

    private final UserAuthRepo userRepo;
    private final LogTransactionRepo logTransactionRepo;

    @Override
    public Optional<UserAuth> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public UserAuth findById(Long id) {
        return userRepo.findUserById(id);
    }

    @Override
    public void save(UserAuth user) {
        userRepo.save(user);
    }

    @Override
    public UserAuth saveUser(UserAuth user) {
        return userRepo.save(user);
    }

    @Override
    public List<Object[]> findUsersWithoutPassword() {
        return userRepo.findUsersWithoutPassword();
    }

    @Override
    public void delete(UserAuth user) {
        userRepo.delete(user);
    }

    public void softDelete(UserAuth user) {
        user.setDeleted(true);
        userRepo.save(user);
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    @Override
    public Boolean isUserActive(String username) {
        return userRepo.existsByUsernameAndActiveTrue(username);
    }

    @Override
    public Boolean isUserBand(String username) {
        return userRepo.existsByUsernameAndBandTrue(username);
    }

    @Override
    public Boolean existsByReferral_code(String referralCode) {
        return userRepo.existsByReferralCode(referralCode);
    }

    @Override
    public Page<UserAuth> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepo.findAllByQuery(search, pageable);
    }

    @Override
    public List<Object[]> getAllUsersWithOutPage() {
        return userRepo.getAllUser();
    }

    @Override
    public Optional<UserAuth> getUserById(Long id) {
        if (id == null)
            return Optional.empty();
        return userRepo.findById(id);
    }

    @Override
    public UserAuth updateUser(UserAuth user) {
        return userRepo.save(user);
    }

    @Override
    public Optional<UserAuth> getUserByRefferalCode(String refferalCodeFreind) {
        return userRepo.findByReferralCode(refferalCodeFreind);
    }

    @Override
    public Boolean checkByUsernameAndPassword(String username, String password) {
        return userRepo.existsByUsernameAndPassword(username, password);

    }

    public void updateResetPasswordToken(String token, String email) throws UserAuthNotFoundException {
        Optional<UserAuth> user = userRepo.findByUsername(email);
        if (user.isPresent()) {
            user.map(e -> {
                e.setResetPasswordToken(token);
                return userRepo.save(e);
            });
        } else {
            throw new UserAuthNotFoundException("Could not find any customer with the email " + email);
        }
    }

    public UserAuth getByResetPasswordToken(String token) {
        return userRepo.findByResetPasswordToken(token);
    }

    public void updatePassword(UserAuth user, String newPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setResetPasswordToken(null);
        userRepo.save(user);
    }

    public List<LogTransaction> getTransactionHistory(Long userId) {
        return logTransactionRepo.findByUserIdOrderByTransactionDateDesc(userId);
    }

    private final UserCounterRepo userCounterRepo;

    private final com.earn.earnmoney.Service.CounterService counterService;

    public UserProfileResponse getProfile(UserAuth user) {
        List<UserCounter> counters = userCounterRepo.findByUser(user);

        if (counters.isEmpty()) {
            counterService.assignFreeCounterToUser(user);
            counters = userCounterRepo.findByUser(user);
        }

        return UserProfileResponse.from(user, counters);
    }

    @Override
    public UserAuth updateProfileImage(Long userId, MultipartFile file) throws IOException {
        if (userId == null)
            throw new RuntimeException("User ID is required");
        UserAuth user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Image image = new Image();
        image.setName(file.getOriginalFilename());
        image.setType(file.getContentType());
        image.setImage(ImageUtilities.compressImage(file.getBytes()));
        user.setProfileImage(image);
        return userRepo.save(user);
    }

    @Override
    public Page<UserAuth> findByReferralCodeFriend(String referralCodeFriend, Pageable pageable) {
        return userRepo.findByReferralCodeFriend(referralCodeFriend, pageable);
    }

    @Override
    public List<UserAuth> findAllByReferralCodeFriend(String referralCodeFriend) {
        // Get all users with this referralCodeFriend without pagination
        return userRepo.findByReferralCodeFriend(referralCodeFriend, Pageable.unpaged()).getContent();
    }

}
