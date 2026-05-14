-- 「ソードフィッシュ」トークン（パック・ライブラリ一覧除外はアプリ側）。シーサーペントの〈配置〉コードを付与。

INSERT INTO card_definition (id, name, cost, base_power, attribute, card_kind, image_file, rarity, pack_initial, ability_deploy_code, ability_passive_code, deploy_help, passive_help)
VALUES
	(
		110,
		'ソードフィッシュ',
		0,
		1,
		'MERFOLK',
		'FIGHTER',
		'__missing__.PNG',
		'C',
		'NONE',
		NULL,
		NULL,
		'効果なし。',
		NULL
	);

UPDATE card_definition
SET ability_deploy_code = 'SEASERPENT'
WHERE id = 74 AND name = 'シーサーペント';
