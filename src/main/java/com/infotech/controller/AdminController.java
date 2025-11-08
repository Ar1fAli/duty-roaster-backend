package com.infotech.controller;

import java.util.List;

import com.infotech.entity.AdminEntity;
import com.infotech.service.AdminService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.29.45:3000")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    String register(@RequestBody AdminEntity adminEntity) {
        String res = adminService.register(adminEntity);
        return res;
    }

    @GetMapping("/register")
    List<AdminEntity> getAdmin() {
        List<AdminEntity> admin = adminService.getAdmin();
        return admin;
    }

    @PostMapping("/login")
    public String login(@RequestBody AdminEntity adminEntity) {
        return adminService.login(adminEntity);
    }

}
