
package com.infotech.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryService {

  private final Cloudinary cloudinary;

  public CloudinaryService(Cloudinary cloudinary) {
    this.cloudinary = cloudinary;
  }

  public String uploadFile(MultipartFile file) throws IOException {

    String imageName = "user_profile_" + UUID.randomUUID();

    Map uploadResult = cloudinary.uploader().upload(
        file.getBytes(),
        ObjectUtils.asMap(
            "public_id", imageName, // 👈 IMAGE NAME
            "folder", "users", // 👈 OPTIONAL FOLDER
            "overwrite", true,
            "resource_type", "image"));
    // System.out.println(uploadResult);

    // Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
    // ObjectUtils.asMap("resource_type", "auto"));
    //
    return uploadResult.get("secure_url").toString();
  }
}
