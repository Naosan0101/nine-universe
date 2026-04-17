-- カードの所属パック（表示用イニシャル）を追加
-- 例: STD / WH / ET / ...（将来のパック追加に備える）
ALTER TABLE card_definition
	ADD COLUMN IF NOT EXISTS pack_initial VARCHAR(12) NOT NULL DEFAULT 'STD';

-- 既存レコードを明示的に埋める（DB によっては既存行へ DEFAULT が遡及されない場合があるため）
UPDATE card_definition
SET pack_initial = COALESCE(NULLIF(pack_initial, ''), 'STD');

