package com.onlinelearn.dto.expert;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionImportDTO {
    private int rowNum;
    private String content;
    private String subjectName;
    private String dimensionName;
    private String lessonName;
    private String levelCode;
    private String optA;
    private String optB;
    private String optC;
    private String optD;
    private String correctOpt;
    private String explanation;
}
