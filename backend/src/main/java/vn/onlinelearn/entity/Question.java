package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "questions")
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id", nullable = false)
    public Long questionId;

    @Column(name = "subject_id", nullable = false)
    public Long subjectId;

    @Column(name = "lesson_id")
    public Long lessonId;

    @Column(name = "level_setting_id", nullable = false)
    public Long levelSettingId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "content", columnDefinition = "nvarchar(max)", nullable = false)
    public String content;

    @org.hibernate.annotations.Nationalized
    @Column(name = "media_url", length = 500)
    public String mediaUrl;

    @Column(name = "media_type", length = 20)
    public String mediaType;

    @org.hibernate.annotations.Nationalized
    @Column(name = "explanation", columnDefinition = "nvarchar(max)")
    public String explanation;

    @Column(name = "status", length = 20, nullable = false)
    public String status = "Active";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_dimensions", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "dimension_id")
    public java.util.Set<Long> dimensionIds = new java.util.HashSet<>();
}
