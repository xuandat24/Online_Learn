package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "settings")
public class Setting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id", nullable = false)
    public Long settingId;

    @Column(name = "type", length = 50, nullable = false)
    public String type;

    @org.hibernate.annotations.Nationalized
    @Column(name = "value", length = 200, nullable = false)
    public String value;

    @Column(name = "sort_order")
    public Integer sortOrder;

    @org.hibernate.annotations.Nationalized
    @Column(name = "description", length = 500)
    public String description;

    @Column(name = "status", length = 20, nullable = false)
    public String status = "Active";
}
