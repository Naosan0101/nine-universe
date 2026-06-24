-- ドラゴンライダー(10): 強さ 2 → 4、常時ボーナス +4 → +3

UPDATE card_definition
SET base_power = 4,
    passive_help = '自分のレストゾーンに「種族：ドラゴン」があるなら、強さ+3。'
WHERE id = 10;
