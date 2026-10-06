package com.morrow.learning.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public record CourseWriteRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 3000) String description,
        @NotBlank @Size(max = 60) String category,
        @NotBlank @Size(max = 120) String instructor,
        @NotNull Long subjectId,
        @NotBlank @Size(max = 40) String level,
        @NotBlank @Size(max = 30) String duration,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @Size(max = 255) String image,
        @Size(max = 30) String accent,
        boolean published,
        @NotEmpty List<@Valid PricePackageWriteRequest> pricePackages
) {
}