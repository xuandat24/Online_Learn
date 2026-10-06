package com.morrow.learning.repository;

import com.morrow.learning.domain.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("select c from Course c where c.published = true "
            + "and (:category is null or lower(c.category) = lower(:category)) "
            + "and (:search is null or lower(c.title) like lower(concat('%', :search, '%')) "
            + "or lower(c.description) like lower(concat('%', :search, '%')) "
            + "or lower(c.instructor) like lower(concat('%', :search, '%'))) "
            + "order by c.createdAt desc")
    List<Course> searchPublished(@Param("search") String search, @Param("category") String category);
}