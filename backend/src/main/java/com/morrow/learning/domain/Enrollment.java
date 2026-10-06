package com.morrow.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 24)
    private String status;

    private LocalDateTime accessExpiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Enrollment() {
    }

    public Enrollment(User user, Course course, String status) {
        this.user = user;
        this.course = course;
        this.status = status;
    }

    public Enrollment(User user, Course course, String status, LocalDateTime accessExpiresAt) {
        this(user, course, status);
        this.accessExpiresAt = accessExpiresAt;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Course getCourse() { return course; }
    public String getStatus() { return status; }
    public LocalDateTime getAccessExpiresAt() { return accessExpiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}