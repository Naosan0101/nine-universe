-- 追加カードのパラメータ修正（既適用環境でも安全に更新できるよう UPDATE で対応）

-- ハーフエルフ: C → R
UPDATE card_definition
SET rarity = 'R'
WHERE id = 51;

-- 磁力合体デンジリオン: Ep/3 → Reg/2
UPDATE card_definition
SET rarity = 'Reg',
    cost = 2
WHERE id = 59;

