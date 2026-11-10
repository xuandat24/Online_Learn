package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "auth_tokens")
public class AuthToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id", nullable = false)
    public Long tokenId;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @JsonIgnore
    @Column(name = "token_hash", length = 128, nullable = false)
    public String tokenHash;

    @Column(name = "purpose", length = 25, nullable = false)
    public String purpose;

    @Column(name = "expires_at", nullable = false)
    public LocalDateTime expiresAt;

    @Column(name = "consumed_at")
    public LocalDateTime consumedAt;
}
