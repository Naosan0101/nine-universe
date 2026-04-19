-- ネムリィ: 相手ターン中の強さボーナスを +5 → +4

UPDATE card_definition
SET passive_help = '自分のレストゾーンの「種族：カーバンクル」の枚数だけコストを-する。相手のターンの間、強さ+4。'
WHERE id = 40;
