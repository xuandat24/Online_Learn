package com.onlinelearn.dto.expert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizFormDTO {

    private Long id;

    @NotNull(message = "Môn học không được để trống")
    private Long subjectId;

    @NotBlank(message = "Tên Quiz không được để trống")
    private String name;

    private Long levelId;

    private Long quizTypeId;

    private Integer duration;

    private Double passRate;

    private String description;

    @Builder.Default
    private List<Long> questionIds = new ArrayList<>();
}
