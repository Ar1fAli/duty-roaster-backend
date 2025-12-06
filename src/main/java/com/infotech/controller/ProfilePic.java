package com.infotech.controller;

import com.infotech.service.CloudinaryService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("imaged") MultipartFile imaged) throws Exception {

        String url = cloudinaryService.uploadFile(imaged);
        return ResponseEntity.ok(url);
    }

}
