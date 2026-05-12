-- ドラゴンの卵(27): 〈配置〉から任意ストーン2を廃止し、レストのドラゴン回収のみに
UPDATE card_definition
SET deploy_help = '自分のレストゾーンにある「種族：ドラゴン」を1枚選んで、手札に加える。'
WHERE id = 27;
