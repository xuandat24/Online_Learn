package com.morrow.learning.dto;

import com.morrow.learning.domain.LessonProgress;
import java.time.LocalDateTime;

public record LessonProgressView(Long lessonId, boolean completed, LocalDateTime updatedAt) {
    public static LessonProgressView from(LessonProgress progress) {
        return new LessonProgressView(progress.getLesson().getId(), progress.isCompleted(), progress.getUpdatedAt());
    }
}