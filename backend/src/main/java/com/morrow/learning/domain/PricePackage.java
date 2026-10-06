package com.morrow.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(name = "price_packages", uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "name"}))
public class PricePackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private int accessDays;

    @Column(nullable = false)
    private boolean published = true;

    protected PricePackage() {
    }

    public PricePackage(String name, BigDecimal price, String currency, int accessDays, boolean published) {
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.accessDays = accessDays;
        this.published = published;
    }

    public void update(String name, BigDecimal price, String currency, int accessDays, boolean published) {
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.accessDays = accessDays;
        this.published = published;
    }

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public int getAccessDays() { return accessDays; }
    public boolean isPublished() { return published; }
    public void setCourse(Course course) { this.course = course; }
    public void setPublished(boolean published) { this.published = published; }
}