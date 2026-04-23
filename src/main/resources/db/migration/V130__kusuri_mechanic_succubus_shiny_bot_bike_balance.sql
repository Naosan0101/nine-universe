-- 薬売り(8): 強さ0／メカニック(45): コスト0／サキュバス(24): 強さ4
-- シャイニ(31): 常時文言（レストの「シャイニ」以外のカーバンクル種類）
-- ボットバイク(57): メカニックをコストにした場合の強さ+3

UPDATE card_definition SET base_power = 0 WHERE id = 8;

UPDATE card_definition SET cost = 0 WHERE id = 45;

UPDATE card_definition SET base_power = 4 WHERE id = 24;

UPDATE card_definition
SET passive_help = '自分のレストゾーンにある「シャイニ」以外の「種族：カーバンクル」のカード1種類（重複×）につき、強さ+2。'
WHERE id = 31;

UPDATE card_definition
SET deploy_help = 'コストとして使用したカードが「メカニック」なら、次の相手のターンの終わりまで、強さを+3。'
WHERE id = 57;
