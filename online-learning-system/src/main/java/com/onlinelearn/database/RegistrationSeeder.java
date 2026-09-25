package com.onlinelearn.database;

import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.CourseAccessStatus;
import com.onlinelearn.entity.enums.RegistrationStatus;
import com.onlinelearn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationSeeder {

    private final RegistrationRepository registrationRepository;
    private final CourseAccessRepository courseAccessRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final PricePackageRepository pricePackageRepository;

    @Transactional
    public void seedRegistrations() {
        log.info("--> Seeding Sample Registrations and Course Access...");

        if (registrationRepository.count() > 0) {
            log.info("Registrations already seeded, skipping.");
            return;
        }

        User customer = userRepository.findByEmail("customer@onlinelearn.com").orElse(null);
        User sale = userRepository.findByEmail("sale@onlinelearn.com").orElse(null);

        if (customer == null) {
            log.warn("Customer user not found, skipping registration seeding.");
            return;
        }

        Subject javaSubject = subjectRepository.findByName("Java Core cơ bản").orElse(null);
        Subject springSubject = subjectRepository.findByName("Spring Boot từ cơ bản đến nâng cao").orElse(null);

        // 1. Paid registration for Java Core
        if (javaSubject != null) {
            List<PricePackage> javaPackages = pricePackageRepository.findBySubjectId(javaSubject.getId());
            PricePackage javaPackage = javaPackages.isEmpty() ? null : javaPackages.get(0);

            Registration paidReg = Registration.builder()
                    .subject(javaSubject)
                    .pricePackage(javaPackage)
                    .fullName(customer.getFullName())
                    .gender(customer.getGender())
                    .email(customer.getEmail())
                    .mobile(customer.getMobile())
                    .registrationTime(LocalDateTime.now().minusDays(5))
                    .sale(sale)
                    .status(RegistrationStatus.PAID)
                    .totalCost(javaPackage != null && javaPackage.getSalePrice() != null ? javaPackage.getSalePrice() : BigDecimal.valueOf(899000))
                    .validFrom(LocalDateTime.now().minusDays(5))
                    .validTo(LocalDateTime.now().plusDays(175))
                    .notes("Đã thanh toán chuyển khoản qua ngân hàng. Kích hoạt tự động.")
                    .build();
            paidReg = registrationRepository.save(paidReg);

            // Grant CourseAccess for Java Core
            courseAccessRepository.save(CourseAccess.builder()
                    .customer(customer)
                    .subject(javaSubject)
                    .registration(paidReg)
                    .grantedDate(LocalDateTime.now().minusDays(5))
                    .expiryDate(LocalDateTime.now().plusDays(175))
                    .status(CourseAccessStatus.ACTIVE)
                    .build());
        }

        // 2. Submitted registration for Spring Boot
        if (springSubject != null) {
            List<PricePackage> springPackages = pricePackageRepository.findBySubjectId(springSubject.getId());
            PricePackage springPackage = springPackages.isEmpty() ? null : springPackages.get(0);

            Registration submittedReg = Registration.builder()
                    .subject(springSubject)
                    .pricePackage(springPackage)
                    .fullName(customer.getFullName())
                    .gender(customer.getGender())
                    .email(customer.getEmail())
                    .mobile(customer.getMobile())
                    .registrationTime(LocalDateTime.now().minusHours(2))
                    .sale(sale)
                    .status(RegistrationStatus.SUBMITTED)
                    .totalCost(springPackage != null && springPackage.getSalePrice() != null ? springPackage.getSalePrice() : BigDecimal.valueOf(1199000))
                    .notes("Học viên đăng ký qua website. Đang chờ nhân viên tư vấn gọi điện xác nhận.")
                    .build();
            registrationRepository.save(submittedReg);
        }

        log.info("✓ Sample Registrations and Course Access seeded successfully.");
    }
}
