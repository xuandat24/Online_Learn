package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id", nullable = false)
    public Long attemptId;

    @Column(name = "quiz_id", nullable = false)
    public Long quizId;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(name = "started_at", nullable = false)
    public LocalDateTime startedAt = LocalDateTime.now(java.time.Clock.systemUTC());

    @Column(name = "expires_at", nullable = false)
    public LocalDateTime expiresAt;

    @Column(name = "submitted_at")
    public LocalDateTime submittedAt;

    @Column(name = "status", length = 20, nullable = false)
    public String status = "In Progress";

    @Column(name = "score", precision = 5, scale = 2)
    public BigDecimal score;
}
