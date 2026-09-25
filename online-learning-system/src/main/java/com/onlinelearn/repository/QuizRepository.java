package com.onlinelearn.repository;

import com.onlinelearn.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findBySubjectId(Long subjectId);

    @org.springframework.data.jpa.repository.Query("SELECT q FROM Quiz q WHERE " +
           "(:ownerId IS NULL OR q.subject.owner.id = :ownerId) AND " +
           "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
           "(:quizTypeId IS NULL OR q.quizType.id = :quizTypeId) AND " +
           "(:keyword IS NULL OR LOWER(q.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Quiz> searchQuizzes(@org.springframework.data.repository.query.Param("ownerId") Long ownerId,
                             @org.springframework.data.repository.query.Param("subjectId") Long subjectId,
                             @org.springframework.data.repository.query.Param("quizTypeId") Long quizTypeId,
                             @org.springframework.data.repository.query.Param("keyword") String keyword);
}
