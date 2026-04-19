-- クリスタクル: レストのカーバンクル3枚以上でストーン+3（任意支払い廃止）

UPDATE card_definition
SET deploy_help = '自分のレストゾーンに「種族：カーバンクル」が3枚以上あるなら、ストーンを3つ得る。'
WHERE id = 35;
