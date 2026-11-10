package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "lessons")
public class Lesson {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id", nullable = false)
    public Long lessonId;

    @Column(name = "subject_id", nullable = false)
    public Long subjectId;

    @Column(name = "package_id")
    public Long packageId;

    @Column(name = "parent_topic_id")
    public Long parentTopicId;

    @Column(name = "quiz_id")
    public Long quizId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    @Column(name = "lesson_type", length = 30, nullable = false)
    public String lessonType;

    @Column(name = "sort_order", nullable = false)
    public Integer sortOrder;

    @org.hibernate.annotations.Nationalized
    @Column(name = "video_url", length = 500)
    public String videoUrl;

    @org.hibernate.annotations.Nationalized
    @Column(name = "html_content", columnDefinition = "nvarchar(max)")
    public String htmlContent;

    @Column(name = "status", length = 20, nullable = false)
    public String status = "Active";
}
