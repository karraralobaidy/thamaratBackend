package com.earn.earnmoney.security.services;

import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.repo.UserAuthRepo;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@NoArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    UserAuthRepo userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserAuthRepo userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAuth user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        if (user.isDeleted()) {
            throw new UsernameNotFoundException("User is deleted");
        }

        return UserDetailsImpl.build(user);
    }

}
