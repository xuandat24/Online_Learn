package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "subjects")
public class Subject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id", nullable = false)
    public Long subjectId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "name", length = 200, nullable = false)
    public String name;

    @Column(name = "owner_user_id", nullable = false)
    public Long ownerUserId;

    @Column(name = "category_setting_id", nullable = false)
    public Long categorySettingId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "tagline", length = 255)
    public String tagline;

    @org.hibernate.annotations.Nationalized
    @Column(name = "thumbnail_url", length = 500)
    public String thumbnailUrl;

    @org.hibernate.annotations.Nationalized
    @Column(name = "description", columnDefinition = "nvarchar(max)")
    public String description;

    @Column(name = "featured", nullable = false)
    public boolean featured;

    @Column(name = "publish_status", length = 20, nullable = false)
    public String publishStatus = "Unpublished";

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now(java.time.Clock.systemUTC());
}
