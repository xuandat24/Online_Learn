package com.morrow.learning.repository;

import com.morrow.learning.domain.LessonProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<LessonProgress> findAllByUserIdAndLessonCourseId(Long userId, Long courseId);
}