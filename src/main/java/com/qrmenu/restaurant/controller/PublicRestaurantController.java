package com.qrmenu.restaurant.controller;

import com.qrmenu.common.response.ApiResponse;
import com.qrmenu.common.util.ClientIpResolver;
import com.qrmenu.restaurant.dto.PublicMenuOpenResponse;
import com.qrmenu.restaurant.dto.PublicRestaurantResponse;
import com.qrmenu.restaurant.service.PublicRestaurantService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/public/menus")
@RequiredArgsConstructor
public class PublicRestaurantController {

    private final PublicRestaurantService publicRestaurantService;

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PublicRestaurantResponse>> getPublicRestaurantInfo(
            @PathVariable String slug,
            @RequestParam(required = false) String visitId,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = ClientIpResolver.resolve(request);
        return ResponseEntity.ok(ApiResponse.success("Restaurant info retrieved",
                publicRestaurantService.getPublicRestaurantInfo(slug, visitId, ipAddress, userAgent)));
    }

    @GetMapping("/{slug}/open")
    public ResponseEntity<ApiResponse<PublicMenuOpenResponse>> openMenu(
            @PathVariable String slug,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String visitId,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = ClientIpResolver.resolve(request);
        return ResponseEntity.ok(ApiResponse.success("Menu opened",
                publicRestaurantService.openMenu(slug, lang, visitId, ipAddress, userAgent)));
    }

    @GetMapping("/{slug}/pdf")
    public ResponseEntity<Resource> openPdf(
            @PathVariable String slug,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String visitId,
            HttpServletRequest request) throws java.io.IOException {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = ClientIpResolver.resolve(request);
        Path pdfPath = publicRestaurantService.getLivePdfPath(slug, lang, visitId, ipAddress, userAgent);
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(pdfPath));
        String normalizedLanguage = lang == null || lang.isBlank() ? "tr" : lang.toLowerCase(java.util.Locale.ROOT);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentLength(Files.size(pdfPath));
        headers.setContentDisposition(ContentDisposition.inline()
                .filename(slug + "-" + normalizedLanguage + ".pdf")
                .build());
        headers.setCacheControl("private, no-store, max-age=0");
        return ResponseEntity.ok().headers(headers).body(resource);
    }

        @GetMapping("/{slug}/logo")
        public ResponseEntity<Resource> logo(@PathVariable String slug) throws java.io.IOException {
                Path logoPath = publicRestaurantService.getLogoPath(slug);
                InputStreamResource resource = new InputStreamResource(Files.newInputStream(logoPath));
                MediaType contentType = MediaTypeFactory.getMediaType(logoPath.getFileName().toString())
                                .orElse(MediaType.APPLICATION_OCTET_STREAM);
                return ResponseEntity.ok().contentType(contentType).cacheControl(org.springframework.http.CacheControl.noCache()).body(resource);
        }
}
