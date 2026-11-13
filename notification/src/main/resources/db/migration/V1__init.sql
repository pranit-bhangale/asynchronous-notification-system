CREATE TABLE IF NOT EXISTS notification_audits (
    id            UUID NOT NULL,
    event_id      VARCHAR(64) NOT NULL,
    from_address  VARCHAR(255),
    send_to       VARCHAR(255) NOT NULL,
    status        VARCHAR(32) NOT NULL,
    started_at    TIMESTAMPTZ,
    finished_at   TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version       BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_notifications
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_notification_audit_event_id ON notification_audits (event_id);
CREATE INDEX IF NOT EXISTS idx_notification_audit_status ON notification_audits (status);
CREATE INDEX IF NOT EXISTS idx_notification_audit_start ON notification_audits (started_at);
CREATE INDEX IF NOT EXISTS idx_notification_audit_send_to ON notification_audits (send_to);
