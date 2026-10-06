package com.morrow.learning.repository;

import com.morrow.learning.domain.Quiz;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findAllByCourseIdOrderByCreatedAtAsc(Long courseId);
    List<Quiz> findAllByCourseIdAndPublishedTrueOrderByCreatedAtAsc(Long courseId);
    Optional<Quiz> findByIdAndCourseId(Long id, Long courseId);
}