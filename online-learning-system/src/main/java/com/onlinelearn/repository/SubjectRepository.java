package com.onlinelearn.repository;

import com.onlinelearn.entity.Subject;
import com.onlinelearn.entity.enums.SubjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByStatus(SubjectStatus status);
    List<Subject> findByFeaturedTrueAndStatus(SubjectStatus status);
    List<Subject> findByOwnerId(Long ownerId);
    Optional<Subject> findByName(String name);
    boolean existsByName(String name);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Subject s WHERE " +
           "(:ownerId IS NULL OR s.owner.id = :ownerId) AND " +
           "(:categoryId IS NULL OR s.category.id = :categoryId) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Subject> searchSubjects(@org.springframework.data.repository.query.Param("ownerId") Long ownerId,
                                 @org.springframework.data.repository.query.Param("categoryId") Long categoryId,
                                 @org.springframework.data.repository.query.Param("status") SubjectStatus status,
                                 @org.springframework.data.repository.query.Param("keyword") String keyword);
}
