package com.morrow.learning.controller;

import com.morrow.learning.dto.CourseView;
import com.morrow.learning.service.CourseService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseView> search(@RequestParam(required = false) String search,
                                   @RequestParam(required = false) String category) {
        return courseService.search(search, category);
    }

    @GetMapping("/{id}")
    public CourseView get(@PathVariable Long id) {
        return courseService.findPublished(id);
    }
}