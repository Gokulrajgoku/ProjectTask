package com.gokulraj.localstorage.controller;



import com.gokulraj.localstorage.model.FileMetadata;
import com.gokulraj.localstorage.repository.FileMetadataRepository;
import com.gokulraj.localstorage.service.FileStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        fileStorageService.init();  // Initialize storage directory
    }

    // Upload file (Allowed for ADMIN and MANAGER)
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<FileMetadata> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) {
            String userName = authentication.getName();
            fileStorageService.save(file, userName);
            FileMetadata savedMetadata = fileMetadataRepository.findByFilename(file.getOriginalFilename())
                    .orElseThrow(() -> new RuntimeException("File metadata not found"));

            return ResponseEntity.ok(savedMetadata);

    }

    // List all files (Allowed for all roles)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DEVELOPER')")
    public ResponseEntity<List<FileMetadata>> listAllFiles() {
        // Fetch all file metadata from the repository and return as a list
        List<FileMetadata> fileDetails = fileMetadataRepository.findAll().stream()
                .map(metadata -> {
                    // Create a copy of the metadata excluding the ID
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


    // Download file (Allowed for all roles)
    @GetMapping("/download/{filename:.+}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DEVELOPER')")
    public ResponseEntity<?> downloadFile(@PathVariable String filename) {
        try {
            Path file = fileStorageService.load(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(404).body("File not found: " + filename);  // Return 404 if file is not found or unreadable
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());  // Return 500 for malformed URL errors
        }
    }


    // Delete file (Allowed only for ADMIN)
    @DeleteMapping("/delete/{filename:.+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        try {
            fileStorageService.delete(filename);
            fileMetadataRepository.deleteByFilename(filename);
            return ResponseEntity.ok("File deleted successfully: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File not found (or) Could not delete file: " + filename);
        }
    }
}
