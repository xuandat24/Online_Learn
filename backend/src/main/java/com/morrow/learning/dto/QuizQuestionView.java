package com.morrow.learning.dto;

import com.morrow.learning.domain.QuizQuestion;
import java.util.List;

public record QuizQuestionView(Long id, String prompt, List<String> options, int correctOptionIndex) {
    public static QuizQuestionView from(QuizQuestion question) {
        return new QuizQuestionView(question.getId(), question.getPrompt(), List.copyOf(question.getOptions()),
                question.getCorrectOptionIndex());
    }
}