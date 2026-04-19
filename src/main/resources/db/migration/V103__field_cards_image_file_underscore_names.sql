-- 静的ファイル名が「カード名.PNG」（スペース）と異なる 4 枚: image_file を実ファイル名に合わせる（GameConstants の ID 限定分岐と対応）
UPDATE card_definition
SET image_file = '宝石の地_グロリア輝石台地.PNG'
WHERE id = 41;

UPDATE card_definition
SET image_file = '探鉱の洞窟_ネビュラ坑道.PNG'
WHERE id = 42;

UPDATE card_definition
SET image_file = '森のハープ弾き.PNG'
WHERE id = 65;

UPDATE card_definition
SET image_file = '霊園教会_デスバウンス.PNG'
WHERE id = 68;
