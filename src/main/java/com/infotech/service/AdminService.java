package com.infotech.service;

import java.util.List;

import com.infotech.entity.AdminEntity;

public interface AdminService {

    AdminEntity addAdmin(AdminEntity adminEntity);

    List<AdminEntity> getAdmin();
}
