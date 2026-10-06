package com.morrow.learning.dto;

import jakarta.validation.constraints.NotNull;

public record ProgressWriteRequest(@NotNull Boolean completed) {
}