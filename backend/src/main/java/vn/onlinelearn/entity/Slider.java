package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "sliders")
public class Slider {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slider_id", nullable = false)
    public Long sliderId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "title", length = 250, nullable = false)
    public String title;

    @org.hibernate.annotations.Nationalized
    @Column(name = "image_url", length = 500, nullable = false)
    public String imageUrl;

    @org.hibernate.annotations.Nationalized
    @Column(name = "backlink", length = 500)
    public String backlink;

    @Column(name = "status", length = 20, nullable = false)
    public String status = "Hidden";

    @org.hibernate.annotations.Nationalized
    @Column(name = "notes", length = 500)
    public String notes;
}
