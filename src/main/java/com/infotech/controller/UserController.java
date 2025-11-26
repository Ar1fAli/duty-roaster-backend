package com.infotech.controller;

import com.infotech.entity.UserEntity;
import com.infotech.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping("/usr")
public class UserController {

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    @PostMapping("/reg")
    public UserEntity createUser(@RequestBody UserEntity usr) {
        usr.setPassword(encoder.encode(usr.getPassword()));
        return userRepository.save(usr);
    }

}
