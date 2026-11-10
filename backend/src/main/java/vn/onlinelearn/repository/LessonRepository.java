package vn.onlinelearn.repository;

import vn.onlinelearn.entity.Lesson;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface LessonRepository extends JpaRepository<Lesson, Long>, JpaSpecificationExecutor<Lesson> {
    List<Lesson> findBySubjectIdOrderBySortOrderAsc(Long subjectId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Lesson e where e.lessonId = :id")
    Optional<Lesson> lockById(@Param("id") Long id);
}
