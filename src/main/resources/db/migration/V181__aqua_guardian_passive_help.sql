-- 「アクアガーディアン」: passive_help を公式1行の〈常時〉本文と一致させる
UPDATE card_definition
SET passive_help = '自分のレストゾーンにある「種族：マーフォーク」のカード1枚につき、強さ+1。'
WHERE id = 75 AND name = 'アクアガーディアン';