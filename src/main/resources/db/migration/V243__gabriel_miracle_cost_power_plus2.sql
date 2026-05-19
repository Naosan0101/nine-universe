-- ガブリエル(104): 〈常時〉コストに「奇跡」があるときの強さボーナスを +1 → +2

UPDATE card_definition
SET deploy_help = '〈常時〉コストとして使用したカードが「奇跡」なら、強さ+2。'
WHERE id = 104;
