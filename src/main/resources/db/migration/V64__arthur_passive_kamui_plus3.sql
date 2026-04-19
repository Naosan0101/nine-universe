-- アーサー: 〈常時〉「決戦の地 カムイ」が場にあるとき強さ+3（仕様）
UPDATE card_definition
SET passive_help = '「決戦の地 カムイ」が配置されているなら、強さ+3'
WHERE id = 43;
