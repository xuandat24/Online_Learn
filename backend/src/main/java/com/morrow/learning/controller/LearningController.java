package com.morrow.learning.controller;

import com.morrow.learning.dto.LearnerQuizView;
import com.morrow.learning.dto.LearningCourseView;
import com.morrow.learning.dto.LessonProgressView;
import com.morrow.learning.dto.LessonView;
import com.morrow.learning.dto.ProgressWriteRequest;
import com.morrow.learning.dto.QuizAttemptView;
import com.morrow.learning.dto.QuizResultView;
import com.morrow.learning.dto.SelectedAnswerRequest;
import com.morrow.learning.service.CourseContentService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learning")
public class LearningController {
    private final CourseContentService contentService;

    public LearningController(CourseContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/courses/{courseId}")
    public LearningCourseView course(Principal principal, @PathVariable Long courseId) {
        return contentService.learningCourse(courseId, principal.getName());
    }

    @GetMapping("/lessons/{lessonId}")
    public LessonView lesson(Principal principal, @PathVariable Long lessonId) {
        return contentService.learningLesson(lessonId, principal.getName());
    }

    @PutMapping("/lessons/{lessonId}/progress")
    public LessonProgressView updateProgress(Principal principal, @PathVariable Long lessonId,
                                             @Valid @RequestBody ProgressWriteRequest request) {
        return contentService.updateLessonProgress(lessonId, principal.getName(), request.completed());
    }

    @GetMapping("/quizzes/{quizId}")
    public LearnerQuizView quiz(Principal principal, @PathVariable Long quizId) {
        return contentService.learnerQuiz(quizId, principal.getName());
    }

    @PostMapping("/quizzes/{quizId}/attempts")
    public QuizAttemptView startOrResume(Principal principal, @PathVariable Long quizId) {
        return contentService.startOrResumeAttempt(quizId, principal.getName());
    }

    @GetMapping("/attempts/{attemptId}")
    public QuizAttemptView attempt(Principal principal, @PathVariable Long attemptId) {
        return contentService.getAttempt(attemptId, principal.getName());
    }

    @PutMapping("/attempts/{attemptId}/answers/{questionId}")
    public QuizAttemptView answer(Principal principal, @PathVariable Long attemptId, @PathVariable Long questionId,
                                 @Valid @RequestBody SelectedAnswerRequest request) {
        return contentService.saveAnswer(attemptId, questionId, principal.getName(), request.selectedOptionIndex());
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public QuizResultView submit(Principal principal, @PathVariable Long attemptId) {
        return contentService.submitAttempt(attemptId, principal.getName());
    }
}