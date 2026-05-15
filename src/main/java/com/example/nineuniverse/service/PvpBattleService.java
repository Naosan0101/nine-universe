package com.example.nineuniverse.service;

import com.example.nineuniverse.battle.BattlePhase;
import com.example.nineuniverse.battle.CpuBattleEngine;
import com.example.nineuniverse.battle.CpuBattleState;
import com.example.nineuniverse.battle.PendingChoice;
import com.example.nineuniverse.pvp.PvpMatch;
import com.example.nineuniverse.web.dto.CpuBattleStateDto;
import com.example.nineuniverse.web.dto.PendingChoiceDto;
import com.example.nineuniverse.web.dto.CpuBattleCommitRequest;
import com.example.nineuniverse.web.dto.CpuBattleChoiceRequest;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PvpBattleService {

	private final CpuBattleEngine engine;
	private final CpuBattleService cpuBattleService;
	private final DeckService deckService;
	private final CardCatalogService cardCatalogService;
	private final MissionService missionService;

	private final Map<String, PvpMatch> matches = new ConcurrentHashMap<>();

	public PvpMatch createWaitingRoom(long hostUserId, long hostDeckId, long invitedGuestUserId) {
		deckService.requireDeck(hostUserId, hostDeckId);
		if (invitedGuestUserId == hostUserId) {
			throw new IllegalArgumentException("自分自身とは対戦できません");
		}
		String id = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		var m = PvpMatch.casual(id, hostUserId, hostDeckId, invitedGuestUserId);
		matches.put(id, m);
		return m;
	}

	public PvpMatch createWaitingRoomLeague(long hostUserId, long hostLeagueSetId, long invitedGuestUserId) {
		if (invitedGuestUserId == hostUserId) {
			throw new IllegalArgumentException("自分自身とは対戦できません");
		}
		var sum = deckService.requireLeagueSummary(hostUserId, hostLeagueSetId);
		String id = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		var m = PvpMatch.league(id, hostUserId, invitedGuestUserId, hostLeagueSetId,
				sum.getDeckSlot1Id(), sum.getDeckSlot2Id());
		matches.put(id, m);
		return m;
	}

	public PvpMatch get(String id) {
		return matches.get(id);
	}

	public void join(String matchId, long guestUserId, long guestDeckId) {
		PvpMatch m = require(matchId);
		synchronized (m) {
			if (m.getFormat() != PvpMatch.Format.CASUAL) {
				throw new IllegalStateException("この部屋はカジュアル対戦用ではありません");
			}
			if (m.getGuestUserId() != null) {
				throw new IllegalStateException("この対戦部屋はすでに埋まっています");
			}
			if (m.getHostUserId() == guestUserId) {
				throw new IllegalStateException("自分の部屋には参加できません");
			}
			if (guestUserId != m.getInvitedGuestUserId()) {
				throw new IllegalStateException("この対戦は招待された相手のみが参加できます");
			}
			deckService.requireDeck(guestUserId, guestDeckId);
			m.setGuestUserId(guestUserId);
			m.setGuestDeckId(guestDeckId);
			startPvpBattleFromCurrentDeckIds(m);
		}
	}

	public void joinLeagueGuest(String matchId, long guestUserId, long guestLeagueSetId) {
		PvpMatch m = require(matchId);
		synchronized (m) {
			if (m.getFormat() != PvpMatch.Format.LEAGUE) {
				throw new IllegalStateException("この部屋はリーグ対戦用ではありません");
			}
			if (m.getGuestUserId() != null) {
				throw new IllegalStateException("この対戦部屋はすでに埋まっています");
			}
			if (m.getHostUserId() == guestUserId) {
				throw new IllegalStateException("自分の部屋には参加できません");
			}
			if (guestUserId != m.getInvitedGuestUserId()) {
				throw new IllegalStateException("この対戦は招待された相手のみが参加できます");
			}
			var gsum = deckService.requireLeagueSummary(guestUserId, guestLeagueSetId);
			m.setGuestUserId(guestUserId);
			m.setGuestLeagueSetId(guestLeagueSetId);
			m.setGuestDeckSlot1Id(gsum.getDeckSlot1Id());
			m.setGuestDeckSlot2Id(gsum.getDeckSlot2Id());
		}
	}

	public void submitLeagueFirstGameSlot(String matchId, long userId, int slot1Or2) {
		if (slot1Or2 != 1 && slot1Or2 != 2) {
			throw new IllegalArgumentException("デッキは1か2を選んでください");
		}
		PvpMatch m = require(matchId);
		synchronized (m) {
			requireParticipant(m, userId);
			if (m.getFormat() != PvpMatch.Format.LEAGUE) {
				throw new IllegalStateException("リーグ対戦ではありません");
			}
			if (m.getGuestUserId() == null) {
				throw new IllegalStateException("相手の参加を待っています");
			}
			if (m.getState() != null) {
				return;
			}
			if (m.getHostUserId() == userId) {
				m.setHostFirstGameSlotPick(slot1Or2);
			} else if (m.getGuestUserId() == userId) {
				m.setGuestFirstGameSlotPick(slot1Or2);
			} else {
				throw new IllegalStateException("この対戦に参加していません");
			}
			tryStartLeagueFirstGame(m);
		}
	}

	private void tryStartLeagueFirstGame(PvpMatch m) {
		if (m.getHostFirstGameSlotPick() == null || m.getGuestFirstGameSlotPick() == null) {
			return;
		}
		deckService.requireLeagueSetBattleReady(m.getHostUserId(), m.getHostLeagueSetId());
		deckService.requireLeagueSetBattleReady(m.getGuestUserId(), m.getGuestLeagueSetId());
		int hs = m.getHostFirstGameSlotPick();
		int gs = m.getGuestFirstGameSlotPick();
		m.setHostActiveSlot(hs);
		m.setGuestActiveSlot(gs);
		long hd = hs == 1 ? m.getHostDeckSlot1Id() : m.getHostDeckSlot2Id();
		long gd = gs == 1 ? m.getGuestDeckSlot1Id() : m.getGuestDeckSlot2Id();
		m.setHostDeckId(hd);
		m.setGuestDeckId(gd);
		startPvpBattleFromCurrentDeckIds(m);
	}

	private void startPvpBattleFromCurrentDeckIds(PvpMatch m) {
		Long hd = m.getHostDeckId();
		Long gd = m.getGuestDeckId();
		if (hd == null || gd == null) {
			throw new IllegalStateException("デッキが未設定です");
		}
		var defs = cardCatalogService.mapById();
		var rnd = new Random();
		List<Short> hostCards = deckService.cardIdsForDeck(hd);
		List<Short> guestCards = deckService.cardIdsForDeck(gd);
		CpuBattleState st = engine.newPvpBattle(hostCards, guestCards, rnd, defs);
		st.setHumanSlotDeckId(hd);
		st.setCpuSlotDeckId(gd);
		st.setPhase(st.isHumansTurn() ? BattlePhase.HUMAN_INPUT : BattlePhase.CPU_THINKING);
		if (st.getTurnStartedAtMs() <= 0) {
			st.setTurnStartedAtMs(System.currentTimeMillis());
		}
		m.setState(st);
	}

	public boolean isGuestJoined(String matchId) {
		PvpMatch m = matches.get(matchId);
		return m != null && m.getGuestUserId() != null;
	}

	public boolean isStarted(String matchId) {
		PvpMatch m = matches.get(matchId);
		return m != null && m.getState() != null;
	}

	public CpuBattleStateDto leagueNextGame(String matchId, long userId) {
		PvpMatch m = require(matchId);
		synchronized (m) {
			requireParticipant(m, userId);
			if (m.getFormat() != PvpMatch.Format.LEAGUE || !m.isLeagueAwaitingNextGameAck()) {
				throw new IllegalStateException("次のゲームを開始できません");
			}
			CpuBattleState st = m.getState();
			if (st == null) {
				return null;
			}
			boolean host = m.getHostUserId() == userId;
			if (m.isLeagueMatchComplete()) {
				m.setLeagueAwaitingNextGameAck(false);
				return wrapPvpState(m, st, host);
			}
			boolean hostWonLast = st.isHumanWon();
			int nh = hostWonLast ? (3 - m.getHostActiveSlot()) : m.getHostActiveSlot();
			int ng = hostWonLast ? m.getGuestActiveSlot() : (3 - m.getGuestActiveSlot());
			m.setHostActiveSlot(nh);
			m.setGuestActiveSlot(ng);
			long hd = nh == 1 ? m.getHostDeckSlot1Id() : m.getHostDeckSlot2Id();
			long gd = ng == 1 ? m.getGuestDeckSlot1Id() : m.getGuestDeckSlot2Id();
			m.setHostDeckId(hd);
			m.setGuestDeckId(gd);
			m.setLeagueAwaitingNextGameAck(false);
			m.setLeagueLastEndedRoundScored(false);
			deckService.requireLeagueSetBattleReady(m.getHostUserId(), m.getHostLeagueSetId());
			deckService.requireLeagueSetBattleReady(m.getGuestUserId(), m.getGuestLeagueSetId());
			startPvpBattleFromCurrentDeckIds(m);
			return wrapPvpState(m, m.getState(), host);
		}
	}

	public CpuBattleStateDto stateForUser(String matchId, long userId) {
		PvpMatch m = require(matchId);
		synchronized (m) {
			requireParticipant(m, userId);
			CpuBattleState st = m.getState();
			if (st == null) {
				return null;
			}
			enforceTimeoutIfNeeded(st);
			boolean host = m.getHostUserId() == userId;
			return wrapPvpState(m, st, host);
		}
	}

	public CpuBattleStateDto commit(String matchId, long userId, CpuBattleCommitRequest req) {
		PvpMatch m = require(matchId);
		synchronized (m) {
			requireParticipant(m, userId);
			CpuBattleState st = m.getState();
			if (st == null) {
				return null;
			}
			if (st.isGameOver()) {
				boolean host = m.getHostUserId() == userId;
				return wrapPvpState(m, st, host);
			}
			enforceTimeoutIfNeeded(st);
			boolean host = m.getHostUserId() == userId;
			if (host) {
				if (!st.isHumansTurn() || st.getPhase() != BattlePhase.HUMAN_INPUT) {
					return wrapPvpState(m, st, true);
				}
				engine.humanTurnInteractive(st,
						req.levelUpRest(),
						req.levelUpDiscardInstanceIds(),
						req.levelUpStones(),
						req.deployInstanceId(),
						req.payCostStones(),
						req.payCostCardInstanceIds(),
						cardCatalogService.mapById());
			} else {
				if (st.isHumansTurn() || st.getPhase() != BattlePhase.CPU_THINKING) {
					return wrapPvpState(m, st, false);
				}
				engine.opponentTurnInteractive(st,
						req.levelUpRest(),
						req.levelUpDiscardInstanceIds(),
						req.levelUpStones(),
						req.deployInstanceId(),
						req.payCostStones(),
						req.payCostCardInstanceIds(),
						cardCatalogService.mapById());
			}
			return wrapPvpState(m, st, host);
		}
	}

	public CpuBattleStateDto resolve(String matchId, long userId) {
		PvpMatch m = require(matchId);
		synchronized (m) {
			requireParticipant(m, userId);
			CpuBattleState st = m.getState();
			if (st == null) {
				return null;
			}
			enforceTimeoutIfNeeded(st);
			boolean host = m.getHostUserId() == userId;
			engine.resolvePendingEffectAndAdvance(st, cardCatalogService.mapById(), new Random());
			return wrapPvpState(m, st, host);
		}
	}

	public CpuBattleStateDto choice(String matchId, long userId, CpuBattleChoiceRequest req) {
		PvpMatch m = require(matchId);
		synchronized (m) {
			requireParticipant(m, userId);
			CpuBattleState st = m.getState();
			if (st == null) {
				return null;
			}
			enforceTimeoutIfNeeded(st);
			boolean host = m.getHostUserId() == userId;
			PendingChoice pc = st.getPendingChoice();
			if (pc != null) {
				if (pc.isCpuSlotChooses()) {
					if (!host) {
						engine.applyCpuSlotChoiceAndAdvance(st,
								req != null && req.confirm(),
								req != null && req.pickedInstanceIds() != null ? req.pickedInstanceIds() : List.of(),
								cardCatalogService.mapById(),
								new Random());
					}
				} else if (pc.isForHuman()) {
					if (host) {
						engine.applyHumanChoiceAndAdvance(st,
								req != null && req.confirm(),
								req != null && req.pickedInstanceIds() != null ? req.pickedInstanceIds() : List.of(),
								cardCatalogService.mapById(),
								new Random());
					}
				}
			}
			return wrapPvpState(m, st, host);
		}
	}

	public CpuBattleStateDto timeoutTick(String matchId, long userId) {
		PvpMatch m = require(matchId);
		synchronized (m) {
			requireParticipant(m, userId);
			CpuBattleState st = m.getState();
			if (st == null) return null;
			enforceTimeoutIfNeeded(st);
			boolean host = m.getHostUserId() == userId;
			return wrapPvpState(m, st, host);
		}
	}

	private CpuBattleStateDto wrapPvpState(PvpMatch m, CpuBattleState st, boolean host) {
		maybeScoreLeagueRound(m, st);
		CpuBattleStateDto base = cpuBattleService.stateDtoFromState(st);
		notifyPvpMissionIfNeeded(m, st);
		CpuBattleStateDto adapted = adaptForViewer(base, st, host);
		return attachLeagueSeriesOverlay(adapted, m, host);
	}

	private static void maybeScoreLeagueRound(PvpMatch m, CpuBattleState st) {
		if (m.getFormat() != PvpMatch.Format.LEAGUE || st == null || !st.isGameOver()) {
			return;
		}
		if (m.isLeagueMatchComplete()) {
			return;
		}
		if (m.isLeagueLastEndedRoundScored()) {
			return;
		}
		m.setLeagueLastEndedRoundScored(true);
		boolean hostWon = st.isHumanWon();
		if (hostWon) {
			m.setHostWins(m.getHostWins() + 1);
		} else {
			m.setGuestWins(m.getGuestWins() + 1);
		}
		if (m.getHostWins() >= 2 || m.getGuestWins() >= 2) {
			m.setLeagueMatchComplete(true);
		}
		m.setLeagueAwaitingNextGameAck(true);
	}

	private void notifyPvpMissionIfNeeded(PvpMatch m, CpuBattleState st) {
		if (st == null || !st.isPvp() || !st.isGameOver()) {
			return;
		}
		if (m.getFormat() == PvpMatch.Format.LEAGUE && !m.isLeagueMatchComplete()) {
			return;
		}
		if (m.isMissionCompletionNotified()) {
			return;
		}
		m.setMissionCompletionNotified(true);
		missionService.onPvpBattlePlayed(m.getHostUserId());
		if (m.getGuestUserId() != null) {
			missionService.onPvpBattlePlayed(m.getGuestUserId());
		}
	}

	private static CpuBattleStateDto attachLeagueSeriesOverlay(CpuBattleStateDto d, PvpMatch m, boolean host) {
		if (m.getFormat() != PvpMatch.Format.LEAGUE) {
			return d;
		}
		Integer myW = host ? m.getHostWins() : m.getGuestWins();
		Integer opW = host ? m.getGuestWins() : m.getHostWins();
		boolean done = m.isLeagueMatchComplete();
		boolean await = m.isLeagueAwaitingNextGameAck();
		return new CpuBattleStateDto(
				d.pvpMatch(), d.cpuBattleMode(), d.cpuLevel(), d.humanGoesFirst(), d.humansTurn(), d.phase(),
				d.turnStartedAtMs(), d.activeTimeLimitSec(), d.activePenaltyStage(),
				d.humanStones(), d.cpuStones(), d.humanDeck(), d.humanHand(), d.humanRest(), d.humanBattle(),
				d.cpuDeck(), d.cpuHand(), d.cpuRest(), d.cpuBattle(), d.activeField(), d.scrapyardFieldTurnsRemaining(),
				d.deathbounceFieldTurnsRemaining(), d.atlantisFieldCounterDisplay(),
				d.weeklyShonenCampFieldCounterDisplay(), d.weeklyShonenCampCount2ComicBonus(),
				d.weeklyShonenCampGlobalDeployCostPlusOneThisTurn(),
				d.worldRebuildFieldCounterDisplay(),
				d.paperCityFieldCounterDisplay(),
				d.humanBattlePower(), d.cpuBattlePower(),
				d.humanNextDeployBonus(), d.humanNextElfOnlyBonus(), d.humanNextDeployCostBonusTimes(),
				d.humanNextMechanicStacks(),
				d.cpuNextDeployBonus(), d.cpuNextElfOnlyBonus(), d.cpuNextDeployCostBonusTimes(), d.cpuNextMechanicStacks(),
				d.lastMessage(), d.gameOver(), d.humanWon(), d.noLegalDeploy(),
				d.pendingEffect(), d.pendingChoice(), d.eventLog(), d.defs(), d.myBattleDeckId(),
				d.spec666NextHumanUndead(), d.spec666NextCpuUndead(),
				myW, opW, done, await, await);
	}

	private void enforceTimeoutIfNeeded(CpuBattleState st) {
		if (st == null || st.isGameOver()) return;
		// 制限時間・時間切れ強制処理は無効化
		if (st.getTurnStartedAtMs() <= 0) {
			st.setTurnStartedAtMs(System.currentTimeMillis());
		}
	}

	public void surrender(String matchId, long userId) {
		PvpMatch m = require(matchId);
		synchronized (m) {
			requireParticipant(m, userId);
			CpuBattleState st = m.getState();
			if (st == null || st.isGameOver()) {
				return;
			}
			boolean host = m.getHostUserId() == userId;
			st.setGameOver(true);
			st.setHumanWon(!host);
			st.setPhase(BattlePhase.GAME_OVER);
			st.setLastMessage(host ? "ホストが降参しました" : "ゲストが降参しました");
			st.addLog(host ? "ホストが降参" : "ゲストが降参");
			// リーグ戦: このゲームのみ相手の勝ちとして扱う（スコアは wrap/maybeScoreLeagueRound に任せる）
		}
	}

	public void removeMatch(String matchId) {
		matches.remove(matchId);
	}

	public void requireParticipant(PvpMatch m, long userId) {
		if (m.getHostUserId() != userId
				&& (m.getGuestUserId() == null || m.getGuestUserId() != userId)) {
			throw new IllegalStateException("この対戦に参加していません");
		}
	}

	private PvpMatch require(String id) {
		PvpMatch m = matches.get(id);
		if (m == null) {
			throw new IllegalArgumentException("対戦が見つかりません");
		}
		return m;
	}

	private CpuBattleStateDto adaptForViewer(CpuBattleStateDto base, CpuBattleState raw, boolean host) {
		if (!raw.isPvp()) {
			return base;
		}
		CpuBattleStateDto adapted = host
				? withPhaseForHost(base, raw)
				: withPhaseForGuest(swapPerspective(base, raw), raw);
		Long myDeck = host ? raw.getHumanSlotDeckId() : raw.getCpuSlotDeckId();
		return withMyBattleDeckId(adapted, myDeck);
	}

	private static CpuBattleStateDto withMyBattleDeckId(CpuBattleStateDto b, Long myBattleDeckId) {
		return new CpuBattleStateDto(
				b.pvpMatch(), b.cpuBattleMode(), b.cpuLevel(), b.humanGoesFirst(), b.humansTurn(), b.phase(),
				b.turnStartedAtMs(), b.activeTimeLimitSec(), b.activePenaltyStage(),
				b.humanStones(), b.cpuStones(), b.humanDeck(), b.humanHand(), b.humanRest(), b.humanBattle(),
				b.cpuDeck(), b.cpuHand(), b.cpuRest(), b.cpuBattle(), b.activeField(), b.scrapyardFieldTurnsRemaining(),
				b.deathbounceFieldTurnsRemaining(), b.atlantisFieldCounterDisplay(),
				b.weeklyShonenCampFieldCounterDisplay(), b.weeklyShonenCampCount2ComicBonus(),
				b.weeklyShonenCampGlobalDeployCostPlusOneThisTurn(),
				b.worldRebuildFieldCounterDisplay(),
				b.paperCityFieldCounterDisplay(),
				b.humanBattlePower(), b.cpuBattlePower(),
				b.humanNextDeployBonus(), b.humanNextElfOnlyBonus(), b.humanNextDeployCostBonusTimes(),
				b.humanNextMechanicStacks(),
				b.cpuNextDeployBonus(), b.cpuNextElfOnlyBonus(), b.cpuNextDeployCostBonusTimes(), b.cpuNextMechanicStacks(),
				b.lastMessage(), b.gameOver(), b.humanWon(), b.noLegalDeploy(),
				b.pendingEffect(), b.pendingChoice(), b.eventLog(), b.defs(),
				myBattleDeckId,
				b.spec666NextHumanUndead(),
				b.spec666NextCpuUndead(),
				null, null, null, null, null);
	}

	private CpuBattleStateDto withPhaseForHost(CpuBattleStateDto base, CpuBattleState raw) {
		if (raw.isGameOver()) {
			return base;
		}
		BattlePhase p = raw.getPhase();
		if (p == BattlePhase.CPU_THINKING) {
			return replacePhase(base, "OPPONENT_TURN");
		}
		if (p == BattlePhase.HUMAN_CHOICE) {
			PendingChoice pc = raw.getPendingChoice();
			if (pc != null && pc.isCpuSlotChooses()) {
				return replacePhase(base, "OPPONENT_TURN");
			}
		}
		return base;
	}

	private CpuBattleStateDto withPhaseForGuest(CpuBattleStateDto swapped, CpuBattleState raw) {
		if (raw.isGameOver()) {
			return swapped;
		}
		BattlePhase p = raw.getPhase();
		if (p == BattlePhase.HUMAN_INPUT || p == BattlePhase.HUMAN_EFFECT_PENDING) {
			return replacePhase(swapped, "OPPONENT_TURN");
		}
		if (p == BattlePhase.CPU_THINKING) {
			return replacePhase(swapped, "HUMAN_INPUT");
		}
		if (p == BattlePhase.CPU_EFFECT_PENDING) {
			return replacePhase(swapped, "HUMAN_EFFECT_PENDING");
		}
		if (p == BattlePhase.HUMAN_CHOICE) {
			PendingChoice pc = raw.getPendingChoice();
			if (pc != null && pc.isCpuSlotChooses()) {
				return replacePendingViewer(replacePhase(swapped, "HUMAN_CHOICE"), pc, false);
			}
			return replacePhase(swapped, "OPPONENT_TURN");
		}
		return swapped;
	}

	private CpuBattleStateDto swapPerspective(CpuBattleStateDto b, CpuBattleState raw) {
		PendingChoiceDto pc = b.pendingChoice();
		PendingChoiceDto npc = null;
		if (pc != null) {
			boolean guestActs = pc.cpuSlotChooses();
			npc = new PendingChoiceDto(
					pc.kind(),
					pc.prompt(),
					pc.forHuman(),
					pc.cpuSlotChooses(),
					pc.abilityDeployCode(),
					pc.stoneCost(),
					pc.optionInstanceIds(),
					guestActs
			);
		}
		com.example.nineuniverse.web.dto.PendingEffectDto pe = b.pendingEffect();
		com.example.nineuniverse.web.dto.PendingEffectDto npe = null;
		if (pe != null) {
			npe = new com.example.nineuniverse.web.dto.PendingEffectDto(
					!pe.ownerHuman(),
					pe.mainInstanceId(),
					pe.cardId(),
					pe.abilityDeployCode()
			);
		}
		return new CpuBattleStateDto(
				b.pvpMatch(),
				b.cpuBattleMode(),
				b.cpuLevel(),
				b.humanGoesFirst(),
				!b.humansTurn(),
				b.phase(),
				b.turnStartedAtMs(),
				b.activeTimeLimitSec(),
				b.activePenaltyStage(),
				b.cpuStones(),
				b.humanStones(),
				b.cpuDeck(),
				b.cpuHand(),
				b.cpuRest(),
				b.cpuBattle(),
				b.humanDeck(),
				b.humanHand(),
				b.humanRest(),
				b.humanBattle(),
				b.activeField(),
				b.scrapyardFieldTurnsRemaining(),
				b.deathbounceFieldTurnsRemaining(),
				b.atlantisFieldCounterDisplay(),
				b.weeklyShonenCampFieldCounterDisplay(), b.weeklyShonenCampCount2ComicBonus(),
				b.weeklyShonenCampGlobalDeployCostPlusOneThisTurn(),
				b.worldRebuildFieldCounterDisplay(),
				b.paperCityFieldCounterDisplay(),
				b.cpuBattlePower(),
				b.humanBattlePower(),
				b.cpuNextDeployBonus(),
				b.cpuNextElfOnlyBonus(),
				b.cpuNextDeployCostBonusTimes(),
				b.cpuNextMechanicStacks(),
				b.humanNextDeployBonus(),
				b.humanNextElfOnlyBonus(),
				b.humanNextDeployCostBonusTimes(),
				b.humanNextMechanicStacks(),
				b.lastMessage(),
				b.gameOver(),
				!b.humanWon(),
				b.noLegalDeploy(),
				npe,
				npc,
				b.eventLog(),
				b.defs(),
				b.myBattleDeckId(),
				raw.isSpec666NextCpuUndead(),
				raw.isSpec666NextHumanUndead(),
				null, null, null, null, null
		);
	}

	private CpuBattleStateDto replacePhase(CpuBattleStateDto b, String phase) {
		return new CpuBattleStateDto(
				b.pvpMatch(), b.cpuBattleMode(), b.cpuLevel(), b.humanGoesFirst(), b.humansTurn(), phase,
				b.turnStartedAtMs(), b.activeTimeLimitSec(), b.activePenaltyStage(),
				b.humanStones(), b.cpuStones(), b.humanDeck(), b.humanHand(), b.humanRest(), b.humanBattle(),
				b.cpuDeck(), b.cpuHand(), b.cpuRest(), b.cpuBattle(), b.activeField(), b.scrapyardFieldTurnsRemaining(),
				b.deathbounceFieldTurnsRemaining(), b.atlantisFieldCounterDisplay(),
				b.weeklyShonenCampFieldCounterDisplay(), b.weeklyShonenCampCount2ComicBonus(),
				b.weeklyShonenCampGlobalDeployCostPlusOneThisTurn(),
				b.worldRebuildFieldCounterDisplay(),
				b.paperCityFieldCounterDisplay(),
				b.humanBattlePower(), b.cpuBattlePower(),
				b.humanNextDeployBonus(), b.humanNextElfOnlyBonus(), b.humanNextDeployCostBonusTimes(),
				b.humanNextMechanicStacks(),
				b.cpuNextDeployBonus(), b.cpuNextElfOnlyBonus(), b.cpuNextDeployCostBonusTimes(), b.cpuNextMechanicStacks(),
				b.lastMessage(), b.gameOver(), b.humanWon(), b.noLegalDeploy(),
				b.pendingEffect(), b.pendingChoice(), b.eventLog(), b.defs(), b.myBattleDeckId(),
				b.spec666NextHumanUndead(), b.spec666NextCpuUndead(),
				null, null, null, null, null);
	}

	private CpuBattleStateDto replacePendingViewer(CpuBattleStateDto b, PendingChoice rawPc, boolean host) {
		PendingChoiceDto pc = b.pendingChoice();
		if (pc == null) {
			return b;
		}
		boolean may = host
				? (rawPc.isForHuman() && !rawPc.isCpuSlotChooses())
				: rawPc.isCpuSlotChooses();
		PendingChoiceDto npc = new PendingChoiceDto(
				pc.kind(), pc.prompt(), pc.forHuman(), pc.cpuSlotChooses(),
				pc.abilityDeployCode(), pc.stoneCost(), pc.optionInstanceIds(), may);
		return new CpuBattleStateDto(
				b.pvpMatch(), b.cpuBattleMode(), b.cpuLevel(), b.humanGoesFirst(), b.humansTurn(), b.phase(),
				b.turnStartedAtMs(), b.activeTimeLimitSec(), b.activePenaltyStage(),
				b.humanStones(), b.cpuStones(), b.humanDeck(), b.humanHand(), b.humanRest(), b.humanBattle(),
				b.cpuDeck(), b.cpuHand(), b.cpuRest(), b.cpuBattle(), b.activeField(), b.scrapyardFieldTurnsRemaining(),
				b.deathbounceFieldTurnsRemaining(), b.atlantisFieldCounterDisplay(),
				b.weeklyShonenCampFieldCounterDisplay(), b.weeklyShonenCampCount2ComicBonus(),
				b.weeklyShonenCampGlobalDeployCostPlusOneThisTurn(),
				b.worldRebuildFieldCounterDisplay(),
				b.paperCityFieldCounterDisplay(),
				b.humanBattlePower(), b.cpuBattlePower(),
				b.humanNextDeployBonus(), b.humanNextElfOnlyBonus(), b.humanNextDeployCostBonusTimes(),
				b.humanNextMechanicStacks(),
				b.cpuNextDeployBonus(), b.cpuNextElfOnlyBonus(), b.cpuNextDeployCostBonusTimes(), b.cpuNextMechanicStacks(),
				b.lastMessage(), b.gameOver(), b.humanWon(), b.noLegalDeploy(),
				b.pendingEffect(), npc, b.eventLog(), b.defs(), b.myBattleDeckId(),
				b.spec666NextHumanUndead(), b.spec666NextCpuUndead(),
				null, null, null, null, null);
	}
}
