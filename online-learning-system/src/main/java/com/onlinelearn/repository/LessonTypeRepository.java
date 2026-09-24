package com.onlinelearn.repository;

import com.onlinelearn.entity.LessonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonTypeRepository extends JpaRepository<LessonType, Long> {
    List<LessonType> findByStatusTrue();
    Optional<LessonType> findByCode(String code);
}
