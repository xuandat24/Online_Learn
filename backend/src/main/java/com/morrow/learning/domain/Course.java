package com.morrow.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false, length = 120)
    private String instructor;

    @Column(nullable = false, length = 40)
    private String level;

    @Column(nullable = false, length = 30)
    private String duration;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private double rating;

    @Column(nullable = false)
    private int students;

    @Column(length = 255)
    private String image;

    @Column(length = 30)
    private String accent;

    @Column(nullable = false)
    private boolean published = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PricePackage> pricePackages = new ArrayList<>();

    protected Course() {
    }

    public Course(String title, String description, String category, String instructor, String level,
                  String duration, BigDecimal price, double rating, int students, String image, String accent) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.instructor = instructor;
        this.level = level;
        this.duration = duration;
        this.price = price;
        this.rating = rating;
        this.students = students;
        this.image = image;
        this.accent = accent;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void update(String title, String description, String category, String instructor, String level,
                       String duration, BigDecimal price, String image, String accent, boolean published) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.instructor = instructor;
        this.level = level;
        this.duration = duration;
        this.price = price;
        this.image = image;
        this.accent = accent;
        this.published = published;
    }

    public void addStudent() { students++; }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getInstructor() { return instructor; }
    public String getLevel() { return level; }
    public String getDuration() { return duration; }
    public BigDecimal getPrice() { return price; }
    public double getRating() { return rating; }
    public int getStudents() { return students; }
    public String getImage() { return image; }
    public String getAccent() { return accent; }
    public boolean isPublished() { return published; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public List<PricePackage> getPricePackages() { return pricePackages; }

    public void addPricePackage(PricePackage pricePackage) {
        pricePackages.add(pricePackage);
        pricePackage.setCourse(this);
    }

    public void reconcilePricePackages(List<PricePackage> incoming) {
        for (PricePackage existing : pricePackages) {
            if (incoming.stream().noneMatch(item -> existing.getId() != null && existing.getId().equals(item.getId()))) {
                existing.setPublished(false);
            }
        }
        for (PricePackage item : incoming) {
            PricePackage existing = item.getId() == null ? null : pricePackages.stream()
                    .filter(candidate -> candidate.getId().equals(item.getId())).findFirst().orElse(null);
            if (existing == null) {
                addPricePackage(item);
            } else {
                existing.update(item.getName(), item.getPrice(), item.getCurrency(), item.getAccessDays(), item.isPublished());
            }
        }
    }
}