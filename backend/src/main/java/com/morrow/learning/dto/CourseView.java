package com.morrow.learning.dto;

import com.morrow.learning.domain.Course;
import com.morrow.learning.domain.PricePackage;
import java.math.BigDecimal;
import java.util.List;

public record CourseView(Long id, String title, String description, String category, String instructor,
                         String level, String duration, BigDecimal price, double rating, int students,
                         String image, String accent, Long subjectId, String subjectName,
                         List<PricePackageView> pricePackages) {
    public static CourseView from(Course course) {
    List<PricePackageView> packages = course.getPricePackages().stream()
        .filter(PricePackage::isPublished)
        .map(PricePackageView::from)
        .toList();
    BigDecimal displayPrice = packages.stream().map(PricePackageView::price).min(BigDecimal::compareTo)
        .orElse(course.getPrice());
        return new CourseView(course.getId(), course.getTitle(), course.getDescription(), course.getCategory(),
        course.getInstructor(), course.getLevel(), course.getDuration(), displayPrice,
                course.getRating(), course.getStudents(), course.getImage(), course.getAccent(),
                course.getSubject() == null ? null : course.getSubject().getId(),
                course.getSubject() == null ? null : course.getSubject().getName(), packages);
    }
}