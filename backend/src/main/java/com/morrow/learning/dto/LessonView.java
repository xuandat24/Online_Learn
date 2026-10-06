package com.morrow.learning.dto;

import com.morrow.learning.domain.Lesson;

public record LessonView(Long id, Long courseId, String title, String summary, String content,
                         String videoUrl, int displayOrder, boolean published, boolean completed) {
    public static LessonView from(Lesson lesson, boolean completed) {
        return new LessonView(lesson.getId(), lesson.getCourse().getId(), lesson.getTitle(), lesson.getSummary(),
                lesson.getContent(), lesson.getVideoUrl(), lesson.getDisplayOrder(), lesson.isPublished(), completed);
    }
}