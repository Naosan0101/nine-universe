-- セラフィム〈配置〉: エンジン側で分岐するため ability_deploy_code を設定する
UPDATE card_definition
SET ability_deploy_code = 'SERAPHIM'
WHERE id = 103 AND name = 'セラフィム';
