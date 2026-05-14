-- 「ポセイドン」〈配置〉: ターン終了まで強さ+3を文言に追加
UPDATE card_definition
SET deploy_help = '〈配置〉自分のターンの終わりまで、強さ+3。自分のレストゾーンにある、すべての「ソードフィッシュ」を手札に加える。'
WHERE id = 70 AND name = 'ポセイドン';
