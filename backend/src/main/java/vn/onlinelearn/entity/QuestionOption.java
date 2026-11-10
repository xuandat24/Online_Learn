package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "question_options")
public class QuestionOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id", nullable = false)
    public Long optionId;

    @Column(name = "question_id", nullable = false)
    public Long questionId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "content", columnDefinition = "nvarchar(max)", nullable = false)
    public String content;

    @Column(name = "is_correct", nullable = false)
    public boolean isCorrect;

    @Column(name = "sort_order", nullable = false)
    public Integer sortOrder;
}
