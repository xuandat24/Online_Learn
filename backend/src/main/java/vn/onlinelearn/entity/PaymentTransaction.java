package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    public Long paymentId;

    @Column(name = "registration_id", nullable = false)
    public Long registrationId;

    @Column(name = "order_code", length = 50, nullable = false)
    public String orderCode;

    @Column(name = "provider", length = 30, nullable = false)
    public String provider;

    @Column(name = "provider_txn_ref", length = 100)
    public String providerTxnRef;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    public BigDecimal amount;

    @Column(name = "currency", length = 3, columnDefinition = "char(3)", nullable = false)
    public String currency = "VND";

    @Column(name = "status", length = 20, nullable = false)
    public String status = "Pending";

    @org.hibernate.annotations.Nationalized
    @Column(name = "checkout_url", length = 2000)
    public String checkoutUrl;

    @Column(name = "paid_at")
    public LocalDateTime paidAt;

    @org.hibernate.annotations.Nationalized
    @Column(name = "raw_response", columnDefinition = "nvarchar(max)")
    public String rawResponse;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now(java.time.Clock.systemUTC());

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now(java.time.Clock.systemUTC());

    @Column(name = "expires_at")
    public LocalDateTime expiresAt;
}
