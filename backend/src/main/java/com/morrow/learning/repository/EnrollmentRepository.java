package com.morrow.learning.repository;

import com.morrow.learning.domain.Enrollment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    List<Enrollment> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<Enrollment> findAllByOrderByCreatedAtDesc();

    @Query("select count(e) > 0 from Enrollment e where e.user.id = :userId and e.course.id = :courseId "
            + "and e.status = 'ACTIVE' and (e.accessExpiresAt is null or e.accessExpiresAt > :now)")
    boolean hasActiveAccess(@Param("userId") Long userId, @Param("courseId") Long courseId,
                            @Param("now") LocalDateTime now);
}