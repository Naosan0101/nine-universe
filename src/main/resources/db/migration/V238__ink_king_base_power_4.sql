-- インクキング: 強さ 3 → 4
UPDATE card_definition
SET base_power = 4
WHERE id = 111 AND name = 'インクキング';
