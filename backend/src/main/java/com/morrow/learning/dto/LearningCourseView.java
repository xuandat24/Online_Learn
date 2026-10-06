package com.morrow.learning.dto;

import java.util.List;

public record LearningCourseView(Long id, String title, String description,
                                 List<LessonView> lessons, List<QuizOverviewView> quizzes) {
}