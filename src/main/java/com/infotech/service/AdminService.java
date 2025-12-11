package com.infotech.service;

import java.util.Optional;

import com.infotech.dto.AdminRequestDto;
import com.infotech.dto.LoginResponse;
import com.infotech.dto.Logindat;
import com.infotech.entity.AdminEntity;

public interface AdminService {

  String register(AdminRequestDto adminEntity);

  LoginResponse login(Logindat adminEntity);

  Optional<AdminEntity> getAdmin(String userName);

  // AdminEntity updateCategory(Long id, AdminEntity admindat);

  String updateCategory(Long id, AdminRequestDto admindat, String operatedBy);
}
