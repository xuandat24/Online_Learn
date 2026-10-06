package com.morrow.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "course_registrations")
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "price_package_id", nullable = false)
    private PricePackage pricePackage;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 190)
    private String email;

    @Column(nullable = false, length = 24)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.SUBMITTED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean loginEmailSent;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;

    protected Registration() {
    }

    public Registration(User customer, Course course, PricePackage pricePackage,
                        String fullName, String email, String phone) {
        this.customer = customer;
        this.course = course;
        this.pricePackage = pricePackage;
        this.fullName = fullName;
        this.email = email.toLowerCase();
        this.phone = phone;
        this.amount = pricePackage.getPrice();
        this.currency = pricePackage.getCurrency();
    }

    @PrePersist
    void onCreate() {
        submittedAt = LocalDateTime.now(ZoneOffset.UTC);
        updatedAt = submittedAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void edit(PricePackage pricePackage, String fullName, String email, String phone) {
        if (status != RegistrationStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted registrations can be edited");
        }
        this.pricePackage = pricePackage;
        this.amount = pricePackage.getPrice();
        this.currency = pricePackage.getCurrency();
        this.fullName = fullName;
        this.email = email.toLowerCase();
        this.phone = phone;
    }

    public void markPaid(User customer, boolean loginEmailSent) {
        if (status != RegistrationStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted registrations can be marked paid");
        }
        this.customer = customer;
        this.status = RegistrationStatus.PAID;
        this.paidAt = LocalDateTime.now(ZoneOffset.UTC);
        this.loginEmailSent = loginEmailSent;
    }

    public void cancel() {
        if (status != RegistrationStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted registrations can be cancelled");
        }
        status = RegistrationStatus.CANCELLED;
    }

    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public Course getCourse() { return course; }
    public PricePackage getPricePackage() { return pricePackage; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public RegistrationStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public boolean isLoginEmailSent() { return loginEmailSent; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
}