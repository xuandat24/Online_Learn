package com.morrow.learning.dto;

import com.morrow.learning.domain.Quiz;
import java.util.List;

public record LearnerQuizView(Long id, Long courseId, String title, String description,
                              List<LearnerQuestionView> questions) {
    public static LearnerQuizView from(Quiz quiz) {
        return new LearnerQuizView(quiz.getId(), quiz.getCourse().getId(), quiz.getTitle(), quiz.getDescription(),
                quiz.getQuestions().stream().map(LearnerQuestionView::from).toList());
    }
}