package vn.onlinelearn.repository;

import vn.onlinelearn.entity.Slider;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface SliderRepository extends JpaRepository<Slider, Long>, JpaSpecificationExecutor<Slider> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Slider e where e.sliderId = :id")
    Optional<Slider> lockById(@Param("id") Long id);
}
