package com.morrow.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "question_bank")
public class QuestionBankItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(length = 150)
    private String lessonTitle;

    @Column(nullable = false, length = 20)
    private String level = "MEDIUM"; // EASY, MEDIUM, HARD

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String optionsJson; // A, B, C, D separated by delimiter or JSON

    @Column(nullable = false)
    private Integer correctOptionIndex = 0;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    protected QuestionBankItem() {}

    public QuestionBankItem(String subject, String lessonTitle, String level, String prompt, String optionsJson, Integer correctOptionIndex, String explanation, String status) {
        this.subject = subject;
        this.lessonTitle = lessonTitle;
        this.level = level;
        this.prompt = prompt;
        this.optionsJson = optionsJson;
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }
    public Integer getCorrectOptionIndex() { return correctOptionIndex; }
    public void setCorrectOptionIndex(Integer correctOptionIndex) { this.correctOptionIndex = correctOptionIndex; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
