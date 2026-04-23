-- 「だれかと対戦」申し込みの通知（画面バナー・ブラウザ通知）をユーザーが設定でOFFにできる
ALTER TABLE app_user
	ADD COLUMN pvp_invite_notify_enabled BOOLEAN NOT NULL DEFAULT TRUE;
