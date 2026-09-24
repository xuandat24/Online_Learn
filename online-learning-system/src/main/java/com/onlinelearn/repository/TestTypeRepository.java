package com.onlinelearn.repository;

import com.onlinelearn.entity.TestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestTypeRepository extends JpaRepository<TestType, Long> {
    List<TestType> findByStatusTrue();
    Optional<TestType> findByCode(String code);
}
