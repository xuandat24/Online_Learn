package com.morrow.learning.dto;

import java.math.BigDecimal;

public record QuizResultView(Long attemptId, String status, BigDecimal score, int passingScore, boolean passed) {
}