package com.morrow.learning.service;

import com.morrow.learning.domain.Enrollment;
import com.morrow.learning.domain.Registration;
import com.morrow.learning.domain.RegistrationStatus;
import com.morrow.learning.domain.Role;
import com.morrow.learning.domain.User;
import com.morrow.learning.dto.RegistrationView;
import com.morrow.learning.repository.EnrollmentRepository;
import com.morrow.learning.repository.RegistrationRepository;
import com.morrow.learning.repository.UserRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SalesRegistrationService {
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginEmailService loginEmailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public SalesRegistrationService(RegistrationRepository registrationRepository, UserRepository userRepository,
                                    EnrollmentRepository enrollmentRepository, PasswordEncoder passwordEncoder,
                                    LoginEmailService loginEmailService) {
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginEmailService = loginEmailService;
    }

    @Transactional(readOnly = true)
    public List<RegistrationView> submitted() {
        return registrationRepository.findAllByStatusOrderBySubmittedAtAsc(RegistrationStatus.SUBMITTED).stream()
                .map(RegistrationView::from).toList();
    }

    @Transactional
    public RegistrationView markPaid(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found"));
        if (registration.getStatus() != RegistrationStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only submitted registrations can be marked paid");
        }

        User customer = userRepository.findByEmailIgnoreCase(registration.getEmail()).orElse(null);
        boolean createdUser = customer == null;
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (customer != null && customer.getRole() != Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This email belongs to a non-customer account; resolve the account before payment");
        }
        if (customer != null && enrollmentRepository.hasActiveAccess(
                customer.getId(), registration.getCourse().getId(), now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This customer already has course access");
        }

        String temporaryPassword = null;
        if (createdUser) {
            temporaryPassword = generateTemporaryPassword();
            customer = userRepository.save(new User(registration.getFullName(), registration.getEmail(),
                    passwordEncoder.encode(temporaryPassword), Role.CUSTOMER));
        }

        int accessDays = registration.getPricePackage().getAccessDays();
        LocalDateTime expiresAt = accessDays == 0 ? null : now.plusDays(accessDays);
        registration.markPaid(customer, createdUser);
        enrollmentRepository.save(new Enrollment(customer, registration.getCourse(), "ACTIVE", expiresAt));
        registration.getCourse().addStudent();
        if (createdUser) {
            try {
                loginEmailService.sendLoginInformation(customer.getFullName(), customer.getEmail(), temporaryPassword);
            } catch (RuntimeException exception) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Login email could not be delivered; the registration remains submitted");
            }
        }
        return RegistrationView.from(registration);
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "") + secureRandom.nextInt(1000, 9999);
    }
}