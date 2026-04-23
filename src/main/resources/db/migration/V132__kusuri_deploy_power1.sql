-- 薬売り(8): 強さ1、効果を〈配置〉に変更（配置時の所持ストーン数で相手ファイターを弱体するスナップショット）

UPDATE card_definition SET
	base_power = 1,
	ability_deploy_code = 'KUSURI',
	ability_passive_code = NULL,
	deploy_help = '自分が所持しているストーン1つにつき、相手のファイターの強さ-1。',
	passive_help = NULL
WHERE id = 8;
