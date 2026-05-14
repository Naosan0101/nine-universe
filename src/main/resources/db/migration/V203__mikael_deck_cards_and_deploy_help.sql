-- 「ミカエル」: 文言と ability_deploy_code
UPDATE card_definition
SET deploy_help = '〈配置〉自分のレストゾーンに「奇跡」が5枚以上あるなら、自分のデッキを「ミカエルデッキ（ミカエルのカード6枚からなるデッキ）」に変化させる。',
    ability_deploy_code = 'MIKAEL'
WHERE id = 106 AND name = 'ミカエル';

-- ミカエルデッキ6枚（パック非収録・ライブラリ除外はアプリ側）
INSERT INTO card_definition (id, name, cost, base_power, attribute, card_kind, image_file, rarity, pack_initial, ability_deploy_code, ability_passive_code, deploy_help, passive_help)
VALUES
	(116, 'ミカエルの怒り', 1, 2, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'Ep', '-', 'MIKAEL_WRATH', NULL,
	 '〈配置〉相手のファイターは〈配置〉効果と〈常時〉効果が使えない。', NULL),
	(117, 'ミカエルパンチ', 1, 3, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'Ep', '-', 'MIKAEL_PUNCH', NULL,
	 '〈配置〉相手のターンの間、強さ+3。', NULL),
	(118, 'ミカエルの戦略', 1, 3, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'Ep', '-', 'MIKAEL_STRATEGY', NULL,
	 '〈配置〉ストーンを2つ得る。', NULL),
	(119, 'ミカエルの使いA', 0, 3, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'Ep', '-', 'MIKAEL_MINION_A', NULL,
	 '〈配置〉自分のレストゾーンに「ミカエルの使いB」があるなら、強さ+2。', NULL),
	(120, 'ミカエルの使いB', 0, 3, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'Ep', '-', 'MIKAEL_MINION_B', NULL,
	 '〈配置〉自分のレストゾーンに「ミカエルの使いA」があるなら、相手のターンの間、強さ+1。', NULL),
	(121, 'ミカエルの一閃', 2, 7, 'ANGEL', 'FIGHTER', '__missing__.PNG', 'Ep', '-', 'MIKAEL_FLASH', NULL,
	 '〈配置〉配置されている〈フィールド〉を、レストゾーンに置く。', NULL);
