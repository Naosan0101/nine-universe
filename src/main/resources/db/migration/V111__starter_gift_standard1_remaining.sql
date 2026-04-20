-- 新規ユーザー向け「スタンダードパック1」プレゼントの未開封数（既存ユーザーは 0）
ALTER TABLE app_user ADD COLUMN starter_gift_standard1_remaining INT NOT NULL DEFAULT 0;
