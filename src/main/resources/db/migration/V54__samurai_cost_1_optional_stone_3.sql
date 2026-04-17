-- サムライ(2): コスト 0→1、任意ストーン 2個→3個（効果文・DB）

UPDATE card_definition
SET cost = 1,
    deploy_help = 'ストーンを3個使用してもよい。使用したなら、相手は手札からカードを2枚選んで、レストゾーンに置く。'
WHERE id = 2;
