-- SPEC-777(id=53): もとの強さを 2 に（表示・未出目時の基準）
UPDATE card_definition
SET base_power = 2
WHERE id = 53;
