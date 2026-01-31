package com.urlshortener.controller;

import com.urlshortener.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor
@Tag(name = "QR Codes", description = "QR code generation and retrieval endpoints")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get QR code", description = "Returns the QR code image for a URL")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String shortCode) {
        byte[] qrCodeBytes = qrCodeService.getQrCodeBytes(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCodeBytes.length);
        headers.set("Content-Disposition", "inline; filename=\"" + shortCode + "_qr.png\"");

        return new ResponseEntity<>(qrCodeBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{shortCode}/download")
    @Operation(summary = "Download QR code", description = "Downloads the QR code image for a URL")
    public ResponseEntity<byte[]> downloadQrCode(@PathVariable String shortCode) {
        byte[] qrCodeBytes = qrCodeService.getQrCodeBytes(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCodeBytes.length);
        headers.set("Content-Disposition", "attachment; filename=\"" + shortCode + "_qr.png\"");

        return new ResponseEntity<>(qrCodeBytes, headers, HttpStatus.OK);
    }
}
