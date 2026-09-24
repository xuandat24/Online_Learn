package com.onlinelearn.repository;

import com.onlinelearn.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByResetPasswordToken(String token);

    List<User> findByRoleCode(String roleCode);

    @Query("SELECT u FROM User u WHERE " +
           "(:roleId IS NULL OR u.role.id = :roleId) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR u.mobile LIKE CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("roleId") Long roleId,
                           @Param("status") com.onlinelearn.entity.enums.UserStatus status,
                           @Param("keyword") String keyword,
                           Pageable pageable);
}
