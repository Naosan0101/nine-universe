package com.example.nineuniverse.pvp;

import com.example.nineuniverse.battle.CpuBattleState;
import lombok.Data;

@Data
public class PvpMatch {
	public enum Format {
		CASUAL,
		LEAGUE
	}

	private final String id;
	private final long hostUserId;
	private final long invitedGuestUserId;
	private Format format = Format.CASUAL;

	/** 現在のゲームでホストが使う物理デッキ行ID（カジュアルでは固定、リーグではゲームごとに変わる） */
	private Long hostDeckId;
	private Long guestDeckId;

	// --- リーグのみ ---
	private Long hostLeagueSetId;
	private Long guestLeagueSetId;
	private Long hostDeckSlot1Id;
	private Long hostDeckSlot2Id;
	private Long guestDeckSlot1Id;
	private Long guestDeckSlot2Id;
	private Integer hostFirstGameSlotPick;
	private Integer guestFirstGameSlotPick;
	private int hostActiveSlot = 1;
	private int guestActiveSlot = 1;
	private int hostWins;
	private int guestWins;
	private boolean leagueMatchComplete;
	private boolean leagueAwaitingNextGameAck;
	private boolean leagueLastEndedRoundScored;

	private Long guestUserId;
	private CpuBattleState state;
	private boolean missionCompletionNotified;

	public PvpMatch(String id, long hostUserId, long invitedGuestUserId) {
		this.id = id;
		this.hostUserId = hostUserId;
		this.invitedGuestUserId = invitedGuestUserId;
	}

	public static PvpMatch casual(String id, long hostUserId, long hostDeckId, long invitedGuestUserId) {
		PvpMatch m = new PvpMatch(id, hostUserId, invitedGuestUserId);
		m.setFormat(Format.CASUAL);
		m.setHostDeckId(hostDeckId);
		return m;
	}

	public static PvpMatch league(String id, long hostUserId, long invitedGuestUserId,
			long hostLeagueSetId, long hostD1, long hostD2) {
		PvpMatch m = new PvpMatch(id, hostUserId, invitedGuestUserId);
		m.setFormat(Format.LEAGUE);
		m.setHostLeagueSetId(hostLeagueSetId);
		m.setHostDeckSlot1Id(hostD1);
		m.setHostDeckSlot2Id(hostD2);
		return m;
	}
}
