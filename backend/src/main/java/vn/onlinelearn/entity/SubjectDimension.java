package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "subject_dimensions")
public class SubjectDimension {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dimension_id", nullable = false)
    public Long dimensionId;

    @Column(name = "subject_id", nullable = false)
    public Long subjectId;

    @Column(name = "type_setting_id", nullable = false)
    public Long typeSettingId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "name", length = 150, nullable = false)
    public String name;

    @org.hibernate.annotations.Nationalized
    @Column(name = "description", length = 500)
    public String description;
}
