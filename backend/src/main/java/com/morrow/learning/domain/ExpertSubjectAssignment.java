package com.morrow.learning.domain;

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
@Table(name = "expert_subject_assignments", uniqueConstraints =
        @UniqueConstraint(columnNames = {"expert_id", "subject_id"}))
public class ExpertSubjectAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expert_id", nullable = false)
    private User expert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    protected ExpertSubjectAssignment() {
    }

    public ExpertSubjectAssignment(User expert, Subject subject) {
        this.expert = expert;
        this.subject = subject;
    }

    public Long getId() { return id; }
    public User getExpert() { return expert; }
    public Subject getSubject() { return subject; }
}