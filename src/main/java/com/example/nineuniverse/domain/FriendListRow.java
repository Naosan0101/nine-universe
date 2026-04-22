package com.example.nineuniverse.domain;

import lombok.Data;

@Data
public class FriendListRow {
	private long friendUserId;
	private String username;
	private String displayName;

	public String getDisplayLabel() {
		if (displayName != null && !displayName.isBlank()) {
			return displayName.trim();
		}
		return username != null ? username : "";
	}
}
