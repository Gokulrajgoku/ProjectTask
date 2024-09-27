package com.gokulraj.localstorage.repository;


import com.gokulraj.localstorage.model.FileMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {
    Optional<FileMetadata> findByFilename(String filename);

    void deleteByFilename(String filename);
}

