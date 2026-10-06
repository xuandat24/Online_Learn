package com.morrow.learning.repository;

import com.morrow.learning.domain.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByStatusOrderByCreatedAtDesc(String status);
    List<Post> findByCategoryAndStatusOrderByCreatedAtDesc(String category, String status);
}
