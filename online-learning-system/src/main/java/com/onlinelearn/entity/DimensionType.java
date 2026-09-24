package com.onlinelearn.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dimension_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean status = true;
}
