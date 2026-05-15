-- 紅蓮峡谷 フレイムガルド: 場にいる間の常時効果テキスト（バトルロジックは CpuBattleEngine）
UPDATE card_definition
SET passive_help = '相手のファイターは〈配置〉効果が使えない。'
WHERE id = 84 AND name = '紅蓮峡谷 フレイムガルド';
