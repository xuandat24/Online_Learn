package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "price_packages")
public class PricePackage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id", nullable = false)
    public Long packageId;

    @Column(name = "subject_id", nullable = false)
    public Long subjectId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "name", length = 150, nullable = false)
    public String name;

    @Column(name = "duration_months", nullable = false)
    public Integer durationMonths;

    @Column(name = "list_price", precision = 18, scale = 2, nullable = false)
    public BigDecimal listPrice;

    @Column(name = "sale_price", precision = 18, scale = 2, nullable = false)
    public BigDecimal salePrice;

    @org.hibernate.annotations.Nationalized
    @Column(name = "description", length = 500)
    public String description;

    @Column(name = "status", length = 20, nullable = false)
    public String status = "Active";
}
