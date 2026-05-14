-- アーサー: カムイ下で〈常時〉+3 に加え〈配置〉でレストの人間ファイターを手札へ（効果は無効化しない）
UPDATE card_definition
SET ability_deploy_code = 'ARTHUR',
    passive_help = '〈常時〉「決戦の地 カムイ」が配置されているなら、強さ+3。',
    deploy_help = '〈配置〉「決戦の地 カムイ」が配置されているなら、自分のレストゾーンから「種族：人間」のファイターを1枚選んで、手札に加える。'
WHERE id = 43 AND name = 'アーサー';
