package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "lesson_progress")
public class LessonProgress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id", nullable = false)
    public Long progressId;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(name = "lesson_id", nullable = false)
    public Long lessonId;

    @Column(name = "is_completed", nullable = false)
    public boolean isCompleted;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now(java.time.Clock.systemUTC());
}
