package com.morrow.learning.repository;

import com.morrow.learning.domain.SubjectDimension;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectDimensionRepository extends JpaRepository<SubjectDimension, Long> {
    List<SubjectDimension> findAllBySubjectIdOrderByDisplayOrderAscIdAsc(Long subjectId);
    Optional<SubjectDimension> findByIdAndSubjectId(Long id, Long subjectId);
}
