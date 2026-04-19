-- id=34（NOXSKUL）と id=37（STONIA）の名称のみ入れ替え。コスト・強さ・ability_deploy_code は据え置き。
-- name に UNIQUE 制約があるため、一時名を挟んで2段階で更新する。
UPDATE card_definition SET name = '__V74_name_swap_tmp__' WHERE id = 34;
UPDATE card_definition SET name = 'ノクスクル' WHERE id = 37;
UPDATE card_definition SET name = 'ストーニア' WHERE id = 34;
