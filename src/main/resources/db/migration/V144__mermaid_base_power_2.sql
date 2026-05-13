-- マーメイド: 強さ（base_power）4 → 2
UPDATE card_definition
SET base_power = 2
WHERE id = 72 AND name = 'マーメイド';
