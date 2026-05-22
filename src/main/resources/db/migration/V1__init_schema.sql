CREATE TABLE users(
    user_id BIGSERIAL PRIMARY KEY ,
    username VARCHAR(20),
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL ,
    created_at TIMESTAMP DEFAULT NOW(),
    confirmed_at TIMESTAMP
);

CREATE TABLE reports (
                         report_id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         pattern_analysis JSONB NOT NULL,
                         emotion_distribution JSONB NOT NULL,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE entries (
                         entry_id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         text VARCHAR(200) NOT NULL,
                         context JSONB,
                         llm_result JSONB,
                         drill_id INT,
                         drill_complete BOOLEAN NOT NULL DEFAULT FALSE,
                         helpful BOOLEAN,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                         feedback_at TIMESTAMPTZ
);

CREATE TABLE insights (
                          insight_id BIGSERIAL PRIMARY KEY,
                          report_id BIGINT NOT NULL,
                          text TEXT NOT NULL,
                          source TEXT,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          CONSTRAINT fk_insights_report
                              FOREIGN KEY (report_id)
                                  REFERENCES reports(report_id)
                                  ON DELETE CASCADE
);