-- 1. pg_trgm 확장 모듈 활성화 (이미 있으면 넘어감)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2. LOWER 함수를 적용한 GIN 인덱스 생성 (대소문자 무시 검색 최적화용)
CREATE INDEX IF NOT EXISTS idx_contents_name_lower_trgm
    ON contents USING gin (LOWER(title) gin_trgm_ops);