
package com.infotech.service;

import com.infotech.entity.AdminEntity;
import com.infotech.repository.AdminRepsitory;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepsitory adminRepsitory;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminEntity admin = adminRepsitory.findByAdminUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(admin.getAdminUsername())
                .password(admin.getAdminPassword()) // must be BCrypt hash
                .roles("ADMIN")
                .build();
    }
}
