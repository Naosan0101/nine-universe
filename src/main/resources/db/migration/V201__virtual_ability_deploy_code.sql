-- ヴァーチャー: 〈配置〉ストーン2・「奇跡」1枚手札（CpuBattleEngine VIRTUAL）
UPDATE card_definition
SET ability_deploy_code = 'VIRTUAL'
WHERE id = 100 AND name = 'ヴァーチャー';
