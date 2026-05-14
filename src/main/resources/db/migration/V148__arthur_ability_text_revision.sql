-- アーサー: 効果文言を〈配置〉1本に統一（+3 はバトル終了までと明記）。ロジック変更なし。
UPDATE card_definition
SET deploy_help = '〈配置〉「決戦の地 カムイ」が配置されているなら、バトル終了まで、強さ+3し、自分のレストゾーンから「種族：人間」のファイターを1枚選んで、手札に加える。',
    passive_help = NULL
WHERE id = 43 AND name = 'アーサー';
