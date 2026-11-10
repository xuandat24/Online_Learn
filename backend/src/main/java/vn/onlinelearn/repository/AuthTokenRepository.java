package vn.onlinelearn.repository;

import vn.onlinelearn.entity.AuthToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long>, JpaSpecificationExecutor<AuthToken> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthToken> findByTokenHash(String tokenHash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from AuthToken e where e.tokenId = :id")
    Optional<AuthToken> lockById(@Param("id") Long id);
}
