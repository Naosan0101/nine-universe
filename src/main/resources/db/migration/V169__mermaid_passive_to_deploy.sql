-- 「マーメイド」: 〈常時〉を〈配置〉に移す（文言は同一）
UPDATE card_definition
SET deploy_help = '〈配置〉手札のすべての「ソードフィッシュ」を、バトル終了まで強さ+2。',
	passive_help = NULL
WHERE id = 72 AND name = 'マーメイド';
