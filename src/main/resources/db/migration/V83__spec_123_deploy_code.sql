-- SPEC-123(id=55): 〈配置〉1～3 のランダム分ストーン獲得をエンジンで解決する
UPDATE card_definition
SET ability_deploy_code = 'SPEC123'
WHERE id = 55;
