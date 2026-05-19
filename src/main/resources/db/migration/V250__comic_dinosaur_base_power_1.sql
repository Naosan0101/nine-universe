-- コミックダイナソー: 強さ 2 → 1
UPDATE card_definition
SET base_power = 1
WHERE id = 97 AND name = 'コミックダイナソー';
