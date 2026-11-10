package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "posts")
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    public Long postId;

    @Column(name = "author_user_id", nullable = false)
    public Long authorUserId;

    @Column(name = "category_setting_id", nullable = false)
    public Long categorySettingId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "title", length = 250, nullable = false)
    public String title;

    @org.hibernate.annotations.Nationalized
    @Column(name = "thumbnail_url", length = 500)
    public String thumbnailUrl;

    @org.hibernate.annotations.Nationalized
    @Column(name = "brief_info", length = 500)
    public String briefInfo;

    @org.hibernate.annotations.Nationalized
    @Column(name = "body", columnDefinition = "nvarchar(max)", nullable = false)
    public String body;

    @Column(name = "featured", nullable = false)
    public boolean featured;

    @Column(name = "status", length = 20, nullable = false)
    public String status = "Hidden";

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now(java.time.Clock.systemUTC());
}
