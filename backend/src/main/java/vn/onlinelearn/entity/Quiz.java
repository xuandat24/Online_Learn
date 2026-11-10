package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id", nullable = false)
    public Long quizId;

    @Column(name = "subject_id", nullable = false)
    public Long subjectId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    @Column(name = "quiz_type_setting_id", nullable = false)
    public Long quizTypeSettingId;

    @Column(name = "level_setting_id")
    public Long levelSettingId;

    @Column(name = "duration_minutes", nullable = false)
    public Integer durationMinutes;

    @Column(name = "pass_rate", precision = 5, scale = 2, nullable = false)
    public BigDecimal passRate;

    @org.hibernate.annotations.Nationalized
    @Column(name = "description", columnDefinition = "nvarchar(max)")
    public String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "quiz_questions", joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "question_id")
    @OrderColumn(name = "sort_order")
    public java.util.List<Long> questionIds = new java.util.ArrayList<>();
}
