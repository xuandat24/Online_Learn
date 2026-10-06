package com.morrow.learning.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 1500)
    private String description;

    @Column(nullable = false)
    private int passingScore;

    @Column(nullable = false)
    private boolean published;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<QuizQuestion> questions = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected Quiz() {
    }

    public Quiz(Course course, String title, String description, int passingScore, boolean published) {
        this.course = course;
        update(title, description, passingScore, published);
    }

    public void update(String title, String description, int passingScore, boolean published) {
        this.title = title;
        this.description = description;
        this.passingScore = passingScore;
        this.published = published;
    }

    public void replaceQuestions(List<QuizQuestion> newQuestions) {
        questions.clear();
        newQuestions.forEach(this::addQuestion);
    }

    public void addQuestion(QuizQuestion question) {
        questions.add(question);
        question.setQuiz(this);
    }

    @PrePersist
    void onCreate() { createdAt = updatedAt = LocalDateTime.now(ZoneOffset.UTC); }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(ZoneOffset.UTC); }

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getPassingScore() { return passingScore; }
    public boolean isPublished() { return published; }
    public List<QuizQuestion> getQuestions() { return questions; }
}