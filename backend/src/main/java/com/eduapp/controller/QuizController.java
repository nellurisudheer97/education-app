package com.eduapp.controller;

import com.eduapp.dto.QuizDTO;
import com.eduapp.dto.QuizResultDTO;
import com.eduapp.dto.QuizSubmissionRequest;
import com.eduapp.dto.ReviewAnswerRequest;
import com.eduapp.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/quizzes")
public class QuizController {
    
    @Autowired
    private QuizService quizService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody QuizDTO quizDTO) {
        return ResponseEntity.ok(quizService.createQuiz(quizDTO));
    }
    
    @GetMapping("/{quizId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizById(quizId));
    }
    
    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizDTO>> getQuizzesByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(quizService.getQuizzesByLesson(lessonId));
    }
    
    @PutMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<QuizDTO> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizDTO quizDTO) {
        return ResponseEntity.ok(quizService.updateQuiz(quizId, quizDTO));
    }
    
    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.ok("Quiz deleted successfully");
    }

    @PostMapping("/{quizId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<String> assignQuiz(
            @PathVariable Long quizId,
            @RequestParam Long studentId) {
        quizService.assignQuizToStudent(quizId, studentId);
        return ResponseEntity.ok("Quiz assigned successfully");
    }

    @PostMapping("/{quizId}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizResultDTO> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizSubmissionRequest request) {
        return ResponseEntity.ok(quizService.submitQuiz(quizId, request));
    }

    @GetMapping("/{quizId}/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<QuizResultDTO>> getQuizResults(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getResultsForQuiz(quizId));
    }

    @PutMapping("/answers/{answerId}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<QuizResultDTO> reviewAnswer(
            @PathVariable Long answerId,
            @RequestBody ReviewAnswerRequest request) {
        return ResponseEntity.ok(quizService.reviewAnswer(answerId, request));
    }
}
