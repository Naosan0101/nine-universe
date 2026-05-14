-- 「クラーケン」: ターン開始時の「ソードフィッシュ」手札追加をエンジンで解決するため ability_deploy_code を付与
UPDATE card_definition
SET ability_deploy_code = 'KRAKEN'
WHERE id = 71 AND name = 'クラーケン';
