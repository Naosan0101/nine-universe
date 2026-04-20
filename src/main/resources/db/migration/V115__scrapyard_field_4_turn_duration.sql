-- 廃棄工場 5C-R4P: 4ターン持続の〈フィールド〉効果（文言のみ。ロジックは CpuBattleEngine）
UPDATE card_definition
SET deploy_help = '4ターンの間、バトルゾーンの、名前に「ガラクタ」とつく自分のファイターは、レストゾーンに置かれず手札にもどる。'
WHERE id = 63;
