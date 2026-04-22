-- ピクシー(16): 基礎強さを 1 に。効果文をライブラリの1行表記に揃える。

UPDATE card_definition
SET base_power = 1,
	deploy_help = '自分のレストゾーンから「種族：エルフ」のカードを1枚選んで、手札に加える。'
WHERE id = 16;
