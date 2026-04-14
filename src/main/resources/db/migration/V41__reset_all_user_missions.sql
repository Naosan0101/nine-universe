-- 全ユーザーのデイリー／ウィークリーミッション進捗をリセットし、次回アクセス時に当日・当週分が再生成されるようにする
DELETE FROM user_daily_mission;
DELETE FROM user_weekly_mission;
UPDATE app_user SET last_mission_date = NULL;
