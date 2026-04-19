-- クリスタクル(id=35): ability_deploy_code が欠落している環境でもエンジンと一致させる
UPDATE card_definition
SET ability_deploy_code = 'CRYSTAKUL'
WHERE id = 35
  AND (ability_deploy_code IS NULL OR ability_deploy_code = '');
