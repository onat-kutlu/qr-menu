package com.qrmenu.storage.service;

import com.qrmenu.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.imageio.ImageIO;

@Service
public class LocalFileStorageService {

    private final Path storageRoot;

    public LocalFileStorageService(@Value("${app.storage.path:storage}") String storagePath) {
        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();
    }

    public Path savePrivatePdf(MultipartFile file, Long restaurantId, String language) {
        String safeLanguage = language == null ? "" : language.toLowerCase();
        Path directory = storageRoot.resolve("menus/private").resolve(String.valueOf(restaurantId)).resolve(safeLanguage).normalize();
        if (!directory.startsWith(storageRoot) || file == null || file.isEmpty()) {
            throw new BadRequestException("Invalid PDF upload");
        }
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(UUID.randomUUID() + ".pdf").normalize();
            file.transferTo(target);
            return target;
        } catch (IOException ex) {
            throw new BadRequestException("Unable to store uploaded PDF");
        }
    }

    public Path saveLogo(MultipartFile file, Long restaurantId) {
        if (file == null || file.isEmpty() || file.getSize() > 2L * 1024 * 1024) {
            throw new BadRequestException("Logo must be present and smaller than 2MB");
        }
        try {
            if (ImageIO.read(file.getInputStream()) == null) {
                throw new BadRequestException("Logo must be a valid image");
            }
            String extension = switch (file.getContentType() == null ? "" : file.getContentType()) {
                case "image/png" -> ".png";
                case "image/jpeg" -> ".jpg";
                case "image/webp" -> ".webp";
                default -> throw new BadRequestException("Only PNG, JPEG or WEBP logos are allowed");
            };
            Path directory = storageRoot.resolve("logos").resolve(String.valueOf(restaurantId)).normalize();
            if (!directory.startsWith(storageRoot)) {
                throw new BadRequestException("Invalid logo path");
            }
            Files.createDirectories(directory);
            Path target = directory.resolve(UUID.randomUUID() + extension).normalize();
            file.transferTo(target);
            return target;
        } catch (IOException ex) {
            throw new BadRequestException("Unable to store logo");
        }
    }
}
