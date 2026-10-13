package com.morrow.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "subject_dimensions", uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "name"}))
public class SubjectDimension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active = true;

    protected SubjectDimension() {
    }

    public SubjectDimension(Subject subject, String name, String description, int displayOrder, boolean active) {
        this.subject = subject;
        update(name, description, displayOrder, active);
    }

    public void update(String name, String description, int displayOrder, boolean active) {
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    public Long getId() { return id; }
    public Subject getSubject() { return subject; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
}
