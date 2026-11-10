package vn.onlinelearn.repository;

import vn.onlinelearn.entity.Quiz;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface QuizRepository extends JpaRepository<Quiz, Long>, JpaSpecificationExecutor<Quiz> {
    List<Quiz> findBySubjectId(Long subjectId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Quiz e where e.quizId = :id")
    Optional<Quiz> lockById(@Param("id") Long id);
}
