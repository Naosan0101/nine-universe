-- 創成の神域パック（pack_initial CS）新カード（テキスト・定義のみ。バトル用 ability_* は未実装）

INSERT INTO card_definition (id, name, cost, base_power, attribute, card_kind, image_file, rarity, pack_initial, ability_deploy_code, ability_passive_code, deploy_help, passive_help)
VALUES
	(86, 'インクナイト', 0, 0, 'COMIC', 'FIGHTER', '__missing__.PNG', 'C', 'CS', NULL, NULL, NULL, '〈常時〉自分のレストゾーンの「インクナイト」1枚につき、強さ+1。'),
	(87, 'スケッチャー', 1, 2, 'COMIC', 'FIGHTER', '__missing__.PNG', 'C', 'CS', NULL, NULL, '〈配置〉手札のカードを1枚選び、そのカードのコピーを1枚手札に加える。', NULL),
	(88, 'コミックウィッチ', 1, 3, 'COMIC', 'FIGHTER', '__missing__.PNG', 'C', 'CS', NULL, NULL, '〈配置〉ストーンを1つ使用してもよい。使用したなら、自分のレストゾーンからカードを2枚まで選んで、そのカードを「インクナイト」に変化させて、手札に加える。', NULL),
	(89, 'ページウォーカー', 0, 2, 'COMIC', 'FIGHTER', '__missing__.PNG', 'Ep', 'CS', NULL, NULL, '〈配置〉〈フィールド〉のカウントを2つ進める。', NULL),
	(90, 'キングメーカー', 0, 3, 'COMIC', 'FIGHTER', '__missing__.PNG', 'R', 'CS', NULL, NULL, '〈配置〉自分の手札に「インクナイト」が3枚以上あるなら、「インクキング」を1枚手札に加える。', NULL),
	(91, 'コミックヒーロー', 2, 2, 'COMIC', 'FIGHTER', '__missing__.PNG', 'Ep', 'CS', NULL, NULL, NULL, '〈常時〉自分のレストゾーンにあるファイターの「種族」1種類につき、強さ+1。'),
	(92, '世界の再構築', 3, 0, 'COMIC', 'FIELD', '__missing__.PNG', 'Ep', 'CS', NULL, NULL, '〈フィールド〉このカードが配置されたとき、自分のレストゾーンに「種族：コミック」が3枚以上あるなら、このカード以外の、自分の手札とデッキと所持ストーンを、バトル開始の状態まで戻す。', NULL),
	(93, '週刊少年 CAMP', 0, 0, 'COMIC', 'FIELD', '__missing__.PNG', 'Reg', 'CS', NULL, NULL, '〈フィールド〉6ターンの間、『カウント6：「種族：コミック」の強さ+2。』、『カウント3：ターンの終わりまで、すべてのカードのコスト+1。』、『カウント2：「種族：コミック」の強さ+4。』', NULL),
	(94, '鳥獣戯画', 1, 0, 'COMIC', 'FIELD', '__missing__.PNG', 'R', 'CS', NULL, NULL, '〈フィールド〉次に配置するファイターを、バトル終了まで「種族：ドラゴン」にし、相手が次に配置するファイターを、バトル終了まで「種族：人間」にする。', NULL),
	(95, 'アメリカンダイナマイトシティ', 0, 0, 'COMIC', 'FIELD', '__missing__.PNG', 'R', 'CS', NULL, NULL, '〈フィールド〉6ターンの間、『カウント4：ストーンを1つ得る。』、『カウント2：すべての「種族：コミック」のファイターのコストを0にする。』', NULL),
	(96, '漫画家', 0, 2, 'HUMAN_COMIC', 'FIGHTER', '__missing__.PNG', 'R', 'CS', NULL, NULL, '〈配置〉このファイターの強さと同じ値の強さの、ランダムなファイターを、1枚手札に加える。', NULL),
	(97, 'コミックダイナソー', 0, 3, 'DRAGON_COMIC', 'FIGHTER', '__missing__.PNG', 'C', 'CS', NULL, NULL, '〈配置〉自分の手札からカードを1枚選んで、レストゾーンに置く。ストーンを1つ得る。「ドラゴンの卵」を2枚手札に加える。', NULL),
	(98, 'ザドキエル', 1, 4, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'C', 'CS', NULL, NULL, '〈配置〉自分の手札にある「奇跡」を1枚レストゾーンに置いてもよい。置いたなら、相手のファイターをレストゾーンに置く。', NULL),
	(99, 'ラミエル', 0, 2, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'C', 'CS', NULL, NULL, '〈配置〉次の自分のターンの開始時、「奇跡」を1枚手札に加える。', NULL),
	(100, 'ヴァーチャー', 2, 5, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'C', 'CS', NULL, NULL, '〈配置〉ストーンを2つ得る。「奇跡」を1枚手札に加える。', NULL),
	(101, 'セレスティア', 0, 1, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'R', 'CS', NULL, NULL, '〈配置〉ストーンを1つ使用してもよい。使用したなら、「奇跡」を2枚手札に加える。', NULL),
	(102, 'ドミニオン', 0, 2, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'Ep', 'CS', NULL, NULL, '〈配置〉手札のすべてのカードを「ミニオンソルジャー」に変化させる。', NULL),
	(103, 'セラフィム', 1, 3, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'R', 'CS', NULL, NULL, '〈配置〉自分の手札にある「奇跡」を1枚レストゾーンに置いてもよい。置いたなら、自分のレストゾーンにある「種族：エンジェル」のカードを2枚まで選んで、手札に加える。', NULL),
	(104, 'ガブリエル', 1, 4, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'C', 'CS', NULL, NULL, NULL, '〈常時〉コストとして使用したカードが「奇跡」なら、強さ+1。'),
	(105, 'エンジェルメイジ', 0, 2, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'R', 'CS', NULL, NULL, NULL, '〈常時〉自分のレストゾーンに「エンジェルメイジ」があるなら、強さ+2。'),
	(106, 'ミカエル', 0, 1, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'Reg', 'CS', NULL, NULL, '〈配置〉自分のレストゾーンに「奇跡」が5枚以上あるなら、自分のデッキを「ミカエルデッキ」に変化させる。', NULL),
	(107, '天界門 ヘヴンズゲート', 0, 0, 'ANGEL', 'FIELD', '__missing__.PNG', 'Ep', 'CS', NULL, NULL, '〈フィールド〉このカードが配置されたとき、「奇跡」を1枚手札に加える。お互いのプレイヤーは、ターン開始時に「奇跡」を1枚手札に加える。', NULL),
	(108, 'ルシファー', 1, 4, 'ANGEL_UNDEAD', 'FIGHTER', '__missing__.PNG', 'Ep', 'CS', NULL, NULL, '〈配置〉バトル終了まで、すべての「奇跡」は「種族：アンデッド」となる。', NULL);
