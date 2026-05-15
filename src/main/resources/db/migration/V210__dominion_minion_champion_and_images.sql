-- ドミニオン〈配置〉文言: ミニオンキング → ミニオンチャンピオン
UPDATE card_definition
SET deploy_help = '〈配置〉自分の手札に「ミニオンソルジャー」があるなら、「ミニオンチャンピオン」を1枚手札に加える。そうでないなら、手札のすべてのカードを「ミニオンソルジャー」に変化させる。'
WHERE id = 102 AND name = 'ドミニオン';

-- id=114: 名称をミニオンチャンピオンに
UPDATE card_definition SET name = 'ミニオンチャンピオン' WHERE id = 114;

-- カード面イラスト
UPDATE card_definition SET image_file = 'minionsolger.PNG' WHERE id = 113 AND name = 'ミニオンソルジャー';
UPDATE card_definition SET image_file = 'minionking.PNG' WHERE id = 114 AND name = 'ミニオンチャンピオン';
