package com.morrow.learning.repository;

import com.morrow.learning.domain.Lesson;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findAllByCourseIdOrderByDisplayOrderAsc(Long courseId);
    List<Lesson> findAllByCourseIdAndPublishedTrueOrderByDisplayOrderAsc(Long courseId);
    Optional<Lesson> findByIdAndCourseId(Long id, Long courseId);
}