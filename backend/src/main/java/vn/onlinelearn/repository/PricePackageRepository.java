package vn.onlinelearn.repository;

import vn.onlinelearn.entity.PricePackage;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface PricePackageRepository extends JpaRepository<PricePackage, Long>, JpaSpecificationExecutor<PricePackage> {
    List<PricePackage> findBySubjectId(Long subjectId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PricePackage e where e.packageId = :id")
    Optional<PricePackage> lockById(@Param("id") Long id);
}
