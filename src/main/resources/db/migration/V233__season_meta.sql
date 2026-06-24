-- シーズン区切りリセットの最終実行区切り開始日（1行のみ）
CREATE TABLE season_meta (
	id SMALLINT PRIMARY KEY CHECK (id = 1),
	last_reset_period_start DATE NOT NULL
);

INSERT INTO season_meta (id, last_reset_period_start) VALUES (1, DATE '2026-06-01');
