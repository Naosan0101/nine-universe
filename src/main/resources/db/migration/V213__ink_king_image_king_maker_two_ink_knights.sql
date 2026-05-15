-- インクキング: イラスト（incking.PNG）
UPDATE card_definition
SET image_file = 'incking.PNG'
WHERE id = 111 AND name = 'インクキング';

-- キングメーカー: 〈配置〉条件を「インクナイト」2枚以上に変更
UPDATE card_definition
SET deploy_help = '〈配置〉自分の手札に「インクナイト」が2枚以上あるなら、「インクキング」を1枚手札に加える。'
WHERE id = 90 AND name = 'キングメーカー';
