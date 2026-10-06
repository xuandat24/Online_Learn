package com.morrow.learning.repository;

import com.morrow.learning.domain.Subject;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByNameIgnoreCase(String name);
    List<Subject> findAllByActiveTrueOrderByNameAsc();
    List<Subject> findAllByOrderByNameAsc();
}