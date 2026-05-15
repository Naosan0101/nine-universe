package com.example.nineuniverse.domain;

import lombok.Data;

@Data
public class LeagueDeckSetSummary {
	private long setId;
	private String setName;
	private long deckSlot1Id;
	private long deckSlot2Id;
	private String deckSlot1Name;
	private String deckSlot2Name;
}
