package com.onlinelearn.repository;

import com.onlinelearn.entity.CourseAccess;
import com.onlinelearn.entity.enums.CourseAccessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseAccessRepository extends JpaRepository<CourseAccess, Long> {
    List<CourseAccess> findByCustomerId(Long customerId);
    List<CourseAccess> findByCustomerIdAndStatus(Long customerId, CourseAccessStatus status);
}
