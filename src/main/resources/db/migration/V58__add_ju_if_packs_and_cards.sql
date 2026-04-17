-- JU/IF パック（表示用イニシャル）と新カードを追加

ALTER TABLE card_definition
	ADD COLUMN IF NOT EXISTS card_kind VARCHAR(12) NOT NULL DEFAULT 'FIGHTER';

-- 既存レコードを明示的に埋める（DB によっては既存行へ DEFAULT が遡及されない場合があるため）
UPDATE card_definition
SET card_kind = COALESCE(NULLIF(card_kind, ''), 'FIGHTER');

-- 宝石の秘境（JU）
INSERT INTO card_definition (id, name, cost, base_power, attribute, card_kind, image_file, rarity, pack_initial, ability_deploy_code, ability_passive_code, deploy_help, passive_help)
VALUES
	(31, 'シャイニ', 2, 1, 'CARBUNCLE', 'FIGHTER', '__missing__.PNG', 'Reg', 'JU', NULL, NULL, NULL, '自分のレストゾーンにある「種族：カーバンクル」のカード1種類（重複×）につき、強さ+2。'),
	(32, 'フロストクル', 1, 2, 'CARBUNCLE', 'FIGHTER', '__missing__.PNG', 'C', 'JU', NULL, NULL, NULL, '自分のレストゾーンに「種族：カーバンクル」があるなら、相手のターンの間、強さ+3。'),
	(33, 'ミスティンクル', 0, 2, 'CARBUNCLE', 'FIGHTER', '__missing__.PNG', 'R', 'JU', NULL, NULL, NULL, '相手のファイターは〈配置〉効果が使えない。'),
	(34, 'ノクスクル', 1, 3, 'CARBUNCLE', 'FIGHTER', '__missing__.PNG', 'C', 'JU', NULL, NULL, 'ストーンを1つ得る。', NULL),
	(35, 'クリスタクル', 0, 0, 'CARBUNCLE', 'FIGHTER', '__missing__.PNG', 'Ep', 'JU', NULL, NULL, '自分のレストゾーンに「種族：カーバンクル」があるなら、ストーンを2つ使用してもよい。使用したなら、ストーンを5つ得る。', NULL),
	(36, 'ミラージュクル', 1, 3, 'CARBUNCLE', 'FIGHTER', '__missing__.PNG', 'R', 'JU', NULL, NULL, '相手のバトルゾーンに〈配置〉効果を持つファイターがいるなら、そのファイターと同じ効果を使用できる。', NULL),
	(37, 'ストーニア', 0, 1, 'CARBUNCLE', 'FIGHTER', '__missing__.PNG', 'C', 'JU', NULL, NULL, '自分が所持しているストーン1つにつき、強さ+1。', NULL),
	(38, 'フェザリア', 0, 3, 'CARBUNCLE', 'FIGHTER', '__missing__.PNG', 'C', 'JU', NULL, NULL, 'ストーンを2つ使用してもよい。使用したなら、自分のレストゾーンから「種族：カーバンクル」のカードを2枚まで選んで、手札に加える。', NULL),
	(39, 'ルミナンク', 2, 2, 'CARBUNCLE', 'FIGHTER', '__missing__.PNG', 'Ep', 'JU', NULL, NULL, '自分が所持しているストーンの数を2倍にする。', NULL),
	(40, 'ネムリィ', 4, 1, 'CARBUNCLE', 'FIGHTER', '__missing__.PNG', 'Ep', 'JU', NULL, NULL, NULL, '自分のレストゾーンの「種族：カーバンクル」の枚数だけコストを-する。相手のターンの間、強さ+5。'),
	(41, '宝石の地 グロリア輝石台地', 1, 0, 'CARBUNCLE', 'FIELD', '__missing__.PNG', 'R', 'JU', NULL, NULL, '〈フィールド〉「種族：カーバンクル」のファイターの強さを常に+2する。', NULL),
	(42, '探鉱の洞窟 ネビュラ坑道', 0, 0, 'CARBUNCLE', 'FIELD', '__missing__.PNG', 'R', 'JU', NULL, NULL, '〈フィールド〉「種族：カーバンクル」のファイターを配置するたびに、ストーンを1つ得る。', NULL),
	(43, 'アーサー王', 1, 4, 'HUMAN', 'FIGHTER', '__missing__.PNG', 'Reg', 'JU', NULL, NULL, NULL, '「決戦の地 カムイ」が配置されているなら、強さ+3'),
	(44, '炭鉱夫', 0, 1, 'HUMAN', 'FIGHTER', '__missing__.PNG', 'C', 'JU', NULL, NULL, '自分のレストゾーンから「種族：人間」のファイターを1枚選んで、手札に加える。ただし、そのファイターの効果は「効果なし。」となる。', NULL),
	(45, 'メカニック', 1, 1, 'HUMAN', 'FIGHTER', '__missing__.PNG', 'C', 'JU', NULL, NULL, '次に自分がバトルゾーンに配置するファイターは、ターンの終わりまで、コスト+1、強さ+3。', NULL),
	(46, '助手', 0, 2, 'HUMAN', 'FIGHTER', '__missing__.PNG', 'C', 'JU', NULL, NULL, '自分のレストゾーンから、名前に「研究者」とつくカードを1枚選んで、手札に加える。', NULL),
	(47, '忍者', 1, 1, 'HUMAN', 'FIGHTER', '__missing__.PNG', 'R', 'JU', NULL, NULL, 'コストとして使用したカードと、このカードを入れ替える。そのカードの効果も使用できる。ただし、強さ-2。', NULL),
	(48, '研究者アストリア', 0, 1, 'HUMAN', 'FIGHTER', '__missing__.PNG', 'R', 'JU', NULL, NULL, '自分のレストゾーンから〈フィールド〉効果を持つカードを1枚選んで、コストを-1して手札に加える。', NULL),
	(49, '決戦の地 カムイ', 1, 0, 'HUMAN', 'FIELD', '__missing__.PNG', 'Ep', 'JU', NULL, NULL, '〈フィールド〉すべての「もとの強さが3」のファイターは〈配置〉効果が使えなくなる。', NULL),
	(50, '信奉者', 0, 2, 'HUMAN_UNDEAD', 'FIGHTER', '__missing__.PNG', 'C', 'JU', NULL, NULL, '自分のレストゾーンのすべての「霊園教会 デスバウンス」を手札に加える。', NULL),
	(51, 'ハーフエルフ', 1, 3, 'HUMAN_ELF', 'FIGHTER', '__missing__.PNG', 'C', 'JU', NULL, NULL, '自分のレストゾーンに「種族：人間」があるなら、強さ+1。自分のレストゾーンに「種族：エルフ」があるなら、強さ+1。', NULL);

-- 鉄面の艦隊（IF）
INSERT INTO card_definition (id, name, cost, base_power, attribute, card_kind, image_file, rarity, pack_initial, ability_deploy_code, ability_passive_code, deploy_help, passive_help)
VALUES
	(52, 'マシンガンナー', 3, 6, 'MACHINE', 'FIGHTER', '__missing__.PNG', 'Ep', 'IF', NULL, NULL, '自分のレストゾーンにある「コストが1」のファイター1枚につき、相手はストーンを１つ捨てる。', NULL),
	(53, 'SPEC-777', 2, 0, 'MACHINE', 'FIGHTER', '__missing__.PNG', 'R', 'IF', NULL, NULL, 'バトル終了まで、このファイターの強さは2～７の間のランダムな値になる。相手よりも強さが低くなった場合、バトルに敗北する。', NULL),
	(54, 'SPEC-666', 0, 3, 'MACHINE', 'FIGHTER', '__missing__.PNG', 'R', 'IF', NULL, NULL, '次に相手と自分が配置するファイター両方を、バトル終了まで「種族：アンデッド」にする。', NULL),
	(55, 'SPEC-123', 1, 1, 'MACHINE', 'FIGHTER', '__missing__.PNG', 'Ep', 'IF', NULL, NULL, '1～3の間のランダムな値分、ストーンを得る。', NULL),
	(56, 'SPEC-0', 1, 2, 'MACHINE', 'FIGHTER', '__missing__.PNG', 'R', 'IF', NULL, NULL, '自分のレストゾーンの「もとの強さが1」のファイターを1枚選んで、デッキの一番上に置く。', NULL),
	(57, 'ボットバイク', 1, 3, 'MACHINE', 'FIGHTER', '__missing__.PNG', 'R', 'IF', NULL, NULL, 'コストとして使用したカードが「メカニック」なら、強さを+2。', NULL),
	(58, 'レッドアイ', 2, 5, 'MACHINE', 'FIGHTER', '__missing__.PNG', 'C', 'IF', NULL, NULL, NULL, '相手が「種族：人間」なら、強さ+1。'),
	(59, '磁力合体デンジリオン', 3, 5, 'MACHINE', 'FIGHTER', '__missing__.PNG', 'Ep', 'IF', NULL, NULL, NULL, 'このファイターは、自分のレストゾーンの「種族：マシン」のファイターの〈常時〉効果をすべて持つ。'),
	(60, 'ガラクタアーム', 0, 2, 'MACHINE', 'FIGHTER', '__missing__.PNG', 'C', 'IF', NULL, NULL, NULL, '相手のターンの間、強さ+1。'),
	(61, 'ガラクタレッグ', 0, 2, 'MACHINE', 'FIGHTER', '__missing__.PNG', 'C', 'IF', NULL, NULL, NULL, '相手のファイターは〈常時〉効果が使えない。'),
	(62, '艦隊 HO-IVI-I3', 3, 0, 'MACHINE', 'FIELD', '__missing__.PNG', 'Ep', 'IF', NULL, NULL, '〈フィールド〉このカードが配置されたとき、自分のレストゾーンの「種族：マシン」のファイターをすべてデッキに加え、シャッフルする。', NULL),
	(63, '廃棄工場 5C-R4P', 0, 0, 'MACHINE', 'FIELD', '__missing__.PNG', 'C', 'IF', NULL, NULL, '〈フィールド〉バトルゾーンの、名前に「ガラクタ」とつく自分のファイターは、レストゾーンに置かれず手札にもどる。', NULL),
	(64, '武器庫 VV-E4-PON', 1, 0, 'MACHINE', 'FIELD', '__missing__.PNG', 'R', 'IF', NULL, NULL, '〈フィールド〉「種族：マシン」のすべてのファイターはコストが1になる。', NULL),
	(65, '森のハープ弾き', 2, 4, 'ELF', 'FIGHTER', '__missing__.PNG', 'C', 'IF', NULL, NULL, '次に自分がバトルゾーンに配置するファイターが「種族：エルフ」なら、ターンの終わりまで、強さ+3。', NULL),
	(66, '神秘の大樹 スカイア', 1, 0, 'ELF', 'FIELD', '__missing__.PNG', 'Reg', 'IF', NULL, NULL, '〈フィールド〉「種族：エルフ」が、カード効果によって+される強さは、相手のターンでも持続するようになる。', NULL),
	(67, '墓守神父', 2, 4, 'UNDEAD', 'FIGHTER', '__missing__.PNG', 'C', 'IF', NULL, NULL, '手札の「種族：アンデッド」のファイターを1枚選び、バトル終了まで、コストを-2する。', NULL),
	(68, '霊園教会 デスバウンス', 1, 0, 'UNDEAD', 'FIELD', '__missing__.PNG', 'Ep', 'IF', NULL, NULL, '〈フィールド〉バトルゾーンの「種族：アンデッド」のファイターは、レストゾーンに置かれず、バトルの終わりまでコストを+1して手札にもどる。', NULL);

