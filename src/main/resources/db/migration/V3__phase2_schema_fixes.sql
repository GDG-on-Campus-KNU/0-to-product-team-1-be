-- ============================================================
-- 2.A users: password_hash NOT NULL → NULL 허용 (OAuth 지원)
-- ============================================================
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

-- ============================================================
-- 2.B entries: time_of_day 컬럼 추가 + UNIQUE + INDEX
-- VARCHAR + CHECK 사용 (Hibernate 타입 매핑 안전)
-- ============================================================
ALTER TABLE entries ADD COLUMN IF NOT EXISTS time_of_day VARCHAR(20);

ALTER TABLE entries DROP CONSTRAINT IF EXISTS chk_entries_time_of_day;
ALTER TABLE entries ADD CONSTRAINT chk_entries_time_of_day
    CHECK (time_of_day IN ('morning', 'afternoon', 'evening'));

-- 2.9 idx_entries_user_created (user_id, created_at DESC)
CREATE INDEX IF NOT EXISTS idx_entries_user_created
    ON entries (user_id, created_at DESC);

-- 2.8 UNIQUE(user_id, DATE(created_at KST), time_of_day) — 중복 방지
-- PostgreSQL: AT TIME ZONE 'Asia/Seoul' 사용
CREATE UNIQUE INDEX IF NOT EXISTS uq_entries_user_date_time_of_day
    ON entries (user_id, CAST((created_at AT TIME ZONE 'Asia/Seoul') AS date), time_of_day);

-- ============================================================
-- 2.C reports: week_of, read_at, UNIQUE(user_id, week_of) 추가
-- ============================================================
ALTER TABLE reports ADD COLUMN IF NOT EXISTS week_of VARCHAR(20);
ALTER TABLE reports ADD COLUMN IF NOT EXISTS read_at TIMESTAMPTZ;

-- 중복 week_of 데이터가 있을 경우를 대비해 조건부로 UNIQUE 추가
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_reports_user_week_of' AND conrelid = 'reports'::regclass
    ) THEN
        ALTER TABLE reports ADD CONSTRAINT uq_reports_user_week_of
            UNIQUE (user_id, week_of);
    END IF;
END $$;

-- ============================================================
-- 2.D insights: user_id FK + category 컬럼 추가
-- ============================================================
ALTER TABLE insights ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE insights ADD COLUMN IF NOT EXISTS category VARCHAR(100);

-- user_id FK 제약 추가 (기존 데이터 없으면 바로 추가 가능)
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_insights_user' AND conrelid = 'insights'::regclass
    ) THEN
        ALTER TABLE insights ADD CONSTRAINT fk_insights_user
            FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
    END IF;
END $$;

-- ============================================================
-- 2.E baselines: sample_count, window_days, UNIQUE(user_id) 추가
-- ============================================================
ALTER TABLE baselines ADD COLUMN IF NOT EXISTS sample_count INT DEFAULT 0;
ALTER TABLE baselines ADD COLUMN IF NOT EXISTS window_days INT DEFAULT 30;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_baselines_user_id' AND conrelid = 'baselines'::regclass
    ) THEN
        ALTER TABLE baselines ADD CONSTRAINT uq_baselines_user_id
            UNIQUE (user_id);
    END IF;
END $$;

-- ============================================================
-- 2.F rejected_drills 테이블 신규 생성
-- ============================================================
CREATE TABLE IF NOT EXISTS rejected_drills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    drill_id INT NOT NULL,
    rejected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_rejected_drills_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    CONSTRAINT uq_rejected_drills_user_drill
        UNIQUE (user_id, drill_id)
);

-- ============================================================
-- 2.G quiz_answers 테이블 신규 생성
-- ============================================================
CREATE TABLE IF NOT EXISTS quiz_answers (
    answer_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    week_of VARCHAR(20) NOT NULL,
    predicted TEXT,
    correct TEXT,
    gap FLOAT,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_quiz_answers_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    CONSTRAINT uq_quiz_answers_user_week
        UNIQUE (user_id, week_of)
);
