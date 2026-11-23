package com.qrmenu.storage;

import com.qrmenu.common.exception.BadRequestException;
import com.qrmenu.storage.service.PdfValidationService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PdfValidationServiceTest {

    private final PdfValidationService validator = new PdfValidationService();

    @Test
    void shouldValidatePdfAndDerivePageCountAndHash() throws Exception {
        Path pdf = Files.createTempFile("valid-menu", ".pdf");
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(pdf.toFile());
        }

        PdfValidationService.ValidationResult result = validator.validate(pdf);

        assertEquals(1, result.pageCount());
        assertEquals(64, result.sha256().length());
    }

    @Test
    void shouldRejectCorruptedPdf() throws Exception {
        Path pdf = Files.createTempFile("invalid-menu", ".pdf");
        Files.writeString(pdf, "%PDF-1.7\nnot-a-real-pdf");

        assertThrows(BadRequestException.class, () -> validator.validate(pdf));
    }
}
