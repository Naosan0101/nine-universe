package com.example.nineuniverse.domain;

import lombok.Data;

@Data
public class Deck {
	private Long id;
	private Long userId;
	private String name;
	/** リーグデッキセットに属する場合のみ（カジュアルデッキは null） */
	private Long leagueSetId;
	/** セット内の並び: 1 または 2 */
	private Integer leagueSlot;
}
