-- 既存カードにパックイニシャルを割り当てる（表示用）
-- 風吹く丘パック: HUMAN / ELF（ただし UNDEAD/DRAGON 系は邪悪なる脅威を優先）
-- 邪悪なる脅威パック: UNDEAD / DRAGON（風の魔人 ID=14 は除外）

-- 邪悪なる脅威（ET）
UPDATE card_definition
SET pack_initial = 'ET'
WHERE id <> 14
  AND (
    UPPER(attribute) LIKE '%UNDEAD%'
    OR UPPER(attribute) LIKE '%DRAGON%'
  );

-- 風吹く丘（WH）
UPDATE card_definition
SET pack_initial = 'WH'
WHERE (
    UPPER(attribute) LIKE '%HUMAN%'
    OR UPPER(attribute) LIKE '%ELF%'
  )
  AND NOT (
    UPPER(attribute) LIKE '%UNDEAD%'
    OR UPPER(attribute) LIKE '%DRAGON%'
  );

