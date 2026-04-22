-- MAX 時に2パック分ある場合、1パックずつ開封するため残りを保持する
ALTER TABLE app_user ADD COLUMN time_pack_bonus_bank SMALLINT NOT NULL DEFAULT 0;
