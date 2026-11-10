package vn.onlinelearn.repository;

import vn.onlinelearn.entity.Post;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Post e where e.postId = :id")
    Optional<Post> lockById(@Param("id") Long id);
}
