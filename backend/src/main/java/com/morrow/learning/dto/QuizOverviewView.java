package com.morrow.learning.dto;

import com.morrow.learning.domain.Quiz;

public record QuizOverviewView(Long id, String title, String description, int questionCount) {
    public static QuizOverviewView from(Quiz quiz) {
        return new QuizOverviewView(quiz.getId(), quiz.getTitle(), quiz.getDescription(), quiz.getQuestions().size());
    }
}