-- V233 適用漏れや手動 DB 復旧後でも season_meta が必ず存在するようにする
CREATE TABLE IF NOT EXISTS season_meta (
	id SMALLINT PRIMARY KEY CHECK (id = 1),
	last_reset_period_start DATE NOT NULL
);

INSERT INTO season_meta (id, last_reset_period_start) VALUES (1, DATE '2026-06-01')
ON CONFLICT (id) DO NOTHING;
