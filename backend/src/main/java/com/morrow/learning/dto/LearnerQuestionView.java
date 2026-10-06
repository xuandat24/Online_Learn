package com.morrow.learning.dto;

import com.morrow.learning.domain.QuizQuestion;
import java.util.List;

public record LearnerQuestionView(Long id, String prompt, List<String> options) {
    public static LearnerQuestionView from(QuizQuestion question) {
        return new LearnerQuestionView(question.getId(), question.getPrompt(), List.copyOf(question.getOptions()));
    }
}