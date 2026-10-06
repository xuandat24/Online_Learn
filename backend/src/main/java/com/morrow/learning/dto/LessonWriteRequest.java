package com.morrow.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LessonWriteRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 1000) String summary,
        @NotBlank @Size(max = 12000) String content,
        @Size(max = 2048) @Pattern(regexp = "https?://[^\\s]+") String videoUrl,
        @Min(1) @Max(10000) int displayOrder,
        boolean published
) {
}