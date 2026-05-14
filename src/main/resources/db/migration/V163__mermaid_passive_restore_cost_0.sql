-- 「マーメイド」: 〈配置〉変更を取り消して〈常時〉に戻し、コストを0にする
UPDATE card_definition
SET cost = 0,
	deploy_help = NULL,
	passive_help = '手札のすべての「ソードフィッシュ」を、バトル終了まで強さ+2。'
WHERE id = 72 AND name = 'マーメイド';
