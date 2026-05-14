-- 「マーメイド」: 〈配置〉効果をエンジンで解決するため ability_deploy_code を付与
UPDATE card_definition
SET ability_deploy_code = 'MERMAID'
WHERE id = 72 AND name = 'マーメイド';
