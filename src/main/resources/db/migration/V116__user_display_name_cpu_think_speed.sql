-- 表示用ユーザー名（ログインIDの username とは別。バトル表示など）
ALTER TABLE app_user ADD COLUMN display_name VARCHAR(64);
UPDATE app_user SET display_name = username WHERE display_name IS NULL OR trim(display_name) = '';
ALTER TABLE app_user ALTER COLUMN display_name SET NOT NULL;

-- CPU戦：思考待ちの長さ（FAST / NORMAL / SLOW）
ALTER TABLE app_user ADD COLUMN cpu_think_speed VARCHAR(16) NOT NULL DEFAULT 'NORMAL';
