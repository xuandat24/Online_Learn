package com.morrow.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 1000)
    private String summary;

    @Column(nullable = false, length = 12000)
    private String content;

    @Column(length = 2048)
    private String videoUrl;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected Lesson() {
    }

    public Lesson(Course course, String title, String summary, String content,
                  String videoUrl, int displayOrder, boolean published) {
        this.course = course;
        update(title, summary, content, videoUrl, displayOrder, published);
    }

    public void update(String title, String summary, String content, String videoUrl,
                       int displayOrder, boolean published) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.videoUrl = videoUrl;
        this.displayOrder = displayOrder;
        this.published = published;
    }

    @PrePersist
    void onCreate() { createdAt = updatedAt = LocalDateTime.now(ZoneOffset.UTC); }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(ZoneOffset.UTC); }

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getContent() { return content; }
    public String getVideoUrl() { return videoUrl; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isPublished() { return published; }
}