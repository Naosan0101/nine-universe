-- お詫び: 全ユーザーのボーナスパック時間ゲージを50%分進める。
-- TIME_PACK_CYCLE_DURATION_MS は 12 時間。経過時間を +6 時間相当にするため cycle_start を 6 時間前にずらす。
UPDATE app_user
SET time_pack_cycle_start = time_pack_cycle_start - INTERVAL '6 hours';
