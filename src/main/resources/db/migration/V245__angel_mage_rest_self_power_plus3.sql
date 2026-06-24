-- エンジェルメイジ(105): 〈常時〉レストに同名があるときの強さボーナス +2 → +3

UPDATE card_definition
SET passive_help = '〈常時〉自分のレストゾーンに「エンジェルメイジ」があるなら、強さ+3。'
WHERE id = 105 AND name = 'エンジェルメイジ';
