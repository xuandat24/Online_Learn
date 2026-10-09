package com.onlinelearn.dto.expert;

import com.onlinelearn.entity.enums.SubjectStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectOverviewFormDTO {

    private Long id;

    @NotBlank(message = "Tên môn học không được để trống")
    private String name;

    private Long categoryId;

    private String thumbnail;

    private Boolean featured;

    private String briefInfo;

    private String description;

    private SubjectStatus status;
}
