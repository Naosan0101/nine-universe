-- 「リヴァイアサン」: 〈配置〉（レスト回収＋〈フィールド〉カウント進行）をエンジンで解決するため ability_deploy_code を付与
UPDATE card_definition
SET ability_deploy_code = 'LEVIATHAN'
WHERE id = 77 AND name = 'リヴァイアサン';
