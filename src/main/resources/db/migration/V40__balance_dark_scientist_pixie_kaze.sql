-- 科学者(6): 強さ3、効果はストーン不要に（deploy_help はアプリの正規テキストと併せる）
UPDATE card_definition
SET base_power = 3,
    deploy_help = '相手と強さを逆転（配置時・ストーン不要）'
WHERE id = 6;

-- 風の魔人(14): 強さ2
UPDATE card_definition
SET base_power = 2,
    deploy_help = 'ストーンを2つ得る'
WHERE id = 14;

-- ピクシー(16): レストからエルフのみ回収（deploy_help）
UPDATE card_definition
SET deploy_help = '自分のレストから「種族：エルフ」を1枚選び手札へ'
WHERE id = 16;

-- ダークドラゴン(28): コスト3、効果にストーン+2（正規テキストはアプリ側）
UPDATE card_definition
SET cost = 3,
    deploy_help = '相手がドラゴンならレストへ。ストーン+2'
WHERE id = 28;
