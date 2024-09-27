package com.gokulraj.localstorage.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "file_metadata")
public class FileMetadata {

    @Id
    private String id;
    private String filename;
    private String uploader;
    private LocalDateTime uploadTime;
    private long fileSize;
    private String fileType;


    public FileMetadata(String filename, String uploader, long fileSize, String fileType) {
        this.filename = filename;
        this.uploader = uploader;
        this.uploadTime = LocalDateTime.now();
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

}
