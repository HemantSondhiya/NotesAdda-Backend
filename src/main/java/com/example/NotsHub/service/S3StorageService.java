package com.example.NotsHub.service;

import com.example.NotsHub.exceptions.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3StorageService.class);
    private static final long MAX_PDF_SIZE_BYTES = 10L * 1024L * 1024L;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final int presignedExpiryMinutes;

    public S3StorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket}") String bucketName,
            @Value("${aws.s3.presigned-expiry-minutes:5}") int presignedExpiryMinutes) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.presignedExpiryMinutes = presignedExpiryMinutes;
    }

    public UploadResult uploadPdf(MultipartFile file, String uploaderUsername) {
        ensureBucketConfigured();
        validatePdf(file);

        String extension = ".pdf";
        String key = "notes/" + uploaderUsername + "/" + UUID.randomUUID() + extension;
        return uploadPdfBytesInternal(file, file.getSize(), key);
    }

    public UploadResult uploadPdfBytes(byte[] bytes, String uploaderUsername) {
        ensureBucketConfigured();
        validatePdfBytes(bytes);

        String key = "notes/" + uploaderUsername + "/" + UUID.randomUUID() + ".pdf";
        return uploadPdfBytesInternal(bytes, key);
    }

    public UploadResult uploadPdfToPendingBytes(byte[] bytes, String uploaderUsername) {
        ensureBucketConfigured();
        validatePdfBytes(bytes);

        String key = "pending/" + uploaderUsername + "/" + UUID.randomUUID() + ".pdf";
        return uploadPdfBytesInternal(bytes, key);
    }

    private UploadResult uploadPdfBytesInternal(MultipartFile file, long contentLength, String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/pdf")
                .contentDisposition("inline")
                .build();

        try {
            try (var inputStream = file.getInputStream()) {
                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read uploaded file", e);
            throw new APIException("Failed to read uploaded file");
        } catch (Exception e) {
            LOGGER.error("Failed to upload PDF to S3: bucket={}, key={}", bucketName, key, e);
            throw new APIException("Failed to upload PDF to S3");
        }

        String fileUrl = "s3://" + bucketName + "/" + key;
        return new UploadResult(key, fileUrl);
    }

    private UploadResult uploadPdfBytesInternal(byte[] bytes, String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/pdf")
                .contentDisposition("inline")
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            LOGGER.error("Failed to upload PDF to S3: bucket={}, key={}", bucketName, key, e);
            throw new APIException("Failed to upload PDF to S3");
        }

        String fileUrl = "s3://" + bucketName + "/" + key;
        return new UploadResult(key, fileUrl);
    }

    /**
     * Upload PDF to the pending/ prefix (for normal users awaiting approval).
     */
    public UploadResult uploadPdfToPending(MultipartFile file, String uploaderUsername) {
        ensureBucketConfigured();
        validatePdf(file);

        String key = "pending/" + uploaderUsername + "/" + UUID.randomUUID() + ".pdf";
        return uploadPdfBytesInternal(file, file.getSize(), key);
    }

    /**
     * Move a file from pending/ prefix to notes/ prefix on approval.
     * Returns the new UploadResult with the approved key.
     */
    public UploadResult moveToApproved(String pendingKey, String uploaderUsername) {
        ensureBucketConfigured();
        if (pendingKey == null || pendingKey.isBlank()) {
            throw new APIException("Pending file key is required");
        }

        String approvedKey = "notes/" + uploaderUsername + "/" + UUID.randomUUID() + ".pdf";

        try {
            // Copy from pending to notes
            s3Client.copyObject(software.amazon.awssdk.services.s3.model.CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(pendingKey)
                    .destinationBucket(bucketName)
                    .destinationKey(approvedKey)
                    .build());

            // Delete the pending file
            deleteFile(pendingKey);
        } catch (Exception e) {
            LOGGER.error("Failed to move file from pending to approved: pendingKey={}, approvedKey={}", pendingKey, approvedKey, e);
            throw new APIException("Failed to approve file upload");
        }

        String fileUrl = "s3://" + bucketName + "/" + approvedKey;
        return new UploadResult(approvedKey, fileUrl);
    }

    public String createPresignedDownloadUrl(String key, String downloadFileName) {
        ensureBucketConfigured();
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .responseContentType("application/pdf")
                    .responseContentDisposition("attachment; filename=\"" + sanitizeFileName(downloadFileName) + "\"")
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignedExpiryMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            LOGGER.error("Failed to create presigned download URL: bucket={}, key={}", bucketName, key, e);
            throw new APIException("Failed to create download link");
        }
    }

    public void deleteFile(String key) {
        if (bucketName == null || bucketName.isBlank() || key == null || key.isBlank()) {
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (Exception ex) {
            LOGGER.warn("Failed to delete S3 object key={} bucket={}", key, bucketName, ex);
        }
    }

    public String extractManagedKeyFromS3Url(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || bucketName == null || bucketName.isBlank()) {
            return null;
        }
        String prefix = "s3://" + bucketName + "/";
        if (!fileUrl.startsWith(prefix)) {
            return null;
        }
        String key = fileUrl.substring(prefix.length());
        return key.isBlank() ? null : key;
    }

    public int getPresignedExpiryMinutes() {
        return presignedExpiryMinutes;
    }

    private void ensureBucketConfigured() {
        if (bucketName == null || bucketName.isBlank()) {
            throw new APIException("S3 bucket is not configured");
        }
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new APIException("PDF file is required");
        }
        if (file.getSize() > MAX_PDF_SIZE_BYTES) {
            throw new APIException("PDF size must be 10MB or less");
        }
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new APIException("Only PDF files are allowed");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.equalsIgnoreCase("application/pdf")) {
            throw new APIException("Invalid file type. Expected application/pdf");
        }
        try (var inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(5);
            String signature = new String(header, StandardCharsets.US_ASCII);
            if (!"%PDF-".equals(signature)) {
                throw new APIException("Invalid PDF file content");
            }
        } catch (IOException e) {
            throw new APIException("Failed to validate uploaded file");
        }
    }

    private void validatePdfBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new APIException("PDF file is required");
        }
        if (bytes.length > MAX_PDF_SIZE_BYTES) {
            throw new APIException("PDF size must be 10MB or less");
        }
        if (bytes.length < 5) {
            throw new APIException("Invalid PDF file content");
        }
        String signature = new String(bytes, 0, 5, StandardCharsets.US_ASCII);
        if (!"%PDF-".equals(signature)) {
            throw new APIException("Invalid PDF file content");
        }
    }

    private String sanitizeFileName(String fileName) {
        String baseName = (fileName == null || fileName.isBlank()) ? "notes.pdf" : fileName;
        String normalized = baseName.replaceAll("[\\r\\n\\\\\"]", "_");
        return normalized.endsWith(".pdf") ? normalized : normalized + ".pdf";
    }

    public UploadResult uploadImage(MultipartFile file, String folderPrefix) {
        ensureBucketConfigured();
        validateImage(file);

        if (folderPrefix == null || folderPrefix.isBlank()) {
            folderPrefix = "images";
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String key = folderPrefix + "/" + UUID.randomUUID() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentDisposition("inline")
                .build();

        try {
            try (var inputStream = file.getInputStream()) {
                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read uploaded image", e);
            throw new APIException("Failed to read uploaded image");
        } catch (Exception e) {
            LOGGER.error("Failed to upload image to S3: bucket={}, key={}", bucketName, key, e);
            throw new APIException("Failed to upload image to S3");
        }

        String fileUrl = "s3://" + bucketName + "/" + key;
        return new UploadResult(key, fileUrl);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new APIException("Image file is required");
        }
        if (file.getSize() > 5L * 1024L * 1024L) {
            throw new APIException("Image size must be 5MB or less");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.startsWith("image/"))) {
            throw new APIException("Only image files are allowed. Expected image/*");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public record UploadResult(String fileKey, String fileUrl) {
    }
}
