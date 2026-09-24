package com.onlinelearn.repository;

import com.onlinelearn.entity.SubjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectCategoryRepository extends JpaRepository<SubjectCategory, Long> {
    List<SubjectCategory> findByStatusTrue();
    Optional<SubjectCategory> findByName(String name);
}
