-- 「ルシファー」〈配置〉: 奇跡の変化を「漆黒の時計塔」への文言に修正
UPDATE card_definition
SET deploy_help = '〈配置〉バトル終了まで、自分のすべての「奇跡」は「漆黒の時計塔」になる。'
WHERE id = 108 AND name = 'ルシファー';
