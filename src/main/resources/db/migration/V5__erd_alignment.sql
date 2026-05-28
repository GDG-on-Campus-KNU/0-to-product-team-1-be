-- V5: ERDCloud 기준 스키마 정렬
-- 삭제 대상 테이블, 불필요 컬럼 제거 + 신규 테이블 생성

-- ============================================================
-- 1. 삭제 대상 테이블 (ERD에 없는 테이블)
-- ============================================================
DROP TABLE IF EXISTS insights CASCADE;
DROP TABLE IF EXISTS reports CASCADE;
DROP TABLE IF EXISTS blacklisted_tokens CASCADE;
DROP TABLE IF EXISTS rejected_drills CASCADE;
DROP TABLE IF EXISTS quiz_answers CASCADE;

-- ============================================================
-- 2. entries 테이블 정리
-- ============================================================
-- 삭제할 컬럼
ALTER TABLE entries DROP COLUMN IF EXISTS context;
ALTER TABLE entries DROP COLUMN IF EXISTS llm_result;
ALTER TABLE entries DROP COLUMN IF EXISTS drill_complete;
ALTER TABLE entries DROP COLUMN IF EXISTS helpful;
ALTER TABLE entries DROP COLUMN IF EXISTS feedback_at;
ALTER TABLE entries DROP COLUMN IF EXISTS time_of_day;

-- 신규 컬럼
ALTER TABLE entries ADD COLUMN IF NOT EXISTS label_result_json JSONB;

-- V3에서 만든 time_of_day 관련 제약/인덱스 제거
ALTER TABLE entries DROP CONSTRAINT IF EXISTS chk_entries_time_of_day;
DROP INDEX IF EXISTS uq_entries_user_date_time_of_day;

-- drill_category, drill_calendar_color 길이 축소 (데이터 없는 상태)
ALTER TABLE entries ALTER COLUMN drill_category TYPE VARCHAR(50);
ALTER TABLE entries ALTER COLUMN drill_calendar_color TYPE VARCHAR(20);
ALTER TABLE entries ALTER COLUMN offered_category TYPE VARCHAR(50);

-- ============================================================
-- 3. baselines 테이블 재생성 (구조 완전 변경)
-- ============================================================
DROP TABLE IF EXISTS baselines CASCADE;

CREATE TABLE baselines (
    user_id BIGINT PRIMARY KEY,
    snapshot_json JSONB,
    captured_at TIMESTAMPTZ,

    CONSTRAINT fk_baselines_user
        FOREIGN KEY (user_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE
);

-- ============================================================
-- 4. 신규 테이블: reports_weekly (복합 PK)
-- ============================================================
CREATE TABLE reports_weekly (
    week_id VARCHAR(10) NOT NULL,
    user_id BIGINT NOT NULL,
    blocks_json JSONB,
    visualizations_json JSONB,
    generated_at TIMESTAMPTZ,

    PRIMARY KEY (week_id, user_id),

    CONSTRAINT fk_reports_weekly_user
        FOREIGN KEY (user_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE
);

-- ============================================================
-- 5. 신규 테이블: reports_monthly (복합 PK)
-- ============================================================
CREATE TABLE reports_monthly (
    month_id VARCHAR(10) NOT NULL,
    user_id BIGINT NOT NULL,
    blocks_json JSONB,
    generated_at TIMESTAMPTZ,

    PRIMARY KEY (month_id, user_id),

    CONSTRAINT fk_reports_monthly_user
        FOREIGN KEY (user_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE
);

-- ============================================================
-- 6. 신규 테이블: insights_user
-- ============================================================
CREATE TABLE insights_user (
    insight_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    discovery_text TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_insights_user_user
        FOREIGN KEY (user_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE
);
