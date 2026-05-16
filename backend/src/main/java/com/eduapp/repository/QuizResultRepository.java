package com.eduapp.repository;

import com.eduapp.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUserId(Long userId);
    List<QuizResult> findByUserIdAndQuizId(Long userId, Long quizId);
    List<QuizResult> findByQuizId(Long quizId);
}
