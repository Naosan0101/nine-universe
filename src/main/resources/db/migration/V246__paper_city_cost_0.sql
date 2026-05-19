-- ペーパーシティ(95): コスト 1 → 0

UPDATE card_definition
SET cost = 0
WHERE id = 95 AND name = 'ペーパーシティ';
