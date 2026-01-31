package com.urlshortener.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class QrCodeService {

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String QR_CODE_DIR = "uploads/qrcodes";
    private static final int QR_CODE_SIZE = 300;

    public QrCodeService() {
        try {
            Files.createDirectories(Paths.get(QR_CODE_DIR));
        } catch (IOException e) {
            log.error("Could not create QR code directory", e);
        }
    }

    public String generateQrCode(String url, String shortCode) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    url,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_SIZE,
                    QR_CODE_SIZE,
                    hints);

            String fileName = shortCode + "_qr.png";
            Path filePath = Paths.get(QR_CODE_DIR, fileName);

            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

            log.info("QR code generated for: {}", shortCode);
            return filePath.toString();

        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for {}: {}", shortCode, e.getMessage());
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public Resource loadQrCode(String shortCode) {
        try {
            String fileName = shortCode + "_qr.png";
            Path filePath = Paths.get(QR_CODE_DIR, fileName);

            if (!Files.exists(filePath)) {
                throw new RuntimeException("QR code not found for: " + shortCode);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            throw new RuntimeException("Could not read QR code file");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to load QR code", e);
        }
    }

    public byte[] getQrCodeBytes(String shortCode) {
        try {
            String fileName = shortCode + "_qr.png";
            Path filePath = Paths.get(QR_CODE_DIR, fileName);

            if (!Files.exists(filePath)) {
                String url = baseUrl + "/r/" + shortCode;
                generateQrCode(url, shortCode);
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read QR code", e);
        }
    }

    public void deleteQrCode(String shortCode) {
        try {
            String fileName = shortCode + "_qr.png";
            Path filePath = Paths.get(QR_CODE_DIR, fileName);
            Files.deleteIfExists(filePath);
            log.info("QR code deleted for: {}", shortCode);
        } catch (IOException e) {
            log.error("Failed to delete QR code for {}: {}", shortCode, e.getMessage());
        }
    }
}
