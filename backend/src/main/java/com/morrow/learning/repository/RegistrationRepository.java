package com.morrow.learning.repository;

import com.morrow.learning.domain.Registration;
import com.morrow.learning.domain.RegistrationStatus;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findAllByCustomerIdOrderBySubmittedAtDesc(Long customerId);
    Optional<Registration> findByIdAndCustomerId(Long id, Long customerId);
    List<Registration> findAllByStatusOrderBySubmittedAtAsc(com.morrow.learning.domain.RegistrationStatus status);
    boolean existsByCourseIdAndEmailIgnoreCaseAndStatusIn(
            Long courseId, String email, Collection<RegistrationStatus> statuses);
}