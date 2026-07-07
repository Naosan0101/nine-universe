-- ユーザー名末尾 _PlayTester のアカウントに、カタログ全カードを各2枚付与する。
INSERT INTO user_collection (user_id, card_id, quantity)
SELECT u.id, c.id, 2
FROM app_user u
CROSS JOIN card_definition c
WHERE TRIM(u.username) LIKE '%\_PlayTester' ESCAPE '\'
ON CONFLICT (user_id, card_id) DO UPDATE SET quantity = 2;
