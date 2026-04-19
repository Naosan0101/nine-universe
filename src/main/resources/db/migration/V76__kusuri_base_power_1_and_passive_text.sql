-- 薬売り: 強さ1、常時テキスト「ストーン1個につき」に統一
UPDATE card_definition
SET base_power = 1,
    passive_help = '自分が所持しているストーン1個につき、相手のファイターの強さ-1'
WHERE id = 8;
