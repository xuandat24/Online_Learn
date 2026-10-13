package com.morrow.learning.dto;

import com.morrow.learning.domain.SubjectDimension;

public record SubjectDimensionView(Long id, Long subjectId, String name, String description,
                                   int displayOrder, boolean active) {
    public static SubjectDimensionView from(SubjectDimension dimension) {
        return new SubjectDimensionView(dimension.getId(), dimension.getSubject().getId(),
                dimension.getName(), dimension.getDescription(), dimension.getDisplayOrder(),
                dimension.isActive());
    }
}
