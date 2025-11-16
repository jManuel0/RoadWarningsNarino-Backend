CREATE TABLE IF NOT EXISTS alert_media (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    url VARCHAR(1000) NOT NULL,
    public_id VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_alert_media_alert
        FOREIGN KEY (alert_id) REFERENCES alerts (id)
);

