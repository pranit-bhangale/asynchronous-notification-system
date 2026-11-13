package com.pranit.notification.mail.entity;

import com.pranit.notification.mail.model.EmailProcessingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit entity mapping to the {@code notification_audits} database table.
 * Contains only persistence mapping fields for auditing with no business logic.
 */
@Entity
@Table(
        name = "notification_audits",
        indexes = {
                @Index(name = "idx_notification_audit_event_id", columnList = "event_id", unique = true),
                @Index(name = "idx_notification_audit_status", columnList = "status"),
                @Index(name = "idx_notification_audit_start", columnList = "started_at"),
                @Index(name = "idx_notification_audit_send_to", columnList = "send_to")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;


    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "from_address")
    private String from;

    @Column(name = "send_to", nullable = false)
    private String sendTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private EmailProcessingStatus status;

    @Column(name = "started_at")
    private Instant start;

    @Column(name = "finished_at")
    private Instant finish;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
