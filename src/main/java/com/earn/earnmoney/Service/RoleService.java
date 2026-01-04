package com.earn.earnmoney.Service;



import com.earn.earnmoney.model.ERole;
import com.earn.earnmoney.model.Role;

import java.util.Optional;

public interface RoleService {
    Optional<Role> findByName(ERole name);
    void save(Role app);
}
