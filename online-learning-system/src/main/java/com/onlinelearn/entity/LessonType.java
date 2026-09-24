package com.onlinelearn.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lesson_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String code; // SUBJECT_TOPIC, LESSON, QUIZ

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean status = true;
}
