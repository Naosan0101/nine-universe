-- 「ページウォーカー」〈配置〉: ストーン獲得を文言に追加
UPDATE card_definition
SET deploy_help = '〈配置〉ストーンを1つ得る。〈フィールド〉のカウントを2つ進める。'
WHERE id = 89 AND name = 'ページウォーカー';
