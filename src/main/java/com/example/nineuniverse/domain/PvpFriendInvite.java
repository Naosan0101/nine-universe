package com.example.nineuniverse.domain;

import java.time.Instant;
import lombok.Data;

@Data
public class PvpFriendInvite {
	private Long id;
	private String matchId;
	private long challengerUserId;
	private long opponentUserId;
	private String status;
	private Instant createdAt;
}
