-- カードリサイクル用アッシュ（ポイント）
ALTER TABLE app_user
  ADD COLUMN IF NOT EXISTS recycle_ash INTEGER NOT NULL DEFAULT 0;

UPDATE app_user SET recycle_ash = 0 WHERE recycle_ash IS NULL;
