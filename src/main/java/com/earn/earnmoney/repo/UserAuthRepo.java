package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.UserAuth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthRepo extends JpaRepository<UserAuth, Long> {
    String Find_users = "SELECT id, full_name , username , points,referral_code,active,band FROM usersauth ";
    String Find_userActive = "SELECT id, username,active FROM users ";

    @Query(value = Find_users, nativeQuery = true)
    List<Object[]> findUsersWithoutPassword();

    @Query("SELECT r FROM UserAuth r WHERE r.username LIKE %:query% OR r.full_name LIKE %:query% OR r.referralCode LIKE %:query% OR CAST(r.points AS string) LIKE %:query%")
    Page<UserAuth> findAllByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT r.full_name,r.username,r.points,r.active,r.band , r.id ,r.date FROM UserAuth r ")
    List<Object[]> getAllUser();

    Optional<UserAuth> findByUsername(String username);

    UserAuth findUserById(Long id);

    Boolean existsByUsername(String username);

    Boolean existsByUsernameAndActiveTrue(String username);

    Boolean existsByUsernameAndBandTrue(String username);

    Boolean existsByReferralCode(String referralCode);

    java.util.Optional<UserAuth> findById(@org.springframework.lang.NonNull Long id);

    UserAuth findByResetPasswordToken(String token);

    Optional<UserAuth> findByReferralCode(String referralCodeFriend);

    Boolean existsByUsernameAndPassword(String username, String password);

}
