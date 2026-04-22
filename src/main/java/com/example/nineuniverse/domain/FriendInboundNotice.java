package com.example.nineuniverse.domain;

import lombok.Data;

@Data
public class FriendInboundNotice {
	private long requestId;
	private long fromUserId;
	private String fromDisplayName;
}
