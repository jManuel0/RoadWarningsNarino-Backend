package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.response.ImageUploadResponseDTO;
import com.roadwarnings.narino.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @PostMapping("/upload")
    public ResponseEntity<ImageUploadResponseDTO> uploadImage(
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "road-warnings") String folder) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            ImageUploadResponseDTO response = imageUploadService.uploadImage(file, folder);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload/base64")
    public ResponseEntity<ImageUploadResponseDTO> uploadBase64Image(
            @RequestBody String base64Image,
            @RequestParam(defaultValue = "road-warnings") String folder) {

        try {
            ImageUploadResponseDTO response = imageUploadService.uploadBase64Image(base64Image, folder);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteImage(@PathVariable String publicId) {
        imageUploadService.deleteImage(publicId);
        return ResponseEntity.noContent().build();
    }
}
