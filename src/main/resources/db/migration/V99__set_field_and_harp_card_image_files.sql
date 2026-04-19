-- イラストファイル名を「カード名.PNG」で明示（静的リソースと image_file を一致させる）
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
