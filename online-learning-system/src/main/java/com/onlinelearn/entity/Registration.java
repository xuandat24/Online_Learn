package com.onlinelearn.entity;

import com.onlinelearn.entity.enums.Gender;
import com.onlinelearn.entity.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private PricePackage pricePackage;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String mobile;

    @Column(nullable = false)
    private LocalDateTime registrationTime;

    // Sale staff assigned
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private User sale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.SUBMITTED;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column
    private LocalDateTime validFrom;

    @Column
    private LocalDateTime validTo;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
