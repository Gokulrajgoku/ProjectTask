package com.gokulraj.localstorage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3FileStorageService {

    private final S3Client s3Client;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3FileStorageService(@Value("${aws.s3.region}") String region) {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    public void save(MultipartFile file) throws IOException {
        String key = file.getOriginalFilename();
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.putObject(putRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));
    }

    public void delete(String filename) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();
        s3Client.deleteObject(deleteRequest);
    }

    public List<String> loadAll() {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
        return s3Client.listObjectsV2(listRequest)
                .contents()
                .stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }
}
