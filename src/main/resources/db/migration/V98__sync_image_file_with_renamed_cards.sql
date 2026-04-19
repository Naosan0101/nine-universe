-- 名称変更後も image_file が旧ファイル名のままのカードを、現在のイラストファイル名に揃える
UPDATE card_definition
SET image_file = 'ピクシー.PNG'
WHERE id = 16 AND name = 'ピクシー';

UPDATE card_definition
SET image_file = 'ネクロマンサー.PNG'
WHERE id = 23 AND name = 'ネクロマンサー';
