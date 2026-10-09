package com.onlinelearn.dto.expert;

import com.onlinelearn.entity.enums.LessonStatus;
import com.onlinelearn.entity.enums.LessonTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonFormDTO {

    private Long id;

    @NotNull(message = "Môn học không được để trống")
    private Long subjectId;

    @NotBlank(message = "Tên bài học không được để trống")
    private String name;

    private Integer orderNum;

    private Long parentId;

    private LessonTypeEnum type;

    private LessonStatus status;

    private String videoLink;

    private String htmlContent;

    private Long quizId;
}
