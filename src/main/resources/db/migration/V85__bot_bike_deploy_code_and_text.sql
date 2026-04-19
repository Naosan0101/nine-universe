-- ボットバイク: 〈配置〉文言（次の相手ターン終了まで+2）、能力コード BOT_BIKE
UPDATE card_definition
SET deploy_help = 'コストとして使用したカードが「メカニック」なら、次の相手のターンの終わりまで、強さを+2。',
    ability_deploy_code = 'BOT_BIKE'
WHERE id = 57;
