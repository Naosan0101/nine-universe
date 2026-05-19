-- アクアガーディアン: 強さ 3 → 2
UPDATE card_definition
SET base_power = 2
WHERE id = 75 AND name = 'アクアガーディアン';
