package com.infotech.service;

import java.util.List;

import com.infotech.entity.AdminEntity;
import com.infotech.repository.AdminRepsitory;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImp implements AdminService {

    private final AdminRepsitory adminRepsitory;

    @Override
    public AdminEntity addAdmin(AdminEntity adminEntity) {
        AdminEntity admin = adminRepsitory.save(adminEntity);
        return admin;
    }

    @Override
    public List<AdminEntity> getAdmin() {
        List<AdminEntity> admindata = adminRepsitory.findAll();
        return admindata;
    }

}
