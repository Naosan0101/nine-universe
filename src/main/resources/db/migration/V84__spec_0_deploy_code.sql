-- SPEC-0(id=56): 〈配置〉レストの強さ1ファイターをデッキ上へ（エンジンで解決）
UPDATE card_definition
SET ability_deploy_code = 'SPEC0'
WHERE id = 56;
