package com.example.nineuniverse.service;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.battle.BattleCard;
import com.example.nineuniverse.battle.CpuBattleEngine;
import com.example.nineuniverse.battle.BattlePhase;
import com.example.nineuniverse.battle.CpuBattleMode;
import com.example.nineuniverse.battle.CpuBattleState;
import com.example.nineuniverse.battle.CpuLeagueBattleSession;
import com.example.nineuniverse.battle.ZoneFighter;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.web.dto.BattleCardDto;
import com.example.nineuniverse.card.CardAttributeLabels;
import com.example.nineuniverse.card.CardFaceAbilityFormatter;
import com.example.nineuniverse.web.dto.AbilityBlockDto;
import com.example.nineuniverse.web.dto.CardDefDto;
import com.example.nineuniverse.web.dto.CpuBattleChoiceRequest;
import com.example.nineuniverse.web.dto.CpuBattleStateDto;
import com.example.nineuniverse.web.dto.BattlePowerModifierDto;
import com.example.nineuniverse.web.dto.ZoneFighterDto;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CpuBattleService {

	public static final String SESSION_KEY = "CPU_BATTLE_STATE";

	private final CpuBattleEngine engine;
	private final CardCatalogService cardCatalogService;
	private final DeckService deckService;
	private final MissionService missionService;

	private volatile Map<Short, CardDefDto> cachedDefDtos;

	/** カード定義の image_file 更新後にバトル用 defs キャッシュを捨てる。 */
	public void invalidateDefDtoCache() {
		cachedDefDtos = null;
	}

	@EventListener
	void onCardCatalogRefreshed(CardCatalogRefreshedEvent event) {
		invalidateDefDtoCache();
	}

	@Transactional
	public CpuBattleState start(long userId, long deckId, int level, CpuBattleMode cpuBattleMode, HttpSession session) {
		deckService.requireDeck(userId, deckId);
		List<Short> ids = deckService.cardIdsForDeck(deckId);
		Map<Short, CardDefinition> defs = cardCatalogService.mapById();
		Random rnd = new Random();
		CpuBattleMode mode = cpuBattleMode != null ? cpuBattleMode : CpuBattleMode.ORIGIN;
		int clampedLevel = switch (mode) {
			case ADVANCED -> Math.min(5, Math.max(1, level));
			case ORIGIN -> Math.min(3, Math.max(1, level));
		};
		CpuBattleState st = engine.newBattle(ids, clampedLevel, mode, rnd, defs);
		st.setHumanSlotDeckId(deckId);
		st.setCpuBattleUserId(userId);
		st.setPhase(st.isHumansTurn() ? BattlePhase.HUMAN_INPUT : BattlePhase.CPU_THINKING);
		if (st.getTurnStartedAtMs() <= 0) {
			st.setTurnStartedAtMs(System.currentTimeMillis());
		}
		session.setAttribute(SESSION_KEY, st);
		return st;
	}

	public CpuBattleState current(HttpSession session) {
		Object o = session.getAttribute(SESSION_KEY);
		if (o instanceof CpuBattleState s) {
			return s;
		}
		return null;
	}

	public void clear(HttpSession session) {
		session.removeAttribute(SESSION_KEY);
		session.removeAttribute(CpuLeagueBattleSession.SESSION_KEY);
	}

	public CpuLeagueBattleSession leagueSession(HttpSession session) {
		Object o = session.getAttribute(CpuLeagueBattleSession.SESSION_KEY);
		return o instanceof CpuLeagueBattleSession s ? s : null;
	}

	@Transactional
	public CpuBattleState startLeagueBattle(long userId, long leagueSetId, int humanSlotPick, int level,
			CpuBattleMode cpuBattleMode, HttpSession session) {
		clear(session);
		deckService.requireLeagueSetBattleReady(userId, leagueSetId);
		int hs = humanSlotPick == 2 ? 2 : 1;
		int cs = 1 + new Random().nextInt(2);
		long humanDeckId = deckService.deckIdForLeagueSlot(userId, leagueSetId, hs);
		List<Short> humanCards = deckService.cardIdsForDeck(humanDeckId);
		Map<Short, CardDefinition> defs = cardCatalogService.mapById();
		Random rnd = new Random();
		CpuBattleMode mode = cpuBattleMode != null ? cpuBattleMode : CpuBattleMode.ORIGIN;
		int clampedLevel = switch (mode) {
			case ADVANCED -> Math.min(5, Math.max(1, level));
			case ORIGIN -> Math.min(3, Math.max(1, level));
		};
		CpuLeagueBattleSession lg = new CpuLeagueBattleSession();
		CpuBattleEngine.CpuLeagueDeckPair cpuPair = engine.buildCpuLeagueDeckPair(clampedLevel, mode, rnd, defs);
		lg.setCpuLeagueOpponentDeck1(new ArrayList<>(cpuPair.cpuSlot1Deck()));
		lg.setCpuLeagueOpponentDeck2(new ArrayList<>(cpuPair.cpuSlot2Deck()));
		List<Short> cpuCards = cs == 1 ? lg.getCpuLeagueOpponentDeck1() : lg.getCpuLeagueOpponentDeck2();
		CpuBattleState st = engine.newCpuBattleWithFixedCpuDeck(humanCards, cpuCards, clampedLevel, mode, rnd, defs);
		st.setHumanSlotDeckId(humanDeckId);
		st.setCpuBattleUserId(userId);
		st.setPhase(st.isHumansTurn() ? BattlePhase.HUMAN_INPUT : BattlePhase.CPU_THINKING);
		if (st.getTurnStartedAtMs() <= 0) {
			st.setTurnStartedAtMs(System.currentTimeMillis());
		}
		lg.setCpuBattleUserId(userId);
		lg.setHumanLeagueSetId(leagueSetId);
		lg.setCpuLevel(clampedLevel);
		lg.setCpuBattleMode(mode);
		lg.setHumanActiveSlot(hs);
		lg.setCpuActiveSlot(cs);
		session.setAttribute(CpuLeagueBattleSession.SESSION_KEY, lg);
		session.setAttribute(SESSION_KEY, st);
		return st;
	}

	public CpuBattleStateDto leagueNextCpuBattle(HttpSession session) {
		CpuLeagueBattleSession lg = leagueSession(session);
		CpuBattleState st = current(session);
		if (lg == null || st == null) {
			return null;
		}
		if (!lg.isAwaitingNextGameAck()) {
			throw new IllegalStateException("次のゲームを開始できません");
		}
		if (lg.isMatchComplete()) {
			lg.setAwaitingNextGameAck(false);
			return stateDto(session, true);
		}
		boolean humanWon = st.isHumanWon();
		int nh = humanWon ? (3 - lg.getHumanActiveSlot()) : lg.getHumanActiveSlot();
		int nc = humanWon ? lg.getCpuActiveSlot() : (3 - lg.getCpuActiveSlot());
		lg.setHumanActiveSlot(nh);
		lg.setCpuActiveSlot(nc);
		long uid = lg.getCpuBattleUserId();
		deckService.requireLeagueSetBattleReady(uid, lg.getHumanLeagueSetId());
		long humanDeckId = deckService.deckIdForLeagueSlot(uid, lg.getHumanLeagueSetId(), nh);
		List<Short> humanCards = deckService.cardIdsForDeck(humanDeckId);
		Map<Short, CardDefinition> defs = cardCatalogService.mapById();
		List<Short> cpuCards = cpuLeagueCpuDeckForSlot(lg, nc, defs);
		CpuBattleState nst = engine.newCpuBattleWithFixedCpuDeck(
				humanCards, cpuCards, lg.getCpuLevel(), lg.getCpuBattleMode(), new Random(), defs);
		nst.setHumanSlotDeckId(humanDeckId);
		nst.setCpuBattleUserId(uid);
		nst.setPhase(nst.isHumansTurn() ? BattlePhase.HUMAN_INPUT : BattlePhase.CPU_THINKING);
		if (nst.getTurnStartedAtMs() <= 0) {
			nst.setTurnStartedAtMs(System.currentTimeMillis());
		}
		lg.setAwaitingNextGameAck(false);
		lg.setLeagueLastEndedRoundScored(false);
		session.setAttribute(SESSION_KEY, nst);
		return stateDto(session, true);
	}

	public Map<Short, CardDefinition> defs() {
		return cardCatalogService.mapById();
	}

	public void humanAct(HttpSession session, int levelUpRest, List<String> levelUpDiscardInstanceIds, int levelUpStones, boolean deploy, int deployIndex) {
		CpuBattleState st = current(session);
		if (st == null) {
			return;
		}
		engine.humanTurn(st, levelUpRest, levelUpDiscardInstanceIds, levelUpStones, deploy, deployIndex, defs());
	}

	public CpuBattleStateDto humanCommit(HttpSession session, int levelUpRest, List<String> levelUpDiscardInstanceIds, int levelUpStones,
			String deployInstanceId, int payCostStones, List<String> payCostCardInstanceIds) {
		CpuBattleState st = current(session);
		if (st == null) {
			return null;
		}
		enforceTimeoutIfNeeded(st, defs());
		engine.humanTurnInteractive(st, levelUpRest, levelUpDiscardInstanceIds, levelUpStones, deployInstanceId, payCostStones, payCostCardInstanceIds, defs());
		// クライアントがバトルゾーン描画に defs を必須とするため、コミット応答でも常に含める
		return stateDto(session, true);
	}

	public CpuBattleStateDto stateDto(HttpSession session) {
		return stateDto(session, true);
	}

	public CpuBattleStateDto stateDto(HttpSession session, boolean includeDefs) {
		CpuBattleState st = current(session);
		if (st == null) {
			return null;
		}
		enforceTimeoutIfNeeded(st, defs());
		maybeScoreCpuLeagueSession(session, st);
		CpuBattleStateDto dto = stateDtoFromState(st, includeDefs);
		maybeNotifyCpuWinMission(st, session);
		return attachCpuLeagueSeriesIfPresent(dto, session);
	}

	public CpuBattleStateDto stateDtoFromState(CpuBattleState st) {
		return stateDtoFromState(st, true);
	}

	public CpuBattleStateDto stateDtoFromState(CpuBattleState st, boolean includeDefs) {
		Map<Short, CardDefinition> defs = defs();
		int hbPow = engine.effectiveBattlePower(st.getHumanBattle(), true, st, defs);
		int cbPow = engine.effectiveBattlePower(st.getCpuBattle(), false, st, defs);
		boolean activeHuman = st.isHumansTurn();
		int activeStage = activeHuman ? st.getHumanTimePenaltyStage() : st.getCpuTimePenaltyStage();
		int activeLimit = CpuBattleEngine.timeLimitSecForStage(activeStage);

		Map<Short, CardDefDto> defDtos = includeDefs ? defDtosCached() : null;

		var pc = st.getPendingChoice();
		boolean noLegalDeploy = false;
		if (!st.isGameOver()) {
			BattlePhase ph = st.getPhase();
			if (ph == BattlePhase.HUMAN_INPUT && st.isHumansTurn()
					&& st.getCpuBattle() != null && st.getCpuBattle().getMain() != null) {
				noLegalDeploy = !engine.canMakeLegalDeploy(st, true, defs);
			} else if (ph == BattlePhase.CPU_THINKING && !st.isHumansTurn()
					&& st.getHumanBattle() != null && st.getHumanBattle().getMain() != null) {
				noLegalDeploy = !engine.canMakeLegalDeploy(st, false, defs);
			}
		}

		CpuBattleMode mode = st.getCpuBattleMode() != null ? st.getCpuBattleMode() : CpuBattleMode.ORIGIN;
		return new CpuBattleStateDto(
				st.isPvp(),
				mode.name(),
				st.getCpuLevel(),
				st.isHumanGoesFirst(),
				st.isHumansTurn(),
				st.getPhase() != null ? st.getPhase().name() : null,
				st.getTurnStartedAtMs(),
				activeLimit,
				activeStage,
				st.getHumanStones(),
				st.getCpuStones(),
				st.getHumanDeck().stream().map(CpuBattleService::toBattleCardDto).toList(),
				st.getHumanHand().stream().map(CpuBattleService::toBattleCardDto).toList(),
				st.getHumanRest().stream().map(c -> toBattleCardDtoForRest(st, c)).toList(),
				toZoneDto(st.getHumanBattle(), engine.explainDisplayedPowerContributors(true, st, defs)),
				st.getCpuDeck().stream().map(CpuBattleService::toBattleCardDto).toList(),
				st.getCpuHand().stream().map(CpuBattleService::toBattleCardDto).toList(),
				st.getCpuRest().stream().map(c -> toBattleCardDtoForRest(st, c)).toList(),
				toZoneDto(st.getCpuBattle(), engine.explainDisplayedPowerContributors(false, st, defs)),
				st.getActiveField() != null
						? toBattleCardDto(st.getActiveField())
						: null,
				st.getScrapyardFieldTurnsRemaining(),
				st.getDeathbounceFieldTurnsRemaining(),
				st.getAtlantisFieldCounterDisplay(),
				st.getWeeklyShonenCampFieldCounterDisplay(),
				st.getWorldRebuildFieldCounterDisplay(),
				st.getPaperCityFieldCounterDisplay(),
				st.getHeavensGateFieldCounterDisplay(),
				hbPow,
				cbPow,
				st.getHumanNextDeployBonus(),
				st.getHumanNextElfOnlyBonus(),
				st.getHumanNextDeployCostBonusTimes(),
				st.getHumanNextMechanicStacks(),
				st.getCpuNextDeployBonus(),
				st.getCpuNextElfOnlyBonus(),
				st.getCpuNextDeployCostBonusTimes(),
				st.getCpuNextMechanicStacks(),
				st.getLastMessage(),
				st.isGameOver(),
				st.isHumanWon(),
				noLegalDeploy,
				st.getPendingEffect() != null
						? new com.example.nineuniverse.web.dto.PendingEffectDto(
								st.getPendingEffect().isOwnerHuman(),
								st.getPendingEffect().getMainInstanceId(),
								st.getPendingEffect().getCardId(),
								st.getPendingEffect().getAbilityDeployCode()
						)
						: null,
				pc != null
						? new com.example.nineuniverse.web.dto.PendingChoiceDto(
								pc.getKind() != null ? pc.getKind().name() : null,
								pc.getPrompt(),
								pc.isForHuman(),
								pc.isCpuSlotChooses(),
								pc.getAbilityDeployCode(),
								pc.getStoneCost(),
								pc.getOptionInstanceIds(),
								pc.isForHuman() && !pc.isCpuSlotChooses()
						)
						: null,
				st.getEventLog(),
				defDtos,
				st.isPvp() ? null : st.getHumanSlotDeckId(),
				st.isSpec666NextHumanUndead(),
				st.isSpec666NextCpuUndead(),
				null,
				null,
				null,
				null,
				null
		);
	}

	private Map<Short, CardDefDto> defDtosCached() {
		Map<Short, CardDefDto> cached = cachedDefDtos;
		if (cached != null) {
			return cached;
		}
		synchronized (this) {
			if (cachedDefDtos != null) {
				return cachedDefDtos;
			}
			Map<Short, CardDefinition> defs = defs();
			cachedDefDtos = defs.values().stream()
					.collect(Collectors.toMap(
							CardDefinition::getId,
							d -> {
								String rarity = d.getRarity();
								String rar = rarity != null && !rarity.isBlank() ? rarity : "C";
								String pi = d.getPackInitial();
								String packInitialOut = GameConstants.excludedFromPackOpenAndLibraryListing(d.getId())
										? "—"
										: (pi != null && !pi.isBlank() ? pi.trim() : "STD");
								boolean isField = d.getCardKind() != null && d.getCardKind().trim().equalsIgnoreCase("FIELD");
								String portraitImageFile = GameConstants.effectiveCardImageFile(d.getId(), d.getImageFile());
								return new CardDefDto(
									d.getId(),
									d.getName(),
									(short) (d.getCost() != null ? d.getCost() : 0),
									(short) (d.getBasePower() != null ? d.getBasePower() : 0),
									d.getAttribute(),
									packInitialOut,
									rar,
									rar,
									portraitImageFile,
									d.getAbilityDeployCode(),
									CardAttributeLabels.japaneseName(d.getAttribute()),
									CardAttributeLabels.japaneseNameLines(d.getAttribute()),
									GameConstants.CARD_LAYER_BASE,
									GameConstants.cardLayerBarPath(d.getAttribute()),
									isField ? GameConstants.CARD_LAYER_DATA_FIELD : GameConstants.CARD_LAYER_DATA,
									GameConstants.cardFacePortraitLayerPath(d.getAttribute(), d.getName(), portraitImageFile, d.getId()),
									GameConstants.cardFacePortraitLayerPathAltNfc(d.getAttribute(), d.getName(), portraitImageFile, d.getId()),
									isField,
									d.getCardKind(),
									CardFaceAbilityFormatter.blocksForCardId(d.getId()).stream()
											.map(b -> new AbilityBlockDto(b.getHeadline(), b.getBody()))
											.toList()
								);
							}
					));
			return cachedDefDtos;
		}
	}

	public CpuBattleStateDto cpuStep(HttpSession session) {
		CpuBattleState st = current(session);
		if (st == null) {
			return null;
		}
		enforceTimeoutIfNeeded(st, defs());
		engine.cpuTurn(st, defs(), new Random());
		return stateDto(session, false);
	}

	public CpuBattleStateDto resolvePending(HttpSession session) {
		CpuBattleState st = current(session);
		if (st == null) {
			return null;
		}
		enforceTimeoutIfNeeded(st, defs());
		engine.resolvePendingEffectAndAdvance(st, defs(), new Random());
		return stateDto(session, false);
	}

	public CpuBattleStateDto choose(HttpSession session, CpuBattleChoiceRequest req) {
		CpuBattleState st = current(session);
		if (st == null) {
			return null;
		}
		enforceTimeoutIfNeeded(st, defs());
		engine.applyHumanChoiceAndAdvance(
				st,
				req != null && req.confirm(),
				req != null && req.pickedInstanceIds() != null ? req.pickedInstanceIds() : List.of(),
				defs(),
				new Random()
		);
		return stateDto(session, false);
	}

	public CpuBattleStateDto timeoutTick(HttpSession session) {
		CpuBattleState st = current(session);
		if (st == null) return null;
		enforceTimeoutIfNeeded(st, defs());
		return stateDtoFromState(st, false);
	}

	private void enforceTimeoutIfNeeded(CpuBattleState st, Map<Short, CardDefinition> defs) {
		if (st == null || st.isGameOver()) return;
		// 制限時間・時間切れ強制処理は無効化
		if (st.getTurnStartedAtMs() <= 0) {
			st.setTurnStartedAtMs(System.currentTimeMillis());
		}
	}

	private static BattleCardDto toBattleCardDto(BattleCard c) {
		if (c == null) {
			return null;
		}
		int handAdjDisplay = c.getHandDeployCostModifier() + c.getDeathbounceHandCostStacks();
		return new BattleCardDto(c.getInstanceId(), c.getCardId(), c.isBlankEffects(), handAdjDisplay,
				c.getBattleTribeOverride(), c.getBattleEndPowerBonus());
	}

	/**
	 * 〈化石（フィールド）〉が共有フィールドにある間、両者のレストのカードは種族表示・バーがマーフォークとなる
	 * （{@link CpuBattleEngine} の fossil レスト上書きと一致）。
	 */
	private static boolean fossilFieldMerfolkRestActive(CpuBattleState st) {
		if (st == null || st.getActiveField() == null) {
			return false;
		}
		Short fid = st.getActiveField().getCardId();
		return fid != null && fid.shortValue() == GameConstants.FOSSIL_FIELD_TRANSFORMS_TOKEN_CARD_ID;
	}

	private static BattleCardDto toBattleCardDtoForRest(CpuBattleState st, BattleCard c) {
		if (c == null) {
			return null;
		}
		int handAdjDisplay = c.getHandDeployCostModifier() + c.getDeathbounceHandCostStacks();
		if (fossilFieldMerfolkRestActive(st)) {
			return new BattleCardDto(c.getInstanceId(), c.getCardId(), c.isBlankEffects(), handAdjDisplay, "MERFOLK",
					c.getBattleEndPowerBonus());
		}
		return new BattleCardDto(c.getInstanceId(), c.getCardId(), c.isBlankEffects(), handAdjDisplay,
				c.getBattleTribeOverride(), c.getBattleEndPowerBonus());
	}

	private static ZoneFighterDto toZoneDto(ZoneFighter z, List<BattlePowerModifierDto> powerModifiers) {
		if (z == null) {
			return null;
		}
		var main = toBattleCardDto(z.getMain());
		var under = z.getCostUnder().stream().map(CpuBattleService::toBattleCardDto).toList();
		List<BattlePowerModifierDto> mods = powerModifiers != null ? powerModifiers : List.of();
		return new ZoneFighterDto(main, under, z.getTemporaryPowerBonus() + z.getLevelUpDeployPowerBonus(), mods,
				z.getSpec777RolledPower(), z.getBattleMainLineSeq(), z.getKusuriOpponentDebuffFromDeployStones());
	}

	private List<Short> cpuLeagueCpuDeckForSlot(CpuLeagueBattleSession lg, int slot1Or2, Map<Short, CardDefinition> defs) {
		if (lg.getCpuLeagueOpponentDeck1() != null && lg.getCpuLeagueOpponentDeck2() != null
				&& !lg.getCpuLeagueOpponentDeck1().isEmpty() && !lg.getCpuLeagueOpponentDeck2().isEmpty()) {
			return slot1Or2 == 2 ? lg.getCpuLeagueOpponentDeck2() : lg.getCpuLeagueOpponentDeck1();
		}
		CpuBattleMode mode = lg.getCpuBattleMode() != null ? lg.getCpuBattleMode() : CpuBattleMode.ORIGIN;
		CpuBattleEngine.CpuLeagueDeckPair pair =
				engine.buildCpuLeagueDeckPair(lg.getCpuLevel(), mode, new Random(), defs);
		lg.setCpuLeagueOpponentDeck1(new ArrayList<>(pair.cpuSlot1Deck()));
		lg.setCpuLeagueOpponentDeck2(new ArrayList<>(pair.cpuSlot2Deck()));
		return slot1Or2 == 2 ? lg.getCpuLeagueOpponentDeck2() : lg.getCpuLeagueOpponentDeck1();
	}

	private static void maybeScoreCpuLeagueSession(HttpSession session, CpuBattleState st) {
		Object o = session.getAttribute(CpuLeagueBattleSession.SESSION_KEY);
		if (!(o instanceof CpuLeagueBattleSession lg)) {
			return;
		}
		if (st == null || !st.isGameOver() || lg.isMatchComplete()) {
			return;
		}
		if (lg.isLeagueLastEndedRoundScored()) {
			return;
		}
		lg.setLeagueLastEndedRoundScored(true);
		if (st.isHumanWon()) {
			lg.setHumanWins(lg.getHumanWins() + 1);
		} else {
			lg.setCpuWins(lg.getCpuWins() + 1);
		}
		if (lg.getHumanWins() >= 2 || lg.getCpuWins() >= 2) {
			lg.setMatchComplete(true);
		}
		lg.setAwaitingNextGameAck(true);
	}

	private static CpuBattleStateDto attachCpuLeagueSeriesIfPresent(CpuBattleStateDto d, HttpSession session) {
		Object o = session.getAttribute(CpuLeagueBattleSession.SESSION_KEY);
		if (!(o instanceof CpuLeagueBattleSession lg)) {
			return d;
		}
		boolean await = lg.isAwaitingNextGameAck();
		return new CpuBattleStateDto(
				d.pvpMatch(), d.cpuBattleMode(), d.cpuLevel(), d.humanGoesFirst(), d.humansTurn(), d.phase(),
				d.turnStartedAtMs(), d.activeTimeLimitSec(), d.activePenaltyStage(),
				d.humanStones(), d.cpuStones(), d.humanDeck(), d.humanHand(), d.humanRest(), d.humanBattle(),
				d.cpuDeck(), d.cpuHand(), d.cpuRest(), d.cpuBattle(), d.activeField(), d.scrapyardFieldTurnsRemaining(),
				d.deathbounceFieldTurnsRemaining(), d.atlantisFieldCounterDisplay(),
				d.weeklyShonenCampFieldCounterDisplay(),
				d.worldRebuildFieldCounterDisplay(),
				d.paperCityFieldCounterDisplay(),
				d.heavensGateFieldCounterDisplay(),
				d.humanBattlePower(), d.cpuBattlePower(),
				d.humanNextDeployBonus(), d.humanNextElfOnlyBonus(), d.humanNextDeployCostBonusTimes(),
				d.humanNextMechanicStacks(),
				d.cpuNextDeployBonus(), d.cpuNextElfOnlyBonus(), d.cpuNextDeployCostBonusTimes(), d.cpuNextMechanicStacks(),
				d.lastMessage(), d.gameOver(), d.humanWon(), d.noLegalDeploy(),
				d.pendingEffect(), d.pendingChoice(), d.eventLog(), d.defs(), d.myBattleDeckId(),
				d.spec666NextHumanUndead(), d.spec666NextCpuUndead(),
				lg.getHumanWins(),
				lg.getCpuWins(),
				lg.isMatchComplete(),
				await,
				await);
	}

	private void maybeNotifyCpuWinMission(CpuBattleState st, HttpSession session) {
		if (st == null || st.isPvp() || st.getCpuBattleUserId() == null) {
			return;
		}
		if (!st.isGameOver() || !st.isHumanWon()) {
			return;
		}
		if (session != null) {
			Object o = session.getAttribute(CpuLeagueBattleSession.SESSION_KEY);
			if (o instanceof CpuLeagueBattleSession lg) {
				if (!lg.isMatchComplete() || lg.getHumanWins() < 2) {
					return;
				}
			}
		}
		if (st.isCpuWinMissionNotified()) {
			return;
		}
		st.setCpuWinMissionNotified(true);
		// ミッションは従来どおり L1〜L3 相当（アドバンスド L4・L5 は L3 相当）
		int missionTier = Math.min(Math.max(st.getCpuLevel(), 1), 3);
		missionService.onCpuBattleWon(st.getCpuBattleUserId(), missionTier);
	}
}
