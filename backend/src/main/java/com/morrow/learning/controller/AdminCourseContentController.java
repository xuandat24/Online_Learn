package com.morrow.learning.controller;

import com.morrow.learning.dto.LessonView;
import com.morrow.learning.dto.LessonWriteRequest;
import com.morrow.learning.dto.QuizView;
import com.morrow.learning.dto.QuizWriteRequest;
import com.morrow.learning.service.CourseContentService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/courses/{courseId}")
@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
public class AdminCourseContentController {
    private final CourseContentService contentService;

    public AdminCourseContentController(CourseContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/lessons")
    public List<LessonView> lessons(Principal principal, @PathVariable Long courseId) {
        return contentService.manageLessons(courseId, principal.getName());
    }

    @PostMapping("/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonView createLesson(Principal principal, @PathVariable Long courseId,
                                   @Valid @RequestBody LessonWriteRequest request) {
        return contentService.createLesson(courseId, principal.getName(), request);
    }

    @PutMapping("/lessons/{lessonId}")
    public LessonView updateLesson(Principal principal, @PathVariable Long courseId, @PathVariable Long lessonId,
                                   @Valid @RequestBody LessonWriteRequest request) {
        return contentService.updateLesson(courseId, lessonId, principal.getName(), request);
    }

    @DeleteMapping("/lessons/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(Principal principal, @PathVariable Long courseId, @PathVariable Long lessonId) {
        contentService.deleteLesson(courseId, lessonId, principal.getName());
    }

    @GetMapping("/quizzes")
    public List<QuizView> quizzes(Principal principal, @PathVariable Long courseId) {
        return contentService.manageQuizzes(courseId, principal.getName());
    }

    @PostMapping("/quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizView createQuiz(Principal principal, @PathVariable Long courseId,
                               @Valid @RequestBody QuizWriteRequest request) {
        return contentService.createQuiz(courseId, principal.getName(), request);
    }

    @PutMapping("/quizzes/{quizId}")
    public QuizView updateQuiz(Principal principal, @PathVariable Long courseId, @PathVariable Long quizId,
                               @Valid @RequestBody QuizWriteRequest request) {
        return contentService.updateQuiz(courseId, quizId, principal.getName(), request);
    }

    @DeleteMapping("/quizzes/{quizId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuiz(Principal principal, @PathVariable Long courseId, @PathVariable Long quizId) {
        contentService.deleteQuiz(courseId, quizId, principal.getName());
    }
}