package com.onlinelearn.entity;

import com.onlinelearn.entity.enums.LessonStatus;
import com.onlinelearn.entity.enums.LessonTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Lesson parentLesson;

    @OneToMany(mappedBy = "parentLesson", cascade = CascadeType.ALL)
    @OrderBy("orderNum ASC")
    @Builder.Default
    private List<Lesson> subLessons = new ArrayList<>();

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LessonTypeEnum type;

    @Column(length = 500)
    private String videoLink;

    @Column(columnDefinition = "LONGTEXT")
    private String htmlContent;

    // Optional Quiz associated when type == QUIZ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private LessonStatus status = LessonStatus.ACTIVE;
}
