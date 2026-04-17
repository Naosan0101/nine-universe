-- アーサー王 → アーサー、常時ボーナス +3 → +4
UPDATE card_definition
SET name = 'アーサー',
    passive_help = '「決戦の地 カムイ」が配置されているなら、強さ+4'
WHERE id = 43;

