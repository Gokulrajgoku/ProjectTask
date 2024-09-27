package com.gokulraj.localstorage.service;

import com.gokulraj.localstorage.model.FileMetadata;
import com.gokulraj.localstorage.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileStorageService {


    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }


    public void save(MultipartFile file, String uploader) {
        try {
            Path root = Paths.get(uploadDir);
            Files.copy(file.getInputStream(), root.resolve(file.getOriginalFilename()));

            long fileSize = file.getSize();
            String fileType = file.getContentType();

            FileMetadata fileMetadata = new FileMetadata(file.getOriginalFilename(), uploader, fileSize, fileType);
            fileMetadataRepository.save(fileMetadata);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    public void save(MultipartFile file) {
        try {
            Path root = Paths.get(uploadDir);
            Files.copy(file.getInputStream(), root.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(Paths.get(uploadDir), 1)
                    .filter(path -> !path.equals(Paths.get(uploadDir)))
                    .map(Paths.get(uploadDir)::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    public Path load(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    public void delete(String filename) throws IOException {
        Path file = load(filename);
        Files.delete(file);
    }
}

