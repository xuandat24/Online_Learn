package com.onlinelearn.repository;

import com.onlinelearn.entity.PricePackage;
import com.onlinelearn.entity.enums.PackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricePackageRepository extends JpaRepository<PricePackage, Long> {
    List<PricePackage> findBySubjectId(Long subjectId);
    List<PricePackage> findBySubjectIdAndStatus(Long subjectId, PackageStatus status);
}
