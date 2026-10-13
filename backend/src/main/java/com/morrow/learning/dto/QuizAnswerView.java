package com.morrow.learning.dto;

import com.morrow.learning.domain.QuizAnswer;

public record QuizAnswerView(Long questionId, int selectedOptionIndex, boolean markedForReview, boolean answerPeeked) {
    public static QuizAnswerView from(QuizAnswer answer) {
        return new QuizAnswerView(
                answer.getQuestion().getId(),
                answer.getSelectedOptionIndex(),
                answer.isMarkedForReview(),
                answer.isAnswerPeeked()
        );
    }
}