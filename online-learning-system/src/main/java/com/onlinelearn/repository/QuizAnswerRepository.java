package com.onlinelearn.repository;

import com.onlinelearn.entity.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    List<QuizAnswer> findByAttemptId(Long attemptId);

    @Modifying
    @Query("DELETE FROM QuizAnswer qa WHERE qa.attempt.id IN :attemptIds")
    void deleteByAttemptIdIn(@Param("attemptIds") List<Long> attemptIds);
}
