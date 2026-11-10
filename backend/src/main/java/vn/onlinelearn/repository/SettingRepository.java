package vn.onlinelearn.repository;

import vn.onlinelearn.entity.Setting;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface SettingRepository extends JpaRepository<Setting, Long>, JpaSpecificationExecutor<Setting> {
    Optional<Setting> findByTypeAndValue(String type, String value);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Setting e where e.settingId = :id")
    Optional<Setting> lockById(@Param("id") Long id);
}
