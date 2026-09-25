package com.onlinelearn.repository;

import com.onlinelearn.entity.DimensionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DimensionTypeRepository extends JpaRepository<DimensionType, Long> {
    Optional<DimensionType> findByName(String name);
}
