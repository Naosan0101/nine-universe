-- 「漫画家」: 〈配置〉効果をエンジンで解決するため ability_deploy_code を付与
UPDATE card_definition
SET ability_deploy_code = 'MANGAKA'
WHERE id = 96 AND name = '漫画家';
