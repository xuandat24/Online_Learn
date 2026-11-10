package vn.onlinelearn.repository;

import vn.onlinelearn.entity.Registration;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface RegistrationRepository extends JpaRepository<Registration, Long>, JpaSpecificationExecutor<Registration> {
    List<Registration> findByCustomerUserIdOrderByRegisteredAtDesc(Long userId);
    List<Registration> findByEmail(String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Registration e where e.registrationId = :id")
    Optional<Registration> lockById(@Param("id") Long id);
}
