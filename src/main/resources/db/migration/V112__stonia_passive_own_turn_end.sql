-- ノクスクル(id=37): 〈常時〉を「自分のターンの終わりまで」に明記
UPDATE card_definition
SET deploy_help = '自分が所持しているストーン1つにつき、自分のターンの終わりまで、強さ+1。'
WHERE id = 37;
