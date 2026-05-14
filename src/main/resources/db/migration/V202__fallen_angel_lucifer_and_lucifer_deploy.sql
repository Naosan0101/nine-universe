-- 「堕天使ルシファー」: ルシファー効果で「奇跡」が変化する先。パック非収録・ライブラリ除外はアプリ側（pack_initial は表示用「-」）。
INSERT INTO card_definition (id, name, cost, base_power, attribute, card_kind, image_file, rarity, pack_initial, ability_deploy_code, ability_passive_code, deploy_help, passive_help)
VALUES (
	115,
	'堕天使ルシファー',
	1,
	3,
	'ANGEL_UNDEAD',
	'FIGHTER',
	'__missing__.PNG',
	'Ep',
	'-',
	'FALLEN_ANGEL_LUCIFER',
	NULL,
	'〈配置〉自分の手札のすべての「種族：アンデッド」のファイターを、バトル終了まで、強さ+1。',
	NULL
);

-- 「ルシファー」: 〈配置〉エンジン用コード
UPDATE card_definition
SET ability_deploy_code = 'LUCIFER'
WHERE id = 108 AND name = 'ルシファー';
