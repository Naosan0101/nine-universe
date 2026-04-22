ALTER TABLE app_user ADD COLUMN selected_epithet_upper_id BIGINT REFERENCES nickname_epithet (id);
ALTER TABLE app_user ADD COLUMN selected_epithet_lower_id BIGINT REFERENCES nickname_epithet (id);

UPDATE app_user SET
	selected_epithet_upper_id = (SELECT id FROM nickname_epithet WHERE kind = 'UPPER' AND phrase = 'ビギナー中の'),
	selected_epithet_lower_id = (SELECT id FROM nickname_epithet WHERE kind = 'LOWER' AND phrase = 'ビギナー')
WHERE selected_epithet_upper_id IS NULL OR selected_epithet_lower_id IS NULL;

ALTER TABLE app_user ALTER COLUMN selected_epithet_upper_id SET NOT NULL;
ALTER TABLE app_user ALTER COLUMN selected_epithet_lower_id SET NOT NULL;

INSERT INTO user_nickname_epithet_owned (user_id, epithet_id)
SELECT u.id, e.id
FROM app_user u
CROSS JOIN LATERAL (
	SELECT id FROM nickname_epithet WHERE kind = 'UPPER' AND phrase = 'ビギナー中の'
) e
ON CONFLICT DO NOTHING;

INSERT INTO user_nickname_epithet_owned (user_id, epithet_id)
SELECT u.id, e.id
FROM app_user u
CROSS JOIN LATERAL (
	SELECT id FROM nickname_epithet WHERE kind = 'LOWER' AND phrase = 'ビギナー'
) e
ON CONFLICT DO NOTHING;
