-- 「漫画家」配置効果の文言（現在の強さを明示）
UPDATE card_definition
SET deploy_help = '〈配置〉このファイターの、現在の強さと同じ値の強さの、ランダムなファイターを、1枚手札に加える。'
WHERE id = 96 AND name = '漫画家';
