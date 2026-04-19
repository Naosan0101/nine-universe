-- アーサー王 → アーサー、常時ボーナス +3 → +4
UPDATE card_definition
SET name = 'アーサー',
    passive_help = '「決戦の地 カムイ」が配置されているなら、強さ+4'
WHERE id = 43;

-- ノクスクル(34)・クリスタクル(35): 〈配置〉能力コードを engine と紐付け
UPDATE card_definition SET ability_deploy_code = 'NOXSKUL' WHERE id = 34;
UPDATE card_definition SET ability_deploy_code = 'CRYSTAKUL' WHERE id = 35;
