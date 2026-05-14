-- 「ルシファー」〈配置〉: 奇跡の変化先を「堕天使ルシファー」に文言修正
UPDATE card_definition
SET deploy_help = '〈配置〉バトル終了まで、自分のすべての「奇跡」は「堕天使ルシファー」になる。'
WHERE id = 108 AND name = 'ルシファー';
