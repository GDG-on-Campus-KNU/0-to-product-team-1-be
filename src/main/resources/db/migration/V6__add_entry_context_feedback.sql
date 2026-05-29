-- entries: context_json 추가 (수면·사교·운동·컨디션 — 주간 맞춤형 핵심)
ALTER TABLE entries ADD COLUMN IF NOT EXISTS context_json JSONB;

-- entries: self_condition 추가 (ERD에 명시되어 있으나 이전 마이그레이션 누락)
ALTER TABLE entries ADD COLUMN IF NOT EXISTS self_condition INT;

-- entries: drill_completed, helpful 재추가 (V5에서 삭제됐으나 ML 개인화 학습에 필요)
ALTER TABLE entries ADD COLUMN IF NOT EXISTS drill_completed BOOLEAN DEFAULT FALSE;
ALTER TABLE entries ADD COLUMN IF NOT EXISTS helpful BOOLEAN;
