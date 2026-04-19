-- 信奉者(id=50): 〈配置〉コード（レストの霊園教会 デスバウンスをすべて手札へ）
UPDATE card_definition
SET deploy_help = '自分のレストゾーンのすべての「霊園教会 デスバウンス」を手札に加える。',
    ability_deploy_code = 'BELIEVER'
WHERE id = 50;
