-- 紅蓮峡谷 フレイムガルド: 〈配置〉封じの常時効果を削除。ドラゴン・ファイターコスト-1のみ（双方・CpuBattleEngine と同内容）
UPDATE card_definition
SET deploy_help = '〈フィールド〉「種族：ドラゴン」のファイターのコストを-1する。',
	passive_help = NULL
WHERE id = 84 AND name = '紅蓮峡谷 フレイムガルド';
