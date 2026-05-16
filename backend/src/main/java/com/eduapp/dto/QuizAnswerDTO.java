package com.eduapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerDTO {
    private Long id;
    private Long questionId;
    private String questionText;
    private String questionType;
    private String selectedOption;
    private String textAnswer;
    private Integer awardedMarks;
    private Integer maxMarks;
    private Boolean isCorrect;
    private Boolean reviewed;
}
