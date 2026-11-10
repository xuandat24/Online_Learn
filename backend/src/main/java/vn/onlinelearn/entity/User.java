package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    public Long userId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "full_name", length = 150, nullable = false)
    public String fullName;

    @org.hibernate.annotations.Nationalized
    @Column(name = "gender", length = 20)
    public String gender;

    @org.hibernate.annotations.Nationalized
    @Column(name = "email", length = 254, nullable = false)
    public String email;

    @Column(name = "mobile", length = 20)
    public String mobile;

    @JsonIgnore
    @org.hibernate.annotations.Nationalized
    @Column(name = "password_hash", length = 255)
    public String passwordHash;

    @Column(name = "role_code", length = 30, nullable = false)
    public String roleCode;

    @Column(name = "status", length = 20, nullable = false)
    public String status = "Active";

    @Column(name = "email_verified_at")
    public LocalDateTime emailVerifiedAt;

    @org.hibernate.annotations.Nationalized
    @Column(name = "avatar_url", length = 500)
    public String avatarUrl;

    @org.hibernate.annotations.Nationalized
    @Column(name = "address", length = 255)
    public String address;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now(java.time.Clock.systemUTC());

    @JsonIgnore
    @Column(name = "token_version")
    public int tokenVersion = 0;
}
