package com.onlinelearn.entity;

import com.onlinelearn.entity.enums.SliderStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sliders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slider extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String image;

    @Column(length = 255)
    private String link;

    @Column(name = "order_num", nullable = false)
    @Builder.Default
    private Integer orderNum = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private SliderStatus status = SliderStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
