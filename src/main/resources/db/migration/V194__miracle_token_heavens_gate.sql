-- 「奇跡」トークン（天界門等で生成）。パック・ライブラリ一覧除外はアプリ側。
-- 「天界門 ヘヴンズゲート」〈フィールド〉文言: 配置したプレイヤーのみ即時に奇跡。

INSERT INTO card_definition (id, name, cost, base_power, attribute, card_kind, image_file, rarity, pack_initial, ability_deploy_code, ability_passive_code, deploy_help, passive_help)
VALUES (
	112,
	'奇跡',
	0,
	0,
	'ANGEL',
	'FIGHTER',
	'__missing__.PNG',
	'C',
	'NONE',
	'MIRACLE',
	NULL,
	'〈配置〉ストーンを1つ得る。',
	NULL
);

UPDATE card_definition
SET deploy_help = '〈フィールド〉このカードを配置したプレイヤーは、「奇跡」を1枚手札に加える。お互いのプレイヤーは、ターン開始時に「奇跡」を1枚手札に加える。'
WHERE id = 107 AND name = '天界門 ヘヴンズゲート';
