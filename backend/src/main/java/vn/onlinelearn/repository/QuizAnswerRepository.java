package vn.onlinelearn.repository;

import vn.onlinelearn.entity.QuizAnswer;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long>, JpaSpecificationExecutor<QuizAnswer> {
    List<QuizAnswer> findByAttemptId(Long attemptId);
    Optional<QuizAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from QuizAnswer e where e.answerId = :id")
    Optional<QuizAnswer> lockById(@Param("id") Long id);
}
