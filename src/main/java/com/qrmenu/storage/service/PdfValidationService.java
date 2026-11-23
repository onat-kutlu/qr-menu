package com.qrmenu.storage.service;

import com.qrmenu.common.exception.BadRequestException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

@Service
public class PdfValidationService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_PAGE_COUNT = 30;

    public ValidationResult validate(Path path) {
        try {
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new BadRequestException("Uploaded PDF file was not found");
            }
            long fileSize = Files.size(path);
            if (fileSize <= 0) {
                throw new BadRequestException("Uploaded PDF file is empty");
            }
            if (fileSize > MAX_FILE_SIZE) {
                throw new BadRequestException("Uploaded PDF file exceeds the 10MB limit");
            }
            try (InputStream input = Files.newInputStream(path)) {
                byte[] header = input.readNBytes(5);
                if (header.length < 4 || header[0] != '%' || header[1] != 'P' || header[2] != 'D' || header[3] != 'F') {
                    throw new BadRequestException("Uploaded file is not a valid PDF");
                }
            }
            try (PDDocument document = Loader.loadPDF(path.toFile())) {
                if (document.isEncrypted()) {
                    throw new BadRequestException("Encrypted PDF files are not allowed");
                }
                int pageCount = document.getNumberOfPages();
                if (pageCount > MAX_PAGE_COUNT) {
                    throw new BadRequestException("Uploaded PDF exceeds the 30-page limit");
                }
                return new ValidationResult(fileSize, pageCount, sha256(path));
            }
        } catch (BadRequestException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            throw new BadRequestException("Uploaded PDF is corrupted or could not be read");
        }
    }

    private String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            StringBuilder result = new StringBuilder();
            for (byte value : digest.digest()) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    public record ValidationResult(long fileSizeBytes, int pageCount, String sha256) {
    }
}
