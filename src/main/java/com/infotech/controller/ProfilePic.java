package com.infotech.controller;

import java.time.LocalDateTime;

import com.infotech.entity.Category;
import com.infotech.entity.ProfilePicture;
import com.infotech.repository.CategoryRepository;
import com.infotech.repository.ProfilePictureRepository;
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
public class ProfilePic {

    private final CloudinaryService cloudinaryService;
    private final ProfilePictureRepository pictureRepository;
    private final CategoryRepository categoryRepository;

    @PostMapping(value = "/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("imaged") MultipartFile imaged, @PathVariable Long id)
            throws Exception {

        ProfilePicture pic = new ProfilePicture();
        pic.setUpdateTime(LocalDateTime.now());

        String url = cloudinaryService.uploadFile(imaged);
        pic.setUrl(url);
        pictureRepository.save(pic);

        Category cate = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("vip not found"));
        cate.setPic(pic);
        categoryRepository.save(cate);

        return ResponseEntity.ok(url);
    }

}
