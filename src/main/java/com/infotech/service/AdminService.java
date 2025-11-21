package com.infotech.service;

import java.util.List;

import com.infotech.entity.AdminEntity;

public interface AdminService {

    String register(AdminEntity adminEntity);

    public String login(AdminEntity adminEntity);

    List<AdminEntity> getAdmin();

    AdminEntity updateCategory(Long id, AdminEntity admindat);
}
