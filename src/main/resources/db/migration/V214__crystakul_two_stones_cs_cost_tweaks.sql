-- クリスタクル: 任意ストーン 3→2（文言は「つ」に統一）
UPDATE card_definition
SET deploy_help = 'ストーンを2つ使用してもよい。使用したなら、次に自分がバトルゾーンに配置するファイターは、次の相手のターンの終わりまで、強さ+3。'
WHERE id = 35 AND name = 'クリスタクル';

-- 漫画家: コスト 0→1
UPDATE card_definition
SET cost = 1
WHERE id = 96 AND name = '漫画家';

-- 週刊少年 CAMP: コスト 0→1
UPDATE card_definition
SET cost = 1
WHERE id = 93 AND name = '週刊少年 CAMP';

-- 鳥獣戯画: コスト 1→0
UPDATE card_definition
SET cost = 0
WHERE id = 94 AND name = '鳥獣戯画';
