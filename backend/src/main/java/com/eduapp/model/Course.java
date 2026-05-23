package com.eduapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;
    
    @Column(name = "is_published", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isPublished = false;
    
    @Column(name = "total_lessons")
    private Integer totalLessons = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
