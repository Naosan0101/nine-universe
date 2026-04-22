-- フレンド申請・成立したフレンド関係
CREATE TABLE friend_request (
	id BIGSERIAL PRIMARY KEY,
	from_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
	to_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
	status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
	created_at TIMESTAMP NOT NULL DEFAULT NOW(),
	CONSTRAINT friend_request_no_self CHECK (from_user_id <> to_user_id)
);

CREATE UNIQUE INDEX friend_request_one_pending_pair
	ON friend_request(from_user_id, to_user_id)
	WHERE status = 'PENDING';

CREATE INDEX friend_request_pending_to_user
	ON friend_request(to_user_id)
	WHERE status = 'PENDING';

CREATE TABLE friendship (
	user_low_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
	user_high_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
	created_at TIMESTAMP NOT NULL DEFAULT NOW(),
	PRIMARY KEY (user_low_id, user_high_id),
	CONSTRAINT friendship_order CHECK (user_low_id < user_high_id)
);

-- フレンド同士の対戦招待（match_id はメモリ上の PvpMatch と対応）
CREATE TABLE pvp_friend_invite (
	id BIGSERIAL PRIMARY KEY,
	match_id VARCHAR(32) NOT NULL,
	challenger_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
	opponent_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
	status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
	created_at TIMESTAMP NOT NULL DEFAULT NOW(),
	CONSTRAINT pvp_friend_invite_no_self CHECK (challenger_user_id <> opponent_user_id)
);

CREATE UNIQUE INDEX pvp_friend_invite_one_pending_pair
	ON pvp_friend_invite(challenger_user_id, opponent_user_id)
	WHERE status = 'PENDING';

CREATE INDEX pvp_friend_invite_opponent_pending
	ON pvp_friend_invite(opponent_user_id)
	WHERE status = 'PENDING';

CREATE INDEX pvp_friend_invite_challenger_pending
	ON pvp_friend_invite(challenger_user_id)
	WHERE status = 'PENDING';
