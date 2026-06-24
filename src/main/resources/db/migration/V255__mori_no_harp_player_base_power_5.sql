-- 森のハープ弾き: 強さ 4 → 5
UPDATE card_definition
SET base_power = 5
WHERE id = 65 AND name = '森のハープ弾き';
