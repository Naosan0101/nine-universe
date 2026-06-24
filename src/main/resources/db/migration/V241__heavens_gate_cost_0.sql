-- 天界門 ヘヴンズゲート: コスト 1 → 0
UPDATE card_definition
SET cost = 0
WHERE id = 107 AND name = '天界門 ヘヴンズゲート';
