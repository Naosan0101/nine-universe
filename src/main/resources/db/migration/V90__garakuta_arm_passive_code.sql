-- ガラクタアーム: 〈常時〉コード（相手ターン中 +1）
UPDATE card_definition
SET passive_help = '相手のターンの間、強さ+1。',
    ability_passive_code = 'GARAKUTA_ARM'
WHERE id = 60;
