package com.morrow.learning.dto;

import com.morrow.learning.domain.QuizAttempt;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record QuizAttemptView(Long id, Long quizId, String status, BigDecimal score,
                              LocalDateTime startedAt, LocalDateTime submittedAt,
                              List<QuizAnswerView> answers) {
    public static QuizAttemptView from(QuizAttempt attempt) {
        return new QuizAttemptView(attempt.getId(), attempt.getQuiz().getId(), attempt.getStatus().name(),
                attempt.getScore(), attempt.getStartedAt(), attempt.getSubmittedAt(),
                attempt.getAnswers().stream().map(QuizAnswerView::from).toList());
    }
}