-- users 테이블
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    confirmed_at TIMESTAMPTZ
);

-- entries 테이블
CREATE TABLE entries (
    entry_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    text VARCHAR(200),
    context JSONB NOT NULL,
    llm_result JSONB,
    drill_id INT,
    drill_complete BOOLEAN,
    helpful BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    feedback_at TIMESTAMPTZ,

    CONSTRAINT fk_entries_user
        FOREIGN KEY (user_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE
);

-- baselines 테이블
CREATE TABLE baselines (
    baseline_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    patterns_avg JSONB,
    behaviors_avg JSONB,
    rejected_drills JSONB,
    updated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_baselines_user
        FOREIGN KEY (user_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE
);

-- reports 테이블
CREATE TABLE reports (
    report_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pattern_analysis JSONB,
    emotion_distribution JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_reports_user
        FOREIGN KEY (user_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE
);

-- insights 테이블
CREATE TABLE insights (
    insight_id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    text TEXT,
    source TEXT,
    week_of VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_insights_report
        FOREIGN KEY (report_id)
            REFERENCES reports(report_id)
            ON DELETE CASCADE
);
