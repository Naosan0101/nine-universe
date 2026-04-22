package com.example.nineuniverse.domain;

import java.time.Instant;
import lombok.Data;

@Data
public class FriendRequest {
	private Long id;
	private long fromUserId;
	private long toUserId;
	private String status;
	private Instant createdAt;
}
