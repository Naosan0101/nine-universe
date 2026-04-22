CREATE TABLE nickname_epithet (
	id BIGSERIAL PRIMARY KEY,
	kind VARCHAR(5) NOT NULL CHECK (kind IN ('UPPER', 'LOWER')),
	phrase VARCHAR(160) NOT NULL,
	CONSTRAINT uq_nickname_epithet_kind_phrase UNIQUE (kind, phrase)
);

CREATE TABLE user_nickname_epithet_owned (
	user_id BIGINT NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
	epithet_id BIGINT NOT NULL REFERENCES nickname_epithet (id) ON DELETE CASCADE,
	PRIMARY KEY (user_id, epithet_id)
);

CREATE INDEX idx_user_nickname_epithet_owned_user ON user_nickname_epithet_owned (user_id);
