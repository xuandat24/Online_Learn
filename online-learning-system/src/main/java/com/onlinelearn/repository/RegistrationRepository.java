package com.onlinelearn.repository;

import com.onlinelearn.entity.Registration;
import com.onlinelearn.entity.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByEmail(String email);
    List<Registration> findByStatus(RegistrationStatus status);
}
