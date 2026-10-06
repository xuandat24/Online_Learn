package com.morrow.learning.repository;

import com.morrow.learning.domain.AttemptStatus;
import com.morrow.learning.domain.QuizAttempt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    boolean existsByQuizId(Long quizId);
    Optional<QuizAttempt> findFirstByQuizIdAndUserIdAndStatusOrderByStartedAtDesc(
            Long quizId, Long userId, AttemptStatus status);
    Optional<QuizAttempt> findByIdAndUserId(Long id, Long userId);
}