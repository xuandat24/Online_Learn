// Thư mục: src/main/java/com/onlinelearn/repository/QuizAttemptRepository.java
package com.onlinelearn.repository;

import com.onlinelearn.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    boolean existsByQuizId(Long quizId);
    long countByQuizId(Long quizId);
    List<QuizAttempt> findByQuizId(Long quizId);
    List<QuizAttempt> findByCustomerId(Long customerId);
}
