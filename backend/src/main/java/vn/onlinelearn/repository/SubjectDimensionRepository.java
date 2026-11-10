package vn.onlinelearn.repository;

import vn.onlinelearn.entity.SubjectDimension;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface SubjectDimensionRepository extends JpaRepository<SubjectDimension, Long>, JpaSpecificationExecutor<SubjectDimension> {
    List<SubjectDimension> findBySubjectId(Long subjectId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from SubjectDimension e where e.dimensionId = :id")
    Optional<SubjectDimension> lockById(@Param("id") Long id);
}
