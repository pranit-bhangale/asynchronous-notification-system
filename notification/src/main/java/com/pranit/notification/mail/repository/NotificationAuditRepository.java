package com.pranit.notification.mail.repository;

import com.pranit.notification.mail.entity.NotificationAudit;
import com.pranit.notification.mail.model.EmailProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationAuditRepository extends JpaRepository<NotificationAudit, UUID> {

    Optional<NotificationAudit> findByEventId(String eventId);

    List<NotificationAudit> findByStatus(EmailProcessingStatus status);

    List<NotificationAudit> findBySendTo(String sendTo);

    List<NotificationAudit> findByStartBetween(Instant fromDate, Instant toDate);
}
