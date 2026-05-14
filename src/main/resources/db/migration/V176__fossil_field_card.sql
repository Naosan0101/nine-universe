-- 「化石」がレストへ行く代わりに場に出るトークン〈フィールド〉（パック・ライブラリ一覧から除外はアプリ側）

INSERT INTO card_definition (id, name, cost, base_power, attribute, card_kind, image_file, rarity, pack_initial, ability_deploy_code, ability_passive_code, deploy_help, passive_help)
VALUES
	(
		109,
		'化石（フィールド）',
		0,
		0,
		'MERFOLK',
		'FIELD',
		'kaseki.PNG',
		'C',
		'NONE',
		NULL,
		NULL,
		'〈フィールド〉レストゾーンのカードはすべて「種族：マーフォーク」になる。',
		NULL
	);
