package com.onlinelearn.dto.expert;

import com.onlinelearn.entity.enums.MediaType;
import com.onlinelearn.entity.enums.QuestionStatus;
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
public class QuestionFormDTO {

    private Long id;

    @NotNull(message = "Môn học không được để trống")
    private Long subjectId;

    private Long lessonId;

    private Long levelId;

    @Builder.Default
    private List<Long> dimensionIds = new ArrayList<>();

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String content;

    @Builder.Default
    private MediaType mediaType = MediaType.NONE;

    private String mediaUrl;

    private String explanation;

    @Builder.Default
    private QuestionStatus status = QuestionStatus.ACTIVE;

    @Builder.Default
    private List<String> optionContents = new ArrayList<>();

    @Builder.Default
    private Integer correctIndex = 0;
}
