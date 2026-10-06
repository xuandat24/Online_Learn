package com.morrow.learning.repository;

import com.morrow.learning.domain.PricePackage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricePackageRepository extends JpaRepository<PricePackage, Long> {
    Optional<PricePackage> findByIdAndCourseIdAndPublishedTrue(Long id, Long courseId);
}