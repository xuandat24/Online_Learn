package vn.onlinelearn.repository;

import vn.onlinelearn.entity.LessonProgress;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long>, JpaSpecificationExecutor<LessonProgress> {
    List<LessonProgress> findByUserId(Long userId);
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from LessonProgress e where e.progressId = :id")
    Optional<LessonProgress> lockById(@Param("id") Long id);
}
