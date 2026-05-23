package com.eduapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType = QuestionType.MCQ;
    
    @Column(name = "option_a", columnDefinition = "TEXT")
    private String optionA;
    
    @Column(name = "option_b", columnDefinition = "TEXT")
    private String optionB;
    
    @Column(name = "option_c", columnDefinition = "TEXT")
    private String optionC;
    
    @Column(name = "option_d", columnDefinition = "TEXT")
    private String optionD;
    
    @Column(name = "correct_answer")
    private String correctAnswer;

    @Column(name = "sample_answer", columnDefinition = "TEXT")
    private String sampleAnswer;
    
    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    private Integer marks = 1;

    @Column(name = "order_index")
    private Integer orderIndex = 0;
}
