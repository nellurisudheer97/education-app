package com.eduapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDTO {
    private Long id;
    private Long quizId;
    private Long userId;
    private String studentName;
    private String studentEmail;
    private Integer score;
    private Integer totalMarks;
    private Double percentage;
    private Boolean isPassed;
    private String grade;
    private String status;
    private Integer timeTakenSeconds;
    private LocalDateTime completedAt;
    private List<QuizAnswerDTO> answers;
}
