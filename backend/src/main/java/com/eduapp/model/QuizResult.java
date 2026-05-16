package com.eduapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    private Integer score = 0;
    
    private Integer totalMarks = 0;
    
    private Double percentage = 0.0;
    
    private Boolean isPassed = false;

    private String grade = "PENDING";

    private String status = "SUBMITTED";

    private Integer timeTakenSeconds = 0;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime completedAt = LocalDateTime.now();
}
