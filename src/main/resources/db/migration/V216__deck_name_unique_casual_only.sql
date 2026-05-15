-- リーグ子デッキは同一ユーザーで未命名（空文字）を複数持てるようにする。
-- カジュアルデッキのみ (user_id, name) の一意を維持する。
ALTER TABLE deck DROP CONSTRAINT IF EXISTS deck_user_id_name_key;

CREATE UNIQUE INDEX uq_deck_user_casual_name ON deck (user_id, name)
  WHERE league_set_id IS NULL;
