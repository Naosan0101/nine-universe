-- ドミニオン〈配置〉分岐用トークン。パック・ライブラリ一覧除外はアプリ側（GameConstants）。

INSERT INTO card_definition (id, name, cost, base_power, attribute, card_kind, image_file, rarity, pack_initial, ability_deploy_code, ability_passive_code, deploy_help, passive_help)
VALUES (
		113,
		'ミニオンソルジャー',
		0,
		2,
		'ANGEL',
		'FIGHTER',
		'__missing__.PNG',
		'R',
		'NONE',
		'MINION_SOLDIER',
		NULL,
		'〈配置〉ターンの終わりまで、強さ+3。',
		NULL
	),
	(
		114,
		'ミニオンキング',
		2,
		4,
		'ANGEL',
		'FIGHTER',
		'__missing__.PNG',
		'Ep',
		'NONE',
		NULL,
		NULL,
		NULL,
		'〈常時〉相手のターンの間、強さ+4。'
	);

UPDATE card_definition
SET
	deploy_help = '〈配置〉自分の手札に「ミニオンソルジャー」があるなら、「ミニオンキング」を1枚手札に加える。そうでないなら、手札のすべてのカードを「ミニオンソルジャー」に変化させる。',
	ability_deploy_code = 'DOMINION'
WHERE id = 102 AND name = 'ドミニオン';
