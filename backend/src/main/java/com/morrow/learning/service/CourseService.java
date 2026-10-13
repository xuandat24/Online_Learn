package com.morrow.learning.service;

import com.morrow.learning.domain.Course;
import com.morrow.learning.domain.PricePackage;
import com.morrow.learning.domain.Subject;
import com.morrow.learning.dto.CourseView;
import com.morrow.learning.dto.CourseWriteRequest;
import com.morrow.learning.dto.PricePackageWriteRequest;
import com.morrow.learning.repository.CourseRepository;
import com.morrow.learning.repository.SubjectRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    public CourseService(CourseRepository courseRepository, SubjectRepository subjectRepository) {
        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseView> search(String search, String category) {
        String cleanSearch = search == null || search.isBlank() ? null : search.trim();
        String cleanCategory = category == null || category.isBlank() || category.equalsIgnoreCase("All courses")
                ? null : category.trim();
        return courseRepository.searchPublished(cleanSearch, cleanCategory).stream().map(CourseView::from).toList();
    }

    @Transactional(readOnly = true)
    public CourseView findPublished(Long id) {
        Course course = courseRepository.findById(id)
                .filter(Course::isPublished)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return CourseView.from(course);
    }

    @Transactional(readOnly = true)
    public List<CourseView> listAll() {
        return courseRepository.findAll().stream().map(CourseView::fromAdmin).toList();
    }

    @Transactional
    public CourseView create(CourseWriteRequest request) {
        Subject subject = findManageableSubject(request.subjectId());
        Course course = new Course(request.title().trim(), request.description().trim(), request.category().trim(),
                request.instructor().trim(), request.level().trim(), request.duration().trim(), request.price(),
                0, 0, request.image(), request.accent());
        course.setSubject(subject);
        course.update(request.title().trim(), request.description().trim(), request.category().trim(),
                request.instructor().trim(), request.level().trim(), request.duration().trim(), request.price(),
                request.image(), request.accent(), request.published());
        course.reconcilePricePackages(toPricePackages(request.pricePackages(), course));
        return CourseView.fromAdmin(courseRepository.save(course));
    }

    @Transactional
    public CourseView update(Long id, CourseWriteRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        course.setSubject(findManageableSubject(request.subjectId()));
        course.update(request.title().trim(), request.description().trim(), request.category().trim(),
                request.instructor().trim(), request.level().trim(), request.duration().trim(), request.price(),
                request.image(), request.accent(), request.published());
        course.reconcilePricePackages(toPricePackages(request.pricePackages(), course));
        return CourseView.fromAdmin(course);
    }

    private List<PricePackage> toPricePackages(List<PricePackageWriteRequest> requests, Course course) {
        return requests.stream().map(request -> {
            if (request.id() == null) {
                return new PricePackage(request.name().trim(), request.price(), request.currency(),
                        request.accessDays(), request.published());
            }
            PricePackage existing = course.getPricePackages().stream()
                    .filter(pricePackage -> request.id().equals(pricePackage.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "A price package does not belong to this course"));
            existing.update(request.name().trim(), request.price(), request.currency(),
                    request.accessDays(), request.published());
            return existing;
        }).toList();
    }

    public void assertCourseExists(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
    }

    private Subject findManageableSubject(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .filter(Subject::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a valid subject"));
    }

    @Transactional
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
        courseRepository.deleteById(id);
    }

    public static Course sample(String title, String description, String category, String instructor,
                                String level, String duration, String price, double rating, int students,
                                String image, String accent) {
        return new Course(title, description, category, instructor, level, duration,
                new BigDecimal(price), rating, students, image, accent);
    }
}