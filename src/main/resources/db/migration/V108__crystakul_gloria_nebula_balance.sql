-- クリスタクル(id=35): レアリティ R
UPDATE card_definition
SET rarity = 'R'
WHERE id = 35;

-- 宝石の地 グロリア輝石台地(id=41): レアリティ Ep
UPDATE card_definition
SET rarity = 'Ep'
WHERE id = 41;

-- 探鉱の洞窟 ネビュラ坑道(id=42): ストーンコスト 1
UPDATE card_definition
SET cost = 1
WHERE id = 42;
