-- セレスティア(id=101): 〈配置〉任意ストーン1で奇跡2枚（エンジン用 ability_deploy_code）

UPDATE card_definition
SET ability_deploy_code = 'CELESTIA'
WHERE id = 101 AND name = 'セレスティア';
