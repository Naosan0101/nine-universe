-- 化石: 強さ（base_power）3 → 1
UPDATE card_definition
SET base_power = 1
WHERE id = 79 AND name = '化石';
