package com.morrow.learning.service;

import com.morrow.learning.domain.Course;
import com.morrow.learning.domain.Enrollment;
import com.morrow.learning.domain.User;
import com.morrow.learning.dto.EnrollmentView;
import com.morrow.learning.dto.AdminEnrollmentView;
import com.morrow.learning.repository.CourseRepository;
import com.morrow.learning.repository.EnrollmentRepository;
import com.morrow.learning.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EnrollmentService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(UserRepository userRepository, CourseRepository courseRepository,
                             EnrollmentRepository enrollmentRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentView> myEnrollments(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        return enrollmentRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(EnrollmentView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminEnrollmentView> allEnrollments() {
        return enrollmentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AdminEnrollmentView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasCourseAccess(String email, Long courseId) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        return enrollmentRepository.hasActiveAccess(user.getId(), courseId, LocalDateTime.now(ZoneOffset.UTC));
    }
}