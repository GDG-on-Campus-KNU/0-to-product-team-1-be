-- self_condition을 context_json 안에 포함하므로 별도 컬럼 제거
ALTER TABLE entries DROP COLUMN IF EXISTS self_condition;
