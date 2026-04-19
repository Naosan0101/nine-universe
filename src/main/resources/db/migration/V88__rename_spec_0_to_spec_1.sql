-- SPEC-0 → SPEC-1（id=56）：名称・〈配置〉コードを更新
UPDATE card_definition
SET name = 'SPEC-1',
    ability_deploy_code = 'SPEC1',
    deploy_help = '自分のレストゾーンの「もとの強さが1」のファイターを1枚選んで、デッキの一番上に置く。'
WHERE id = 56;
