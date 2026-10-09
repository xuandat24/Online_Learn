package com.onlinelearn.dto.expert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectDimensionFormDTO {

    private Long id;

    @NotNull(message = "Loại chuẩn không được để trống")
    private Long typeId;

    @NotBlank(message = "Tên chuẩn không được để trống")
    private String name;

    private String description;
}
