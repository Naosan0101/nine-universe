-- 「ページウォーカー」〈配置〉: エンジン解決用の ability_deploy_code を付与

UPDATE card_definition

SET ability_deploy_code = 'PAGE_WALKER'

WHERE id = 89 AND name = 'ページウォーカー';

