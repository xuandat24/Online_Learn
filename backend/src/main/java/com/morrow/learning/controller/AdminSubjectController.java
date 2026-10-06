package com.morrow.learning.controller;

import com.morrow.learning.dto.ExpertSubjectView;
import com.morrow.learning.dto.SubjectView;
import com.morrow.learning.dto.SubjectWriteRequest;
import com.morrow.learning.service.SubjectService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/subjects")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubjectController {
    private final SubjectService subjectService;

    public AdminSubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public List<SubjectView> list() {
        return subjectService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectView create(@Valid @RequestBody SubjectWriteRequest request) {
        return subjectService.create(request);
    }

    @PutMapping("/{subjectId}")
    public SubjectView update(@PathVariable Long subjectId, @Valid @RequestBody SubjectWriteRequest request) {
        return subjectService.update(subjectId, request);
    }

    @GetMapping("/{subjectId}/experts")
    public List<ExpertSubjectView> assignments(@PathVariable Long subjectId) {
        return subjectService.assignments(subjectId);
    }

    @PutMapping("/{subjectId}/experts/{expertId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpertSubjectView assign(@PathVariable Long subjectId, @PathVariable Long expertId) {
        return subjectService.assign(subjectId, expertId);
    }

    @DeleteMapping("/{subjectId}/experts/{expertId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassign(@PathVariable Long subjectId, @PathVariable Long expertId) {
        subjectService.unassign(subjectId, expertId);
    }
}