-- ルシファー: 配置効果に「奇跡」1枚手札加算を追記
UPDATE card_definition
SET deploy_help = '〈配置〉「奇跡」を1枚手札に加える。バトル終了まで、自分のすべての「奇跡」は「堕天使ルシファー」になる。'
WHERE id = 108 AND name = 'ルシファー';
