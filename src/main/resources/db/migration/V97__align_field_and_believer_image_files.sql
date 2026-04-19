-- フィールド・複合種族カードで image_file が未設定のままの環境向けに、表示用ファイル名を name.PNG に揃える
UPDATE card_definition
SET image_file = name || '.PNG'
WHERE id IN (41, 42, 50, 51, 65, 68)
  AND (
		image_file IS NULL
		OR TRIM(BOTH FROM image_file) = ''
		OR LOWER(TRIM(BOTH FROM image_file)) = '__missing__.png'
	);
