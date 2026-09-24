package com.onlinelearn.repository;

import com.onlinelearn.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findBySubjectIdOrderByOrderNumAsc(Long subjectId);
    List<Lesson> findByParentLessonIdOrderByOrderNumAsc(Long parentId);
    List<Lesson> findBySubjectIdAndType(Long subjectId, com.onlinelearn.entity.enums.LessonTypeEnum type);
    List<Lesson> findBySubjectIdAndNameContainingIgnoreCaseOrderByOrderNumAsc(Long subjectId, String name);
}
