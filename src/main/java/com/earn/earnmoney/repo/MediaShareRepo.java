package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.MediaShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaShareRepo extends JpaRepository<MediaShare, Long> {


    @Query("SELECT r.id ,r.name,r.Image.id FROM MediaShare r  where  r.name LIKE %:query% ")
    Page<MediaShare> findAllUserImagePage(@Param("query") String query, Pageable pageable);

    Optional<MediaShare> findByName(String username);
    @Query("SELECT r.Image.id FROM MediaShare r where r.name LIKE %:query%")
    Optional<MediaShare> findQuery(@Param("query") String query);

}
