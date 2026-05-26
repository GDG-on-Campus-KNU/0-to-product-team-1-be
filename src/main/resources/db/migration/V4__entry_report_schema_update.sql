-- entries 신규 컬럼 추가
ALTER TABLE entries ADD COLUMN IF NOT EXISTS recorded_date DATE;
ALTER TABLE entries ADD COLUMN IF NOT EXISTS drill_category VARCHAR(100);
ALTER TABLE entries ADD COLUMN IF NOT EXISTS drill_calendar_color VARCHAR(50);
ALTER TABLE entries ADD COLUMN IF NOT EXISTS awaiting_answer BOOLEAN DEFAULT FALSE;
ALTER TABLE entries ADD COLUMN IF NOT EXISTS offered_category VARCHAR(100);
ALTER TABLE entries ADD COLUMN IF NOT EXISTS crisis_flag BOOLEAN DEFAULT FALSE;
ALTER TABLE entries ADD COLUMN IF NOT EXISTS recommendation_json JSONB;

-- 일 1회 정책 UNIQUE 제약
ALTER TABLE entries ADD CONSTRAINT uq_entries_user_recorded_date
    UNIQUE (user_id, recorded_date);

-- reports 신규 컬럼 추가
ALTER TABLE reports ADD COLUMN IF NOT EXISTS report_type VARCHAR(20);
ALTER TABLE reports ADD COLUMN IF NOT EXISTS period_id VARCHAR(20);
ALTER TABLE reports ADD COLUMN IF NOT EXISTS blocks_json JSONB;
ALTER TABLE reports ADD COLUMN IF NOT EXISTS visualizations_json JSONB;

-- users 소프트 삭제 컬럼
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
