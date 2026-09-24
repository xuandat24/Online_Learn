package com.onlinelearn.repository;

import com.onlinelearn.entity.QuestionLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionLevelRepository extends JpaRepository<QuestionLevel, Long> {
    List<QuestionLevel> findByStatusTrue();
    Optional<QuestionLevel> findByCode(String code);
}
