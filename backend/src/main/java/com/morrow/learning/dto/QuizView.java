package com.morrow.learning.dto;

import com.morrow.learning.domain.Quiz;
import java.util.List;

public record QuizView(Long id, Long courseId, String title, String description, int passingScore,
                       boolean published, boolean locked, List<QuizQuestionView> questions) {
    public static QuizView from(Quiz quiz) {
        return from(quiz, false);
    }

    public static QuizView from(Quiz quiz, boolean locked) {
        return new QuizView(quiz.getId(), quiz.getCourse().getId(), quiz.getTitle(), quiz.getDescription(),
                quiz.getPassingScore(), quiz.isPublished(), locked, quiz.getQuestions().stream()
                .map(QuizQuestionView::from).toList());
    }
}