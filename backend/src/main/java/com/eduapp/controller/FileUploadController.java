package com.eduapp.controller;

import com.eduapp.dto.FileUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${file.video-dir}")
    private String videoDir;
    
    @Value("${file.course-dir}")
    private String courseDir;
    
    @Value("${file.book-dir}")
    private String bookDir;
    
    @PostMapping("/video")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<FileUploadResponse> uploadVideo(@RequestParam("file") MultipartFile file) {
        return handleFileUpload(file, videoDir, "video");
    }
    
    @PostMapping("/course")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<FileUploadResponse> uploadCourse(@RequestParam("file") MultipartFile file) {
        return handleFileUpload(file, courseDir, "course");
    }
    
    @PostMapping("/book")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<FileUploadResponse> uploadBook(@RequestParam("file") MultipartFile file) {
        return handleFileUpload(file, bookDir, "book");
    }
    
    @PostMapping("/thumbnail")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<FileUploadResponse> uploadThumbnail(@RequestParam("file") MultipartFile file) {
        return handleFileUpload(file, uploadDir + "/thumbnails", "thumbnail");
    }
    
    @PostMapping("/resource")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<FileUploadResponse> uploadResource(@RequestParam("file") MultipartFile file) {
        return handleFileUpload(file, uploadDir + "/resources", "resource");
    }

    @PostMapping("/submission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponse> uploadSubmission(@RequestParam("file") MultipartFile file) {
        return handleFileUpload(file, uploadDir + "/submissions", "submission");
    }

    private ResponseEntity<FileUploadResponse> handleFileUpload(MultipartFile file, String directory, String type) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String originalFilename = file.getOriginalFilename();
            String safeOriginalFilename = originalFilename == null ? "upload" : Paths.get(originalFilename).getFileName().toString();
            String extension = "";
            int extensionIndex = safeOriginalFilename.lastIndexOf(".");
            if (extensionIndex >= 0) {
                extension = safeOriginalFilename.substring(extensionIndex);
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            Path filePath = Paths.get(directory, filename);
            Files.write(filePath, file.getBytes());
            
            return ResponseEntity.ok(new FileUploadResponse(
                    filename,
                    safeOriginalFilename,
                    file.getContentType(),
                    file.getSize(),
                    "/api/upload/" + type + "/" + filename
            ));
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/{type}/{filename}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String type,
            @PathVariable String filename) {
        try {
            String directory = switch (type) {
                case "video" -> videoDir;
                case "course" -> courseDir;
                case "book" -> bookDir;
                case "thumbnail" -> uploadDir + "/thumbnails";
                case "resource" -> uploadDir + "/resources";
                case "submission" -> uploadDir + "/submissions";
                default -> uploadDir;
            };
            
            Path filePath = Paths.get(directory, filename);
            byte[] content = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
