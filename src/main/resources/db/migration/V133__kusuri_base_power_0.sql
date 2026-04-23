-- 薬売り(8): 強さを0に戻す（〈配置〉効果は維持）

UPDATE card_definition SET base_power = 0 WHERE id = 8;
