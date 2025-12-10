package com.infotech.controller;

import java.time.LocalDateTime;

import com.infotech.entity.AdminEntity;
import com.infotech.entity.Category;
import com.infotech.entity.Officer;
import com.infotech.entity.ProfilePicture;
import com.infotech.entity.UserEntity;
import com.infotech.repository.AdminRepsitory;
import com.infotech.repository.CategoryRepository;
import com.infotech.repository.OfficerRepository;
import com.infotech.repository.ProfilePictureRepository;
import com.infotech.repository.UserRepository;
import com.infotech.service.CloudinaryService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/profile")
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProfilePictureController {

    private final CloudinaryService cloudinaryService;
    private final ProfilePictureRepository pictureRepository;
    private final CategoryRepository categoryRepository;
    private final OfficerRepository officerRepository;
    private final AdminRepsitory adminRepsitory;
    private final UserRepository useRepository;

    @PostMapping(value = "/upload/{id}/{role}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("imaged") MultipartFile imaged,
            @PathVariable Long id,
            @PathVariable String role) throws Exception {

        ProfilePicture pic = new ProfilePicture();
        pic.setUpdateTime(LocalDateTime.now());

        String url = cloudinaryService.uploadFile(imaged);
        pic.setUrl(url);
        pic.setUpdaterId(id);
        pic.setUpdatedby(role);
        pictureRepository.save(pic);

        System.out.println(role + " role is this ");

        // safer: ignore case & trim spaces
        String normalizedRole = role.trim();

        if ("guard".equalsIgnoreCase(normalizedRole)) {
            Officer offi = officerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("guard data not found"));
            offi.setPic(pic);
            officerRepository.save(offi);

            System.out.println(offi + " officer is saved");

        } else if ("vip".equalsIgnoreCase(normalizedRole)) {
            System.out.println("vip called ");
            Category cate = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("vip not found"));
            System.out.println(pic + " pic is this ");
            cate.setPic(pic);
            categoryRepository.save(cate);

            System.out.println(cate + " category is saved ");
        } else if ("admin".equalsIgnoreCase(normalizedRole)) {
            System.out.println("admin executed");
            AdminEntity admin = adminRepsitory.findById(id)
                    .orElseThrow(() -> new RuntimeException("Admin id is wrong correct admin please"));
            admin.setPic(pic);
            adminRepsitory.save(admin);
            System.out.println(admin);

        } else if ("user".equalsIgnoreCase(normalizedRole)) {
            System.out.println("admin executed");
            UserEntity admin = useRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Admin id is wrong correct admin please"));
            admin.setPic(pic);
            useRepository.save(admin);
            System.out.println(admin);

        }

        return ResponseEntity.ok(url);
    }
}
