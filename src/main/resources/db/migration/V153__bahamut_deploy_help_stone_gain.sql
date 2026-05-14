-- 「バハムート」〈配置〉: ストーン獲得を文言に追加
UPDATE card_definition
SET deploy_help = 'ストーンを2つ得る。相手のファイターをレストゾーンに置く。配置されている〈フィールド〉を、レストゾーンに置く。'
WHERE id = 82 AND name = 'バハムート';
