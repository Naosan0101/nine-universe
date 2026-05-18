-- コミックダイナソー: コスト1・強さ2・配置効果（種族：コミックのみ・ストーン獲得なし）
UPDATE card_definition
SET cost = 1,
    base_power = 2,
    deploy_help = '〈配置〉自分の手札から「種族：コミック」のカードを1枚選んで、レストゾーンに置く。「ドラゴンの卵」を2枚手札に加える。'
WHERE id = 97 AND name = 'コミックダイナソー';
