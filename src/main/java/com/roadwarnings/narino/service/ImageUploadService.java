package com.roadwarnings.narino.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.roadwarnings.narino.dto.response.ImageUploadResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final Cloudinary cloudinary;

    public ImageUploadResponseDTO uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, "road-warnings");
    }

    public ImageUploadResponseDTO uploadImage(MultipartFile file, String folder) throws IOException {
        log.info("Subiendo imagen a Cloudinary: {}", file.getOriginalFilename());

        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "transformation", new com.cloudinary.Transformation()
                            .width(1200).height(1200).crop("limit").quality("auto")
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            log.info("Imagen subida exitosamente: {}", uploadResult.get("public_id"));

            return ImageUploadResponseDTO.builder()
                    .url((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .format((String) uploadResult.get("format"))
                    .width((Integer) uploadResult.get("width"))
                    .height((Integer) uploadResult.get("height"))
                    .bytes(((Number) uploadResult.get("bytes")).longValue())
                    .build();

        } catch (IOException e) {
            log.error("Error al subir imagen a Cloudinary: {}", e.getMessage());
            throw new IOException("Error al subir la imagen: " + e.getMessage(), e);
        }
    }

    public ImageUploadResponseDTO uploadBase64Image(String base64Image, String folder) throws IOException {
        log.info("Subiendo imagen base64 a Cloudinary");

        try {
            String uniqueFilename = "img_" + UUID.randomUUID();

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", uniqueFilename,
                    "resource_type", "image",
                    "transformation", new com.cloudinary.Transformation()
                            .width(1200).height(1200).crop("limit").quality("auto")
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(base64Image, uploadParams);

            log.info("Imagen base64 subida exitosamente: {}", uploadResult.get("public_id"));

            return ImageUploadResponseDTO.builder()
                    .url((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .format((String) uploadResult.get("format"))
                    .width((Integer) uploadResult.get("width"))
                    .height((Integer) uploadResult.get("height"))
                    .bytes(((Number) uploadResult.get("bytes")).longValue())
                    .build();

        } catch (IOException e) {
            log.error("Error al subir imagen base64 a Cloudinary: {}", e.getMessage());
            throw new IOException("Error al subir la imagen: " + e.getMessage(), e);
        }
    }

    public void deleteImage(String publicId) {
        try {
            log.info("Eliminando imagen de Cloudinary: {}", publicId);
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Imagen eliminada: {}", result.get("result"));
        } catch (IOException e) {
            log.error("Error al eliminar imagen de Cloudinary: {}", e.getMessage());
        }
    }

    public String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }

        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String afterUpload = parts[1];
            String[] pathParts = afterUpload.split("/");

            if (pathParts.length < 2) {
                return null;
            }

            StringBuilder publicId = new StringBuilder();
            for (int i = 1; i < pathParts.length; i++) {
                if (i > 1) {
                    publicId.append("/");
                }
                publicId.append(pathParts[i]);
            }

            String result = publicId.toString();
            int lastDot = result.lastIndexOf('.');
            if (lastDot > 0) {
                result = result.substring(0, lastDot);
            }

            return result;

        } catch (Exception e) {
            log.error("Error al extraer publicId de la URL: {}", e.getMessage());
            return null;
        }
    }
}
