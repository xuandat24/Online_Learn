package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "registrations")
public class Registration {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id", nullable = false)
    public Long registrationId;

    @Column(name = "customer_user_id")
    public Long customerUserId;

    @Column(name = "package_id", nullable = false)
    public Long packageId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "full_name", length = 150, nullable = false)
    public String fullName;

    @org.hibernate.annotations.Nationalized
    @Column(name = "email", length = 254, nullable = false)
    public String email;

    @Column(name = "mobile", length = 20, nullable = false)
    public String mobile;

    @org.hibernate.annotations.Nationalized
    @Column(name = "gender", length = 20)
    public String gender;

    @Column(name = "total_cost", precision = 18, scale = 2, nullable = false)
    public BigDecimal totalCost;

    @Column(name = "status", length = 20, nullable = false)
    public String status = "Submitted";

    @Column(name = "registered_at", nullable = false)
    public LocalDateTime registeredAt = LocalDateTime.now(java.time.Clock.systemUTC());

    @Column(name = "valid_from")
    public LocalDateTime validFrom;

    @Column(name = "valid_to")
    public LocalDateTime validTo;

    @Column(name = "updated_by")
    public Long updatedBy;

    @org.hibernate.annotations.Nationalized
    @Column(name = "notes", columnDefinition = "nvarchar(max)")
    public String notes;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @JsonIgnore
    @Column(name = "public_token_hash")
    public String publicTokenHash;

    @Column(name = "duration_months")
    public int durationMonths = 1;

    @Column(name = "created_by")
    public Long createdBy;
}
