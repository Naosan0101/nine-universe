-- V100（アンダースコア file 名）を取り消し、V99 と同じ「カード名.PNG」（スペース区切り）に戻す
UPDATE card_definition
SET image_file = '宝石の地 グロリア輝石台地.PNG'
WHERE id = 41;

UPDATE card_definition
SET image_file = '探鉱の洞窟 ネビュラ坑道.PNG'
WHERE id = 42;

UPDATE card_definition
SET image_file = '森のハープ弾き.PNG'
WHERE id = 65;

UPDATE card_definition
SET image_file = '霊園教会 デスバウンス.PNG'
WHERE id = 68;
