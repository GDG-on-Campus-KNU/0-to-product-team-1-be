CREATE TABLE blacklisted_tokens (
    id BIGSERIAL PRIMARY KEY,
    token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_blacklisted_tokens_expires_at ON blacklisted_tokens(expires_at);
