package com.eduapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "completed_lessons")
    private Integer completedLessons = 0;
    
    @Column(name = "total_lessons")
    private Integer totalLessons = 0;
    
    @Column(name = "progress_percentage")
    private Double progressPercentage = 0.0;
    
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt = LocalDateTime.now();
}
