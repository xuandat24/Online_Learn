package vn.onlinelearn.repository;

import vn.onlinelearn.entity.Subject;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {
    boolean existsByNameIgnoreCase(String name);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Subject e where e.subjectId = :id")
    Optional<Subject> lockById(@Param("id") Long id);
}
