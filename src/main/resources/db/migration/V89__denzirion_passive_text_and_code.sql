-- 磁力合体デンジリオン: 文言（「レストゾーンにある」）、〈常時〉コード
UPDATE card_definition
SET passive_help = 'このファイターは、自分のレストゾーンにある「種族：マシン」のファイターの〈常時〉効果をすべて持つ。',
    ability_passive_code = 'DENZIRION'
WHERE id = 59;
