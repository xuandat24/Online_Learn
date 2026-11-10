package vn.onlinelearn.repository;

import vn.onlinelearn.entity.CourseAccess;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface CourseAccessRepository extends JpaRepository<CourseAccess, Long>, JpaSpecificationExecutor<CourseAccess> {
    List<CourseAccess> findByUserId(Long userId);
    boolean existsByRegistrationId(Long registrationId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CourseAccess e where e.accessId = :id")
    Optional<CourseAccess> lockById(@Param("id") Long id);
}
