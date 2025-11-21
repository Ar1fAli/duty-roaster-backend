package com.infotech.service;

import java.util.List;

import jakarta.transaction.Transactional;

import com.infotech.entity.AdminEntity;
import com.infotech.repository.AdminRepsitory;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    public List<AdminEntity> getAdmin() {
        List<AdminEntity> admindata = adminRepsitory.findAll();
        return admindata;
    }

    @Override
    public String login(AdminEntity adminEntity) {
        try {
            System.out.println(adminEntity.getAdminUsername());
            System.out.println(adminEntity.getAdminPassword());

            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            adminEntity.getAdminUsername(),
                            adminEntity.getAdminPassword()));

            System.out.println("working");
            return jwtService.generateToken(adminEntity.getAdminUsername());

        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            throw new RuntimeException("Invalid username or password");
        }
    }

    public AdminEntity updateCategory(Long id, AdminEntity admindat) {

        return adminRepsitory.findById(id).map(admin -> {
            admin.setAdminId(admindat.getId());
            admin.setAdminName(admindat.getAdminName());
            admin.setAdminUsername(admindat.getAdminUsername());
            admin.setAdminEmail(admindat.getAdminEmail());
            admin.setAdminPassword(encoder.encode(admindat.getAdminPassword()));
            admin.setContactNo(admindat.getContactNo());
            admin.setAdminStatus(admindat.getAdminStatus());

            return adminRepsitory.save(admin);
        }).orElseThrow(() -> new RuntimeException("Category not found with id " +
                id));
    }

}
