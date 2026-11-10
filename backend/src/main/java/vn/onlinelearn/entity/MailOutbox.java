package vn.onlinelearn.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="mail_outbox")
public class MailOutbox {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="mail_id") public Long mailId;
    @org.hibernate.annotations.Nationalized @Column(length=254) public String recipient;
    @org.hibernate.annotations.Nationalized @Column(length=250) public String subject;
    @org.hibernate.annotations.Nationalized @Column(columnDefinition="nvarchar(max)") public String body;
    @Column(length=20) public String status="Pending";
    public int attempts;
    public LocalDateTime createdAt=vn.onlinelearn.service.Rules.now();
    public LocalDateTime sentAt;
}
