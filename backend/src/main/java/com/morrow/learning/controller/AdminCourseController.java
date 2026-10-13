package com.morrow.learning.controller;

import com.morrow.learning.dto.CourseView;
import com.morrow.learning.dto.CourseWriteRequest;
import com.morrow.learning.service.CourseService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/courses")
public class AdminCourseController {
    private final CourseService courseService;

    public AdminCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public java.util.List<CourseView> list() {
        return courseService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseView create(@Valid @RequestBody CourseWriteRequest request) {
        return courseService.create(request);
    }

    @PutMapping("/{id}")
    public CourseView update(@PathVariable Long id, @Valid @RequestBody CourseWriteRequest request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }
}