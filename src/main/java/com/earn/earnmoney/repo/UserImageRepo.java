package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.UserImage;
import com.earn.earnmoney.model.Withdraw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserImageRepo extends JpaRepository<UserImage, Long> {


    @Query("SELECT r.id ,r.username,r.userImage.id FROM UserImage r  where  r.username LIKE %:query% ")
    Page<UserImage> findAllUserImagePage(@Param("query") String query, Pageable pageable);


    Optional<UserImage> findByUsername(String username);
    @Query("SELECT r.userImage.id FROM UserImage r where r.username LIKE %:query%")
    Optional<UserImage> findQuery(@Param("query") String query);

}
