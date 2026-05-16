package com.eduapp.repository;

import com.eduapp.model.QuizAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAssignmentRepository extends JpaRepository<QuizAssignment, Long> {
    List<QuizAssignment> findByUserId(Long userId);
    boolean existsByUserIdAndQuizId(Long userId, Long quizId);
}
