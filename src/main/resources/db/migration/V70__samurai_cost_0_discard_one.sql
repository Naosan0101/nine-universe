-- サムライ(2): コスト 1→0、効果「相手は手札から2枚→1枚」

UPDATE card_definition
SET cost = 0,
    deploy_help = 'ストーンを3個使用してもよい。使用したなら、相手は手札からカードを1枚選んで、レストゾーンに置く。'
WHERE id = 2;
