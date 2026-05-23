package com.eduapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDTO {
    private Long id;
    private String title;
    private String description;
    private Long lessonId;
    private Integer totalQuestions;
    private Integer passingScore;
    private Integer timerMinutes;
    private Integer totalMarks;
    private java.util.List<QuestionDTO> questions;
    private boolean assigned;
    private QuizResultDTO latestResult;
}
