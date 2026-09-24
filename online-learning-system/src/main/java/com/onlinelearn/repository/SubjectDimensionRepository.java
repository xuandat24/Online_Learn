package com.onlinelearn.repository;

import com.onlinelearn.entity.SubjectDimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectDimensionRepository extends JpaRepository<SubjectDimension, Long> {
    List<SubjectDimension> findBySubjectId(Long subjectId);
}
