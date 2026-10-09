package com.onlinelearn.repository;

import com.onlinelearn.entity.Question;
import com.onlinelearn.entity.enums.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySubjectId(Long subjectId);

    @Query("SELECT q FROM Question q WHERE " +
           "(:ownerId IS NULL OR q.subject.owner.id = :ownerId) AND " +
           "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
           "(:lessonId IS NULL OR q.lesson.id = :lessonId) AND " +
           "(:dimensionId IS NULL OR :dimensionId IN (SELECT d.id FROM q.dimensions d)) AND " +
           "(:levelId IS NULL OR q.level.id = :levelId) AND " +
           "(:status IS NULL OR q.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Question> searchQuestions(@Param("ownerId") Long ownerId,
                                   @Param("subjectId") Long subjectId,
                                   @Param("lessonId") Long lessonId,
                                   @Param("dimensionId") Long dimensionId,
                                   @Param("levelId") Long levelId,
                                   @Param("status") QuestionStatus status,
                                   @Param("keyword") String keyword);
}
