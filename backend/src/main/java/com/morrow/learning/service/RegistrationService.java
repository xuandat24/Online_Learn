package com.morrow.learning.service;

import com.morrow.learning.domain.Course;
import com.morrow.learning.domain.PricePackage;
import com.morrow.learning.domain.Registration;
import com.morrow.learning.domain.RegistrationStatus;
import com.morrow.learning.domain.User;
import com.morrow.learning.dto.RegistrationEditRequest;
import com.morrow.learning.dto.RegistrationRequest;
import com.morrow.learning.dto.RegistrationView;
import com.morrow.learning.repository.CourseRepository;
import com.morrow.learning.repository.PricePackageRepository;
import com.morrow.learning.repository.RegistrationRepository;
import com.morrow.learning.repository.UserRepository;
import java.util.List;
import java.util.EnumSet;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PricePackageRepository pricePackageRepository;
    private final RegistrationRepository registrationRepository;

    public RegistrationService(UserRepository userRepository, CourseRepository courseRepository,
                               PricePackageRepository pricePackageRepository,
                               RegistrationRepository registrationRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.pricePackageRepository = pricePackageRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public RegistrationView submit(String email, RegistrationRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .filter(Course::isPublished)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        PricePackage pricePackage = findPackage(request.pricePackageId(), course.getId());
        User customer = email == null ? null : userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (email != null && (customer == null || customer.getRole() != com.morrow.learning.domain.Role.CUSTOMER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Customer accounts can register while signed in");
        }
        if (email == null && userRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sign in with this email before registering");
        }
        String registrationEmail = customer == null ? request.email().trim() : customer.getEmail();
        if (registrationRepository.existsByCourseIdAndEmailIgnoreCaseAndStatusIn(course.getId(), registrationEmail,
                EnumSet.of(RegistrationStatus.SUBMITTED, RegistrationStatus.PAID))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active registration already exists for this course");
        }
        Registration registration = new Registration(customer, course, pricePackage,
                customer == null ? request.fullName().trim() : customer.getFullName(), registrationEmail,
                request.phone().trim());
        return RegistrationView.from(registrationRepository.save(registration));
    }

    @Transactional(readOnly = true)
    public List<RegistrationView> mine(String email) {
        User customer = requireCustomer(email);
        return registrationRepository.findAllByCustomerIdOrderBySubmittedAtDesc(customer.getId()).stream()
                .map(RegistrationView::from).toList();
    }

    @Transactional
    public RegistrationView edit(String email, Long registrationId, RegistrationEditRequest request) {
        User customer = requireCustomer(email);
        Registration registration = registrationRepository.findByIdAndCustomerId(registrationId, customer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found"));
        if (!customer.getEmail().equalsIgnoreCase(request.email().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Registration email must match the signed-in customer account");
        }
        PricePackage pricePackage = findPackage(request.pricePackageId(), registration.getCourse().getId());
        try {
            registration.edit(pricePackage, request.fullName().trim(), customer.getEmail(), request.phone().trim());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
        return RegistrationView.from(registration);
    }

    @Transactional
    public void cancel(String email, Long registrationId) {
        User customer = requireCustomer(email);
        Registration registration = registrationRepository.findByIdAndCustomerId(registrationId, customer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found"));
        try {
            registration.cancel();
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    private User requireCustomer(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    private PricePackage findPackage(Long packageId, Long courseId) {
        return pricePackageRepository.findByIdAndCourseIdAndPublishedTrue(packageId, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Choose a published price package for this course"));
    }
}