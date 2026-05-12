-- 複合種族と共有イラストの対応（カード ID に依存せず attribute で統一）
-- ・DRAGON_MERFOLK（ドラゴン＋マーフォーク）→ ドラゴンマーフォーク.PNG
-- ・HUMAN_MERFOLK（人間＋マーフォーク）→ 人間マーフォーク.PNG
UPDATE card_definition SET image_file = 'ドラゴンマーフォーク.PNG' WHERE UPPER(TRIM(attribute)) = 'DRAGON_MERFOLK';
UPDATE card_definition SET image_file = '人間マーフォーク.PNG' WHERE UPPER(TRIM(attribute)) = 'HUMAN_MERFOLK';
