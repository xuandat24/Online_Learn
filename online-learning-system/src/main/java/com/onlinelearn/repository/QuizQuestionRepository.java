package com.onlinelearn.repository;

import com.onlinelearn.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizIdOrderByOrderNumAsc(Long quizId);
    void deleteByQuizId(Long quizId);
    boolean existsByQuizIdAndQuestionId(Long quizId, Long questionId);
}
