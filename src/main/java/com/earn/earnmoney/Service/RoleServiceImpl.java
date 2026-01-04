package com.earn.earnmoney.Service;

import com.earn.earnmoney.model.ERole;
import com.earn.earnmoney.model.Role;
import com.earn.earnmoney.repo.RoleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepo roleRepo;


    @Override
    public Optional<Role> findByName(ERole name) {
        return roleRepo.findByName(name);
    }

    @Override
    public void save(Role role) {
        roleRepo.save(role);
    }

}
