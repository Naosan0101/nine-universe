-- 「マーメイド」〈配置〉: ストーン獲得を文言に追加
UPDATE card_definition
SET deploy_help = '〈配置〉ストーンを1つ得る。手札のすべての「ソードフィッシュ」を、バトル終了まで強さ+2。'
WHERE id = 72 AND name = 'マーメイド';
