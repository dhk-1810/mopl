-- PostgreSQL pg_trgm 확장 모듈 활성화
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- contents 테이블 title 컬럼 GIN Trigram 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_contents_title_trgm ON contents USING gin (title gin_trgm_ops);
