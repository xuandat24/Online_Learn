package com.onlinelearn.entity;

import com.onlinelearn.entity.enums.SettingType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SettingType type;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_num")
    @Builder.Default
    private Integer orderNum = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean status = true;
}
