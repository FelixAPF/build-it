package com.build_it.buildit.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

  // This will create a folder named "uploads" in your Spring Boot root directory
  private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

  public FileStorageService() {
    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception ex) {
      throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
    }
  }

  public Resource loadFileAsResource(String fileName) {
    try {
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists()) {
        return resource;
      } else {
        throw new RuntimeException("File not found " + fileName);
      }
    } catch (Exception ex) {
      throw new RuntimeException("File not found " + fileName, ex);
    }
  }

  public String storeFile(MultipartFile file, String userEmail) {
    try {
      // Clean the email to make it a safe filename (e.g. felix@test.com -> felix_test_com)
      String safeEmail = userEmail.replace("@", "_").replace(".", "_");

      // Extract original extension (e.g. .pdf, .jpg)
      String originalFilename = file.getOriginalFilename();
      String fileExtension = "";
      if (originalFilename != null && originalFilename.contains(".")) {
        fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
      }

      // Format: ID_felix_test_com_168439201.pdf
      String fileName = "ID_" + safeEmail + "_" + System.currentTimeMillis() + fileExtension;

      // Copy file to the target location (Replacing existing file with the same name)
      Path targetLocation = this.fileStorageLocation.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      return fileName;
    } catch (IOException ex) {
      throw new RuntimeException("Could not store file. Please try again!", ex);
    }
  }
}
