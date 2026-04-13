-- 剣闘士：「お互」→「お互い」表記修正
UPDATE card_definition SET deploy_help = 'お互いのプレイヤーは、手札からカードを1枚選んで、レストゾーンに置く。' WHERE id = 5;
