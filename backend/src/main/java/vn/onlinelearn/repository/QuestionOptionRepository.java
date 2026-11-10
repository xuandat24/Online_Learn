package vn.onlinelearn.repository;

import vn.onlinelearn.entity.QuestionOption;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long>, JpaSpecificationExecutor<QuestionOption> {
    List<QuestionOption> findByQuestionIdOrderBySortOrderAsc(Long questionId);
    void deleteByQuestionId(Long questionId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from QuestionOption e where e.optionId = :id")
    Optional<QuestionOption> lockById(@Param("id") Long id);
}
