-- グリフォン: 強さ 2 → 3
UPDATE card_definition
SET base_power = 3
WHERE id = 17 AND name = 'グリフォン';
