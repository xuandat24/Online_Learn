package com.morrow.learning.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, length = 1000)
    private String prompt;

    @ElementCollection
    @CollectionTable(name = "quiz_question_options", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "option_order")
    @Column(name = "option_text", nullable = false, length = 500)
    private List<String> options = new ArrayList<>();

    @Column(nullable = false)
    private int correctOptionIndex;

    @Column(nullable = false)
    private int displayOrder;

    protected QuizQuestion() {
    }

    public QuizQuestion(String prompt, List<String> options, int correctOptionIndex, int displayOrder) {
        this.prompt = prompt;
        this.options = new ArrayList<>(options);
        this.correctOptionIndex = correctOptionIndex;
        this.displayOrder = displayOrder;
    }

    public Long getId() { return id; }
    public Quiz getQuiz() { return quiz; }
    public String getPrompt() { return prompt; }
    public List<String> getOptions() { return options; }
    public int getCorrectOptionIndex() { return correctOptionIndex; }
    public int getDisplayOrder() { return displayOrder; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
}