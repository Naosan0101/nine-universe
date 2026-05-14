-- 「深海神殿 アトランティス」: レア Ep、フィールド効果文言（2ターン・カウント2/0）
UPDATE card_definition
SET rarity = 'Ep',
	deploy_help = '〈フィールド〉2ターンの間、『カウント2：「ソードフィッシュ」を1枚手札に加える。』、『カウント0：自分のレストゾーンに「種族：マーフォーク」があるなら、ストーンを2つ得る。』'
WHERE id = 76 AND name = '深海神殿 アトランティス';

-- 「ファフニール」: レア C
UPDATE card_definition
SET rarity = 'C'
WHERE id = 83 AND name = 'ファフニール';
