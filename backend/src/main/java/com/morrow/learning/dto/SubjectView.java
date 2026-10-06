package com.morrow.learning.dto;

import com.morrow.learning.domain.Subject;

public record SubjectView(Long id, String name, String description, boolean active) {
    public static SubjectView from(Subject subject) {
        return new SubjectView(subject.getId(), subject.getName(), subject.getDescription(), subject.isActive());
    }
}