-- 海底の潮流パック（pack_initial OT）新カード（テキスト・定義のみ。バトル用 ability_* は未実装）

INSERT INTO card_definition (id, name, cost, base_power, attribute, card_kind, image_file, rarity, pack_initial, ability_deploy_code, ability_passive_code, deploy_help, passive_help)
VALUES
	(69, 'セイレーン', 0, 3, 'MERFOLK', 'FIGHTER', '__missing__.PNG', 'Reg', 'OT', NULL, NULL, '相手のバトルゾーンのファイターを自分の手札に加え、そのファイターを「ソードフィッシュ」に変化させる。バトル終了まで、そのファイターの強さを+2。', NULL),
	(70, 'ポセイドン', 2, 5, 'MERFOLK', 'FIGHTER', '__missing__.PNG', 'R', 'OT', NULL, NULL, '自分のレストゾーンにある、すべての「ソードフィッシュ」を手札に加える。', NULL),
	(71, 'クラーケン', 0, 3, 'MERFOLK', 'FIGHTER', '__missing__.PNG', 'C', 'OT', NULL, NULL, '自分のレストゾーンに「ソードフィッシュ」があるなら、次の自分のターンの開始時に、「ソードフィッシュ」を1枚手札に加える。', NULL),
	(72, 'マーメイド', 1, 4, 'MERFOLK', 'FIGHTER', '__missing__.PNG', 'Ep', 'OT', NULL, NULL, NULL, '手札のすべての「ソードフィッシュ」を、バトル終了まで強さ+2。'),
	(73, 'メガロドン', 1, 4, 'MERFOLK', 'FIGHTER', '__missing__.PNG', 'C', 'OT', NULL, NULL, NULL, NULL),
	(74, 'シーサーペント', 0, 2, 'MERFOLK', 'FIGHTER', '__missing__.PNG', 'R', 'OT', NULL, NULL, 'ストーンを1つ使用してもよい。使用したなら、「ソードフィッシュ」を2枚手札に加える。', NULL),
	(75, 'アクアガーディアン', 2, 3, 'MERFOLK', 'FIGHTER', '__missing__.PNG', 'Ep', 'OT', NULL, NULL, NULL, '自分のレストゾーンにある「種族：マーフォーク」のカード1枚につき、強さ+1。'),
	(76, '深海神殿 アトランティス', 1, 0, 'MERFOLK', 'FIELD', '__missing__.PNG', 'R', 'OT', NULL, NULL, '〈フィールド〉このカードを配置したプレイヤーは、「ソードフィッシュ」を2枚手札に加える。', NULL),
	(77, 'リヴァイアサン', 3, 6, 'DRAGON_MERFOLK', 'FIGHTER', '__missing__.PNG', 'R', 'OT', NULL, NULL, '自分のレストゾーンにある「種族：ドラゴン」もしくは「種族：マーフォーク」のカードを2枚まで選んで、手札に加える。', NULL),
	(78, '龍鱗海峡 ラグナロク', 1, 0, 'DRAGON_MERFOLK', 'FIELD', '__missing__.PNG', 'Ep', 'OT', NULL, NULL, '〈フィールド〉すべての、効果「効果なし。」のファイターの強さを常に+3する。', NULL),
	(79, '化石', 0, 3, 'HUMAN_MERFOLK', 'FIGHTER', '__missing__.PNG', 'C', 'OT', NULL, NULL, 'このファイターがバトルゾーンからレストゾーンに置かれたとき、このカードを「化石（フィールド）」に変化させて、〈フィールド〉に置く。', NULL),
	(80, 'ワイバーン', 0, 2, 'DRAGON', 'FIGHTER', '__missing__.PNG', 'C', 'OT', NULL, NULL, NULL, '相手のファイターは〈配置〉効果が使えない。'),
	(81, 'ベヒモス', 1, 2, 'DRAGON', 'FIGHTER', '__missing__.PNG', 'C', 'OT', NULL, NULL, 'コストとして使用したカードが「ドラゴンの卵」なら、ストーンを3つ得る。', NULL),
	(82, 'バハムート', 4, 8, 'DRAGON', 'FIGHTER', '__missing__.PNG', 'Ep', 'OT', NULL, NULL, '相手のファイターをレストゾーンに置く。配置されている〈フィールド〉を、レストゾーンに置く。', NULL),
	(83, 'ファフニール', 3, 0, 'DRAGON', 'FIGHTER', '__missing__.PNG', 'R', 'OT', NULL, NULL, '相手のファイターをレストゾーンに置き、そのファイターの強さの値と同じだけ、次の相手のターンの終わりまで、このファイターの強さを+する。', NULL),
	(84, '紅蓮峡谷 フレイムガルド', 1, 0, 'DRAGON', 'FIELD', '__missing__.PNG', 'Reg', 'OT', NULL, NULL, '〈フィールド〉「種族：ドラゴン」のファイターのコストを-1する。', NULL),
	(85, '研究者フローラ', 0, 2, 'ELF', 'FIGHTER', '__missing__.PNG', 'C', 'OT', NULL, NULL, 'ストーンを1つ使用してもよい。使用したなら、自分のレストゾーンから「種族：エルフ」のカードを1枚選び、手札に加える。', NULL);
