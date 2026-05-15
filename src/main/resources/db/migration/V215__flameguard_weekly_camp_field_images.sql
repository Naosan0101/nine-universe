-- 紅蓮峡谷 フレイムガルド（id=84）・週刊少年 CAMP（id=93）: イラスト実装済みファイルを image_file に紐付け（従来 __missing__.PNG のまま白抜けになっていた）

UPDATE card_definition SET image_file = 'gurenkyoukokuhureimugarudo.PNG' WHERE id = 84 AND name = '紅蓮峡谷 フレイムガルド';
UPDATE card_definition SET image_file = 'syuukansyounennkyanp.PNG' WHERE id = 93 AND name = '週刊少年 CAMP';
