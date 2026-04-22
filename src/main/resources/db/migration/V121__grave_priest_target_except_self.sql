-- 墓守神父(id=67): 対象を「墓守神父」以外のアンデッドに明記
UPDATE card_definition
SET deploy_help = '手札の「墓守神父」以外の「種族：アンデッド」のファイターを1枚選び、バトル終了まで、コストを-2する。'
WHERE id = 67;
