-- セレスティア: 〈配置〉を「奇跡」2枚手札追加のみに変更（任意ストーン1廃止）
UPDATE card_definition
SET deploy_help = '〈配置〉「奇跡」を2枚手札に加える。'
WHERE id = 101 AND name = 'セレスティア';
