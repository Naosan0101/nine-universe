package com.example.nineuniverse.battle;

import java.io.Serializable;
import java.util.ArrayList;
import lombok.Data;

/**
 * CPU リーグ戦（2本先取）のセッション。{@link CpuBattleState} は {@code CPU_BATTLE_STATE} に保持する。
 */
@Data
public class CpuLeagueBattleSession implements Serializable {
	public static final String SESSION_KEY = "CPU_LEAGUE_BATTLE_SESSION";

	private long cpuBattleUserId;
	private long humanLeagueSetId;
	private int cpuLevel;
	private CpuBattleMode cpuBattleMode = CpuBattleMode.ORIGIN;
	private int humanActiveSlot = 1;
	private int cpuActiveSlot = 1;
	private int humanWins;
	private int cpuWins;
	private boolean matchComplete;
	private boolean awaitingNextGameAck;
	private boolean leagueLastEndedRoundScored;

	/**
	 * CPU リーグ用ランダムデッキ（スロット1）。スロット2とカード ID が重複しない。
	 */
	private ArrayList<Short> cpuLeagueOpponentDeck1;
	/**
	 * CPU リーグ用ランダムデッキ（スロット2）。
	 */
	private ArrayList<Short> cpuLeagueOpponentDeck2;
}
