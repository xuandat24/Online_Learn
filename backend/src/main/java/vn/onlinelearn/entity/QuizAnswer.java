package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "quiz_answers")
public class QuizAnswer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id", nullable = false)
    public Long answerId;

    @Column(name = "attempt_id", nullable = false)
    public Long attemptId;

    @Column(name = "question_id", nullable = false)
    public Long questionId;

    @Column(name = "option_id")
    public Long optionId;

    @Column(name = "marked_for_review", nullable = false)
    public boolean markedForReview;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now(java.time.Clock.systemUTC());
}
