package com.onlinelearn.repository;

import com.onlinelearn.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySubjectId(Long subjectId);

    @org.springframework.data.jpa.repository.Query("SELECT q FROM Question q WHERE " +
           "(:ownerId IS NULL OR q.subject.owner.id = :ownerId) AND " +
           "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
           "(:lessonId IS NULL OR q.lesson.id = :lessonId) AND " +
           "(:levelId IS NULL OR q.level.id = :levelId) AND " +
           "(:status IS NULL OR q.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Question> searchQuestions(@org.springframework.data.repository.query.Param("ownerId") Long ownerId,
                                   @org.springframework.data.repository.query.Param("subjectId") Long subjectId,
                                   @org.springframework.data.repository.query.Param("lessonId") Long lessonId,
                                   @org.springframework.data.repository.query.Param("levelId") Long levelId,
                                   @org.springframework.data.repository.query.Param("status") com.onlinelearn.entity.enums.QuestionStatus status,
                                   @org.springframework.data.repository.query.Param("keyword") String keyword);
}
