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
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "quiz_answers", uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "question_id"}))
public class QuizAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(nullable = false)
    private int selectedOptionIndex;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected QuizAnswer() {
    }

    public QuizAnswer(QuizQuestion question, int selectedOptionIndex) {
        this.question = question;
        update(selectedOptionIndex);
    }

    public void update(int selectedOptionIndex) {
        this.selectedOptionIndex = selectedOptionIndex;
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public QuizAttempt getAttempt() { return attempt; }
    public QuizQuestion getQuestion() { return question; }
    public int getSelectedOptionIndex() { return selectedOptionIndex; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setAttempt(QuizAttempt attempt) { this.attempt = attempt; }
}