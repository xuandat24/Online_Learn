package vn.onlinelearn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "course_access")
public class CourseAccess {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_id", nullable = false)
    public Long accessId;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(name = "subject_id", nullable = false)
    public Long subjectId;

    @Column(name = "registration_id", nullable = false)
    public Long registrationId;

    @Column(name = "valid_from", nullable = false)
    public LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    public LocalDateTime validTo;
}
