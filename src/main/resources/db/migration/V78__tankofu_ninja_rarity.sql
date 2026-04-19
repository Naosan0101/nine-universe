-- 炭鉱夫(44): C → R、忍者(47): R → Ep
UPDATE card_definition SET rarity = 'R' WHERE id = 44;
UPDATE card_definition SET rarity = 'Ep' WHERE id = 47;
