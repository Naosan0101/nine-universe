ALTER TABLE user_daily_mission
	ADD COLUMN reward_gems INT NOT NULL DEFAULT 3;

ALTER TABLE user_weekly_mission
	ADD COLUMN reward_gems INT NOT NULL DEFAULT 10;
