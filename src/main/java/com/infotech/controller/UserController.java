package com.infotech.controller;

import com.infotech.dto.UserProfileResponse;
import com.infotech.entity.UserEntity;
import com.infotech.repository.UserRepository;
import com.infotech.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/profile")
    public UserProfileResponse getAdmin(@RequestParam String username) {
        UserEntity admindata = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("user not found "));
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(admindata.getId());
        profile.setName(admindata.getName());
        profile.setUsername(admindata.getUsername());
        profile.setEmail(admindata.getEmail());
        profile.setContactno(admindata.getContactno());
        profile.setStatus(admindata.getStatus());
        if (admindata.getPic() != null) {
            profile.setUrl(admindata.getPic().getUrl());

        }

        return profile;
    }

    private final UserService userService;

    @PutMapping("/{id}/{role}")
    public ResponseEntity<UserEntity> updateUser(
            @PathVariable Long id,
            @RequestBody UserEntity updatedUser, @PathVariable String role) {

        // You can also take operatedBy from SecurityContext instead of hardcoding
        // String operatedBy = "SYSTEM"; // or current logged-in username

        try {
            UserEntity saved = userService.updateUser(id, updatedUser, role);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException ex) {
            // for "User not found" etc
            return ResponseEntity.notFound().build();
        }
    }

}
