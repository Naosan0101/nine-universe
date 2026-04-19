-- ハーフエルフ(id=51): 〈配置〉コード（レストの人間・エルフに応じて強さ+1/+2）
UPDATE card_definition
SET deploy_help = '自分のレストゾーンに「種族：人間」があるなら、強さ+1。自分のレストゾーンに「種族：エルフ」があるなら、強さ+1。',
    ability_deploy_code = 'HALF_ELF'
WHERE id = 51;
