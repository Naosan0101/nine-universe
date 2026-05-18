-- ミカエル: 強さ3
UPDATE card_definition
SET base_power = 3
WHERE id = 106 AND name = 'ミカエル';

-- セラフィム: コスト0・レストの「種族：エンジェル」は1枚まで手札に
UPDATE card_definition
SET cost = 0,
    deploy_help = '〈配置〉自分の手札にある「奇跡」を1枚レストゾーンに置いてもよい。置いたなら、自分のレストゾーンにある「種族：エンジェル」のカードを1枚選んで、手札に加える。'
WHERE id = 103 AND name = 'セラフィム';
