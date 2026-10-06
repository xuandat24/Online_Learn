package com.morrow.learning.dto;

import com.morrow.learning.domain.ExpertSubjectAssignment;

public record ExpertSubjectView(Long expertId, String expertName, String expertEmail,
                                Long subjectId, String subjectName) {
    public static ExpertSubjectView from(ExpertSubjectAssignment assignment) {
        return new ExpertSubjectView(assignment.getExpert().getId(), assignment.getExpert().getFullName(),
                assignment.getExpert().getEmail(), assignment.getSubject().getId(),
                assignment.getSubject().getName());
    }
}