package vn.onlinelearn.repository;

import vn.onlinelearn.entity.QuizAttempt;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long>, JpaSpecificationExecutor<QuizAttempt> {
    boolean existsByQuizId(Long quizId);
    List<QuizAttempt> findByUserIdAndQuizIdOrderByStartedAtDesc(Long userId, Long quizId);
    boolean existsByUserIdAndQuizIdAndStatus(Long userId, Long quizId, String status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from QuizAttempt e where e.attemptId = :id")
    Optional<QuizAttempt> lockById(@Param("id") Long id);
}
