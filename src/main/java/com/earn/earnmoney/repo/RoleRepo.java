package com.earn.earnmoney.repo;


import com.earn.earnmoney.model.ERole;
import com.earn.earnmoney.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoleRepo extends JpaRepository<Role,Long> {
    Optional<Role> findByName(ERole name);
}
