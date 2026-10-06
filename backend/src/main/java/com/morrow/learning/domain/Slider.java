package com.morrow.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sliders")
public class Slider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 50)
    private String tag;

    @Column(nullable = false, length = 255)
    private String image;

    @Column(length = 255)
    private String backlink;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private Integer displayOrder = 1;

    protected Slider() {}

    public Slider(String title, String tag, String image, String backlink, String status, Integer displayOrder) {
        this.title = title;
        this.tag = tag;
        this.image = image;
        this.backlink = backlink;
        this.status = status;
        this.displayOrder = displayOrder;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getBacklink() { return backlink; }
    public void setBacklink(String backlink) { this.backlink = backlink; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
