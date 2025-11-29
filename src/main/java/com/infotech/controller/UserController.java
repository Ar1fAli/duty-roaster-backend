package com.infotech.controller;

import java.util.Optional;

import com.infotech.entity.UserEntity;
import com.infotech.repository.UserRepository;

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
    public Optional<UserEntity> getAdmin(@RequestParam String username) {
        Optional<UserEntity> admindata = userRepository.findByUsername(username);
        return admindata;
    }

    @PutMapping("/{id}")
    public UserEntity updateCategory(@PathVariable Long id, @RequestBody UserEntity updateduser) {
        return userRepository.findById(id).map(userEnt -> {
            userEnt.setId(updateduser.getId());
            userEnt.setName(updateduser.getName());
            userEnt.setEmail(updateduser.getEmail());
            userEnt.setStatus(updateduser.getStatus());
            userEnt.setContactno(updateduser.getContactno());
            userEnt.setUsername(updateduser.getUsername());
            userEnt.setPassword(encoder.encode(updateduser.getPassword()));

            System.out.println("updated category p" + updateduser.getPassword());

            // Clear existing items (important for orphanRemoval = true)
            // category.getDataItems().clear();

            // Add updated items with proper category assignment
            // for (DataItem item : updatedCategory.getDataItems()) {
            // item.setCategory(category);
            // category.getDataItems().add(item);
            // }

            return userRepository.save(userEnt);
        }).orElseThrow(() -> new RuntimeException("Category not found with id " + id));
    }

}
