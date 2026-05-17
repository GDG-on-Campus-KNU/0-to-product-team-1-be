-- users 테이블
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  display_name VARCHAR(100),
  created_at TIMESTAMP DEFAULT NOW()
);

-- entries 테이블
CREATE TABLE entries (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id),
  text TEXT NOT NULL,
  self_condition INT, -- 1~5
  llm_result JSONB, -- LLM 라벨링 결과 전체
  created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_entries_user_created ON entries(user_id, created_at);

-- feedbacks 테이블
CREATE TABLE feedbacks (
  id BIGSERIAL PRIMARY KEY,
  entry_id BIGINT REFERENCES entries(id),
  drill_id VARCHAR(10) NOT NULL, -- D01~D71
  label VARCHAR(20), -- 'helpful' or 'not_helpful'
  created_at TIMESTAMP DEFAULT NOW()
);

-- user_preferences 테이블
CREATE TABLE user_preferences (
  user_id BIGINT PRIMARY KEY REFERENCES users(id),
  ask_sleep BOOLEAN DEFAULT NULL, -- null/true/false
  ask_activity BOOLEAN DEFAULT NULL,
  ask_meal BOOLEAN DEFAULT NULL,
  ask_caffeine BOOLEAN DEFAULT FALSE, -- 카페인은 옵션
  declined_at TIMESTAMP, -- 영구 거부 시
  updated_at TIMESTAMP DEFAULT NOW()
);
