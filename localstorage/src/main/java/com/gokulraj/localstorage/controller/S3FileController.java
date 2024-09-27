package com.gokulraj.localstorage.controller;

import com.gokulraj.localstorage.model.FileMetadata;
import com.gokulraj.localstorage.repository.FileMetadataRepository;
import com.gokulraj.localstorage.service.S3FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/S3/files")
public class S3FileController {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    @Autowired
    private S3FileStorageService fileStorageService;
    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    public S3FileController(S3Client s3Client) {
        this.s3Client = s3Client;
    }


    // Upload file (Allowed for ADMIN and MANAGER)
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<FileMetadata> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        fileStorageService.save(file);

        // Create and save file metadata
        FileMetadata metadata = new FileMetadata();
        metadata.setFilename(file.getOriginalFilename());
        metadata.setUploader(authentication.getName());
        metadata.setFileSize(file.getSize());
        metadata.setFileType(file.getContentType());
        fileMetadataRepository.save(metadata);

        return ResponseEntity.ok(metadata);
    }

    // List all files (Allowed for all roles)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DEVELOPER')")
    public ResponseEntity<List<FileMetadata>> listAllFiles() {
        // Fetch all file metadata from the repository and return as a list
        List<FileMetadata> fileDetails = fileMetadataRepository.findAll().stream()
                .map(metadata -> {
                    FileMetadata metadataWithoutId = new FileMetadata();
                    metadataWithoutId.setFilename(metadata.getFilename());
                    metadataWithoutId.setUploader(metadata.getUploader());
                    metadataWithoutId.setUploadTime(metadata.getUploadTime());
                    metadataWithoutId.setFileSize(metadata.getFileSize());
                    metadataWithoutId.setFileType(metadata.getFileType());
                    return metadataWithoutId;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(fileDetails); // Return the list of file details
    }



    @GetMapping("/download/{filename:.+}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DEVELOPER')")
    public ResponseEntity<?> downloadFile(@PathVariable String filename) {
        try {
            // Retrieve the file from S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            // Get the InputStream for the file in S3
            ResponseInputStream<GetObjectResponse> s3ObjectInputStream = s3Client.getObject(getObjectRequest);

            // Convert the InputStream to InputStreamResource
            InputStreamResource resource = new InputStreamResource(s3ObjectInputStream);

            // Return the response with the file content
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (S3Exception e) {
            // Return an error response if the file can't be found or any other S3 exception occurs
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: File not found or could not be read.");
        }
    }


    // Delete file (Allowed only for ADMIN)
    @DeleteMapping("/delete/{filename:.+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        try{
        fileStorageService.delete(filename);
        fileMetadataRepository.deleteByFilename(filename);

        return ResponseEntity.ok("File deleted successfully: " + filename);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body("File not found (or) Could not delete file: " + filename);
        }
    }
}

