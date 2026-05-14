-- ザドキエル〈配置〉: 奇跡任意＋次配置ファイターの相手ターン中強さ+3（旧: 相手ファイターをレストへ）

UPDATE card_definition
SET deploy_help = '〈配置〉自分の手札にある「奇跡」を1枚レストゾーンに置いてもよい。置いたなら、次に自分がバトルゾーンに配置するファイターは、相手のターンの間、強さ+3。',
    ability_deploy_code = 'ZADKIEL'
WHERE id = 98 AND name = 'ザドキエル';
