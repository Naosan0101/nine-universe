-- 「インクキング」トークン（パック・ライブラリ一覧除外はアプリ側）。キングメーカー〈配置〉用コード付与。

INSERT INTO card_definition (id, name, cost, base_power, attribute, card_kind, image_file, rarity, pack_initial, ability_deploy_code, ability_passive_code, deploy_help, passive_help)
VALUES (
		111,
		'インクキング',
		2,
		3,
		'COMIC',
		'FIGHTER',
		'__missing__.PNG',
		'Reg',
		'NONE',
		'INK_KING',
		NULL,
		'〈配置〉ターンの終わりまで、強さ+4。自分のレストゾーンのすべての「インクナイト」を手札に加える。',
		NULL
	);

UPDATE card_definition
SET ability_deploy_code = 'KING_MAKER'
WHERE id = 90 AND name = 'キングメーカー';
