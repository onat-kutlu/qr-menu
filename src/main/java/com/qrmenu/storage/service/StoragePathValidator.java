package com.qrmenu.storage.service;

import com.qrmenu.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class StoragePathValidator {

    private final Path storageRoot;

    public StoragePathValidator(@Value("${app.storage.path:storage}") String storagePath) {
        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();
    }

    public Path resolve(String suppliedPath) {
        if (suppliedPath == null || suppliedPath.isBlank()) {
            throw new BadRequestException("File path is required");
        }
        Path candidate = Path.of(suppliedPath);
        Path resolved = (candidate.isAbsolute() ? candidate : storageRoot.resolve(candidate))
                .toAbsolutePath().normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new BadRequestException("File path must stay inside application storage");
        }
        return resolved;
    }
}
