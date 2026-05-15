CREATE TABLE league_deck_set (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  name VARCHAR(120) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_league_deck_set_user ON league_deck_set(user_id);

ALTER TABLE deck
  ADD COLUMN league_set_id BIGINT NULL REFERENCES league_deck_set(id) ON DELETE CASCADE,
  ADD COLUMN league_slot SMALLINT NULL;

ALTER TABLE deck ADD CONSTRAINT deck_league_slot_consistency CHECK (
  (league_set_id IS NULL AND league_slot IS NULL)
  OR (league_set_id IS NOT NULL AND league_slot IN (1, 2))
);

CREATE UNIQUE INDEX uq_deck_league_set_slot ON deck (league_set_id, league_slot)
  WHERE league_set_id IS NOT NULL;

CREATE INDEX idx_deck_user_casual ON deck (user_id) WHERE league_set_id IS NULL;
