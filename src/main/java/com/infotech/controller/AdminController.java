package com.infotech.controller;

import com.infotech.dto.AdminRequestDto;
import com.infotech.dto.AdminResponseProfile;
import com.infotech.dto.LoginResponse;
import com.infotech.dto.Logindat;
import com.infotech.entity.AdminEntity;
import com.infotech.service.AdminService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
public class AdminController {

  private final AdminService adminService;

  @PostMapping("/register")
  String register(@RequestBody AdminRequestDto adminEntity) {
    String res = adminService.register(adminEntity);
    return res;
  }

  // @GetMapping("/profile")
  // Optional<AdminEntity> getAdmin(@RequestBody String userName) {
  // Optional<AdminEntity> admin = adminService.getAdmin(userName);
  // return admin;
  // }@RequestParam String userName

  @GetMapping("/profile")
  public ResponseEntity<AdminResponseProfile> getAdmin(@RequestParam String userName) {
    // String userName = authentication.getName(); // or from principal
    AdminEntity admin = adminService.getAdmin(userName)
        .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
    AdminResponseProfile profile = new AdminResponseProfile();
    profile.setId(admin.getId());
    profile.setAdminName(admin.getAdminName());
    profile.setAdminUsername(admin.getAdminUsername());
    profile.setAdminEmail(admin.getAdminEmail());
    profile.setContactNo(admin.getContactNo());
    profile.setRole(admin.getRole());
    if (admin.getPic() != null) {
      System.out.println("null invocked");
      profile.setUrl(admin.getPic().getUrl());
    }
    return ResponseEntity.ok(profile);
  }

  @PostMapping("/login")
  public LoginResponse login(@RequestBody Logindat adminEntity) {
    return adminService.login(adminEntity);
  }

  @PostMapping("/update/{id}/{role}")
  public AdminEntity updateCategory(@PathVariable Long id, @RequestBody AdminEntity admindat,
      @PathVariable String role) {

    return adminService.updateCategory(id, admindat, role);
  }
}
