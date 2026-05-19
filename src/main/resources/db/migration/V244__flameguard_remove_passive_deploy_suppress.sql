-- 紅蓮峡谷 フレイムガルド(84): 〈常時〉の配置封じテキストを削除（効果はドラゴン・ファイターのコスト-1のみ）

UPDATE card_definition
SET passive_help = NULL
WHERE id = 84 AND name = '紅蓮峡谷 フレイムガルド';
