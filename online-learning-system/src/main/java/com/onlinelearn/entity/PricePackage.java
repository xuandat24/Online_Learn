package com.onlinelearn.entity;

import com.onlinelearn.entity.enums.PackageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "price_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricePackage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false, length = 100)
    private String packageName;

    // Duration in months (or days e.g., 30, 90, 180, 365)
    @Column(nullable = false)
    private Integer accessDuration;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal listPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PackageStatus status = PackageStatus.ACTIVE;
}
