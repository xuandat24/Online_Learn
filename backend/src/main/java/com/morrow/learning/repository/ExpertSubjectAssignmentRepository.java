package com.morrow.learning.repository;

import com.morrow.learning.domain.ExpertSubjectAssignment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertSubjectAssignmentRepository extends JpaRepository<ExpertSubjectAssignment, Long> {
    boolean existsByExpertIdAndSubjectId(Long expertId, Long subjectId);
    Optional<ExpertSubjectAssignment> findByExpertIdAndSubjectId(Long expertId, Long subjectId);
    List<ExpertSubjectAssignment> findAllByExpertId(Long expertId);
}