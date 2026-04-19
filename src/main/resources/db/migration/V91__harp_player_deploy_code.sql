-- 森のハープ弾き(id=65): 〈配置〉コード（次のエルフ配置 +3・ターン終了まで）
UPDATE card_definition
SET deploy_help = '次に自分がバトルゾーンに配置するファイターが「種族：エルフ」なら、ターンの終わりまで、強さ+3。',
    ability_deploy_code = 'HARP_PLAYER'
WHERE id = 65;
