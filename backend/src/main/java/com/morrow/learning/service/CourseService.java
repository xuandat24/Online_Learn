package com.morrow.learning.service;

import com.morrow.learning.domain.Course;
import com.morrow.learning.domain.PricePackage;
import com.morrow.learning.domain.Role;
import com.morrow.learning.domain.Subject;
import com.morrow.learning.domain.User;
import com.morrow.learning.dto.CourseView;
import com.morrow.learning.dto.CourseWriteRequest;
import com.morrow.learning.dto.PricePackageWriteRequest;
import com.morrow.learning.repository.CourseRepository;
import com.morrow.learning.repository.ExpertSubjectAssignmentRepository;
import com.morrow.learning.repository.SubjectRepository;
import com.morrow.learning.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ExpertSubjectAssignmentRepository assignmentRepository;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository,
                         SubjectRepository subjectRepository,
                         ExpertSubjectAssignmentRepository assignmentRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.assignmentRepository = assignmentRepository;
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
    public List<CourseView> listAll(String actorEmail) {
        User actor = requireManager(actorEmail);
        return courseRepository.findAll().stream()
                .filter(course -> actor.getRole() == Role.ADMIN || (course.getSubject() != null
                        && assignmentRepository.existsByExpertIdAndSubjectId(actor.getId(), course.getSubject().getId())))
                .map(CourseView::from).toList();
    }

    @Transactional
    public CourseView create(CourseWriteRequest request, String actorEmail) {
        Subject subject = findManageableSubject(request.subjectId(), actorEmail);
        Course course = new Course(request.title().trim(), request.description().trim(), request.category().trim(),
                request.instructor().trim(), request.level().trim(), request.duration().trim(), request.price(),
                0, 0, request.image(), request.accent());
        course.setSubject(subject);
        course.update(request.title().trim(), request.description().trim(), request.category().trim(),
                request.instructor().trim(), request.level().trim(), request.duration().trim(), request.price(),
                request.image(), request.accent(), request.published());
        course.reconcilePricePackages(toPricePackages(request.pricePackages(), course));
        return CourseView.from(courseRepository.save(course));
    }

    @Transactional
    public CourseView update(Long id, CourseWriteRequest request, String actorEmail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        assertCourseAssignment(course, actorEmail);
        course.setSubject(findManageableSubject(request.subjectId(), actorEmail));
        course.update(request.title().trim(), request.description().trim(), request.category().trim(),
                request.instructor().trim(), request.level().trim(), request.duration().trim(), request.price(),
                request.image(), request.accent(), request.published());
        course.reconcilePricePackages(toPricePackages(request.pricePackages(), course));
        return CourseView.from(course);
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

    public void assertCanManageCourse(Long courseId, String actorEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        assertCourseAssignment(course, actorEmail);
    }

    private void assertCourseAssignment(Course course, String actorEmail) {
        User actor = requireManager(actorEmail);
        if (actor.getRole() == Role.ADMIN) return;
        if (course.getSubject() == null
                || !assignmentRepository.existsByExpertIdAndSubjectId(actor.getId(), course.getSubject().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this course subject");
        }
    }

    private Subject findManageableSubject(Long subjectId, String actorEmail) {
        User actor = requireManager(actorEmail);
        Subject subject = subjectRepository.findById(subjectId)
                .filter(Subject::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an active subject"));
        if (actor.getRole() == Role.EXPERT
                && !assignmentRepository.existsByExpertIdAndSubjectId(actor.getId(), subjectId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this subject");
        }
        return subject;
    }

    private User requireManager(String email) {
        User actor = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.EXPERT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course management is restricted to Admin and Expert");
        }
        return actor;
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