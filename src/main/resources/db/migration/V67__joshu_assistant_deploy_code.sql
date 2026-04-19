-- 助手(46): 〈配置〉効果をエンジンに紐付け（名前に「研究者」を含むカードをレストから手札へ）
UPDATE card_definition SET ability_deploy_code = 'JOSHU' WHERE id = 46;
