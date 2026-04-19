-- 墓守神父(id=67): 〈配置〉コード
UPDATE card_definition
SET deploy_help = '手札の「種族：アンデッド」のファイターを1枚選び、バトル終了まで、コストを-2する。',
    ability_deploy_code = 'GRAVE_PRIEST'
WHERE id = 67;
