-- 「コミックダイナソー」: 〈配置〉効果をエンジンで解決するため ability_deploy_code を付与
UPDATE card_definition
SET ability_deploy_code = 'COMIC_DINOSAUR'
WHERE id = 97 AND name = 'コミックダイナソー';
