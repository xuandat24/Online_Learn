package vn.onlinelearn.repository;

import vn.onlinelearn.entity.Question;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {
    List<Question> findBySubjectId(Long subjectId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Question e where e.questionId = :id")
    Optional<Question> lockById(@Param("id") Long id);
}
