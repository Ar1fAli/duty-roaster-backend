package com.infotech.service;

import java.util.Optional;

import jakarta.transaction.Transactional;

import com.infotech.dto.LoginResponse;
import com.infotech.dto.Logindat;
import com.infotech.entity.AdminEntity;
import com.infotech.repository.AdminRepsitory;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImp implements AdminService {

    private final AdminRepsitory adminRepsitory;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @Override
    public String register(AdminEntity adminEntity) {
        System.out.println(adminEntity.getAdminPassword());
        if (adminRepsitory.findByAdminUsername(adminEntity.getAdminUsername()).isPresent())
            return "Username already exists";

        adminEntity.setAdminPassword(encoder.encode(adminEntity.getAdminPassword()));
        adminRepsitory.save(adminEntity);
        return "User registered successfully";
    }

    @Override
    public Optional<AdminEntity> getAdmin(String userName) {
        Optional<AdminEntity> admindata = adminRepsitory.findByAdminUsername(userName);
        return admindata;
    }

    @Override
    public LoginResponse login(Logindat loginDto) {
        try {
            System.out.println(loginDto.getUsername());
            System.out.println(loginDto.getPassword());

            // This will call CustomUserDetailsService.loadUserByUsername()
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getUsername(),
                            loginDto.getPassword()));

            System.out.println("working");

            // Extract role from granted authorities
            String fullRole = authentication.getAuthorities()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No role found"))
                    .getAuthority(); // e.g. "ROLE_ADMIN"

            // Optional: strip "ROLE_"
            String role = fullRole.replace("ROLE_", ""); // ADMIN / GUARD / VIP

            LoginResponse res = new LoginResponse();
            // If your JWT should also contain role, adjust generateToken accordingly
            res.setData(jwtService.generateToken(loginDto.getUsername()));
            res.setRole(role);

            return res;

        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            throw new RuntimeException("Invalid username or password");
        }
    }

    // @Override
    // public LoginResponse login(Logindat adminEntity) {
    // try {
    // System.out.println(adminEntity.getUsername());
    // System.out.println(adminEntity.getPassword());
    //
    // authManager.authenticate(
    // new UsernamePasswordAuthenticationToken(
    // adminEntity.getUsername(),
    // adminEntity.getPassword()));
    //
    // System.out.println("working");
    // AdminEntity admin =
    // adminRepsitory.findByAdminUsername(adminEntity.getUsername())
    // .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    // LoginResponse res = new LoginResponse();
    // res.setData(jwtService.generateToken(adminEntity.getUsername()));
    // res.setRole(admin.getRole());
    // return res;
    //
    // } catch (Exception e) {
    // System.out.println("Login failed: " + e.getMessage());
    // throw new RuntimeException("Invalid username or password");
    // }
    // }

    public AdminEntity updateCategory(Long id, AdminEntity admindat) {

        return adminRepsitory.findById(id).map(admin -> {
            admin.setId(admindat.getId());
            admin.setAdminName(admindat.getAdminName());
            admin.setAdminUsername(admindat.getAdminUsername());
            admin.setAdminEmail(admindat.getAdminEmail());
            admin.setAdminPassword(encoder.encode(admindat.getAdminPassword()));
            admin.setContactNo(admindat.getContactNo());
            admin.setRole(admindat.getRole());

            return adminRepsitory.save(admin);
        }).orElseThrow(() -> new RuntimeException("Category not found with id " +
                id));
    }

}
