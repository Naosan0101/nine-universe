package com.example.nineuniverse.battle;

import com.example.nineuniverse.card.CardAttributes;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.web.dto.BattlePowerModifierDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CpuBattleEngine {

	private static final short RYUOH_ID = 30;
	private static final short KUSURI_ID = 8;
	private static final short ARCHER_ID = 12;
	private static final short DRAGON_RIDER_ID = 10;
	private static final short GAIKOTSU_ID = 18;
	private static final short SHIREI_ID = 20;
	private static final short HONE_ID = 24;
	private static final short KORYU_ID = 29;
	private static final short SHINY_ID = 31;
	private static final short FROSTKRUL_ID = 32;
	private static final short MISTYINKUL_ID = 33;
	private static final short NEMURY_ID = 40;
	/** フェザリア（レスト回収で自分自身は選べない） */
	private static final short FEATHERIA_ID = 38;
	/** 宝石の地 グロリア輝石台地 */
	private static final short FIELD_GLORIA_ID = 41;
	/** 探鉱の洞窟 ネビュラ坑道 */
	private static final short FIELD_NEBULA_ID = 42;
	/** 決戦の地 カムイ（〈フィールド〉） */
	private static final short FIELD_KAMUI_ID = 49;
	private static final short ARTHUR_ID = 43;

	public CpuBattleState newBattle(List<Short> humanDeckCardIds, int cpuLevel, Random rnd,
			Map<Short, CardDefinition> defs) {
		var st = new CpuBattleState();
		st.setCpuLevel(cpuLevel);
		st.setHumanGoesFirst(rnd.nextBoolean());
		st.setHumansTurn(st.isHumanGoesFirst());
		st.setHumanStones(0);
		st.setCpuStones(0);
		st.setHumanTurnStarts(0);
		st.setCpuTurnStarts(0);

		st.setHumanDeck(buildShuffledInstances(humanDeckCardIds, rnd));
		st.setCpuDeck(buildShuffledInstances(buildCpuDeckIds(cpuLevel, rnd, defs), rnd));

		for (int i = 0; i < 4; i++) {
			drawOne(st.getHumanDeck(), st.getHumanHand());
			drawOne(st.getCpuDeck(), st.getCpuHand());
		}

		st.addLog(st.isHumanGoesFirst() ? "先攻: あなた" : "先攻: CPU");
		// ターン開始時点でストーン付与（先攻1ターン目のみ獲得なし）
		beginTurnGainStone(st, st.isHumansTurn());
		st.setLastMessage("バトル開始");
		return st;
	}

	/** 対人戦: ホストが human、ゲストが cpu スロット。cpuLevel は未使用。 */
	public CpuBattleState newPvpBattle(List<Short> hostDeckCardIds, List<Short> guestDeckCardIds, Random rnd,
			Map<Short, CardDefinition> defs) {
		var st = new CpuBattleState();
		st.setPvp(true);
		st.setCpuLevel(0);
		st.setHumanGoesFirst(rnd.nextBoolean());
		st.setHumansTurn(st.isHumanGoesFirst());
		st.setHumanStones(0);
		st.setCpuStones(0);
		st.setHumanTurnStarts(0);
		st.setCpuTurnStarts(0);

		st.setHumanDeck(buildShuffledInstances(hostDeckCardIds, rnd));
		st.setCpuDeck(buildShuffledInstances(guestDeckCardIds, rnd));

		for (int i = 0; i < 4; i++) {
			drawOne(st.getHumanDeck(), st.getHumanHand());
			drawOne(st.getCpuDeck(), st.getCpuHand());
		}

		st.addLog(st.isHumanGoesFirst() ? "先攻: ホスト" : "先攻: ゲスト");
		beginTurnGainStone(st, st.isHumansTurn());
		st.setLastMessage("対人戦開始");
		return st;
	}

	private static int clampInt(int n, int min, int max) {
		return Math.max(min, Math.min(max, n));
	}

	private static double clampDouble(double n, double min, double max) {
		return Math.max(min, Math.min(max, n));
	}

	private static boolean isFieldCard(CardDefinition d) {
		return d != null && d.getCardKind() != null && "FIELD".equalsIgnoreCase(d.getCardKind().trim());
	}

	/** 種族に「人間」を含み、かつファイター（フィールドカードは除外） */
	private static boolean isHumanFighterDef(CardDefinition d) {
		if (d == null || isFieldCard(d)) {
			return false;
		}
		if (d.getCardKind() == null || !"FIGHTER".equalsIgnoreCase(d.getCardKind().trim())) {
			return false;
		}
		return CardAttributes.hasAttribute(d, "HUMAN");
	}

	/** バトルログ用。対人戦では相手プレイヤー相当を「相手」、CPU戦では「CPU」と表記する。 */
	private static String opponentActorLogLabel(CpuBattleState st) {
		return st != null && st.isPvp() ? "相手" : "CPU";
	}

	private static int rarityRank(String rarity) {
		if (rarity == null) return 0;
		return switch (rarity.trim()) {
			case "Reg" -> 3;
			case "Ep" -> 2;
			case "R" -> 1;
			default -> 0; // C or unknown
		};
	}

	private List<Short> buildCpuDeckIds(int cpuLevel, Random rnd, Map<Short, CardDefinition> defs) {
		final int lvl = clampInt(cpuLevel, 1, 10);
		final String[] coreTribes = new String[] {"HUMAN", "ELF", "UNDEAD", "DRAGON"};

		List<Short> picked = new ArrayList<>();
		Map<Short, Integer> cnt = new HashMap<>();
		Map<String, Integer> tribeCount = new HashMap<>();

		// Deck "theme" tribe. Higher levels will adhere to it more often, but not always.
		final String theme = coreTribes[rnd.nextInt(coreTribes.length)];
		// Convergence strength: how much we bias toward the (theme or currently dominant) tribe.
		// Higher levels should converge much harder, but never become deterministic.
		final double convergeBoost = clampDouble(0.55 + 0.40 * (lvl - 1), 0.55, 4.6);
		// Rarity strength: higher levels more likely to pick high rarity.
		final double rarityFactor = clampDouble(0.22 + 0.12 * (lvl - 1), 0.22, 1.3);

		while (picked.size() < 8) {
			// Determine current dominant tribe in picked cards to encourage convergence.
			String dominant = theme;
			int best = -1;
			for (String t : coreTribes) {
				int c = tribeCount.getOrDefault(t, 0);
				if (c > best) {
					best = c;
					dominant = t;
				}
			}

			// Weighted pick from all card ids (1..30) with max 2 copies.
			double totalW = 0.0;
			double[] w = new double[31];
			for (short id = 1; id <= 30; id++) {
				if (cnt.getOrDefault(id, 0) >= 2) {
					w[id] = 0.0;
					continue;
				}
				CardDefinition d = defs != null ? defs.get(id) : null;
				if (d == null) {
					w[id] = 0.0;
					continue;
				}
				double ww = 1.0;

				// ① Tribe convergence: prefer theme & dominant tribe (but never exclusive).
				boolean hasTheme = CardAttributes.hasAttribute(d, theme);
				boolean hasDom = CardAttributes.hasAttribute(d, dominant);
				// Dragons are intentionally harder to converge into (they are weaker overall),
				// so reduce convergence pressure when the target tribe is DRAGON.
				double themeMul = theme != null && theme.equals("DRAGON") ? 0.45 : 1.0;
				double domMul = dominant != null && dominant.equals("DRAGON") ? 0.55 : 1.0;
				if (hasTheme) ww *= (1.0 + convergeBoost * 0.70 * themeMul);
				if (hasDom) ww *= (1.0 + convergeBoost * 1.15 * domMul);

				// ② Rarity bias: higher level → higher rarity is more likely.
				int rr = rarityRank(d.getRarity());
				ww *= (1.0 + rr * rarityFactor);
				// レベル1〜2: レジェンダリー・エピックを極めて／かなり出にくくする
				if (lvl <= 2 && d.getRarity() != null) {
					String tr = d.getRarity().trim();
					if ("Reg".equalsIgnoreCase(tr)) {
						ww *= lvl == 1 ? 0.012 : 0.06;
					} else if ("Ep".equalsIgnoreCase(tr)) {
						ww *= lvl == 1 ? 0.18 : 0.35;
					} else if ("R".equalsIgnoreCase(tr)) {
						ww *= lvl == 1 ? 0.58 : 0.72;
					}
				}
				// Extra push at higher rarities — low CPU levels skip this so L1/L2 stay mostly C/R.
				if (lvl > 2) {
					if (rr >= 2) ww *= (1.0 + 0.35 * rarityFactor);
					if (rr >= 3) ww *= (1.0 + 0.55 * rarityFactor);
				}

				// Mild variety: avoid too many exact same id early.
				int already = cnt.getOrDefault(id, 0);
				if (already == 1) ww *= 0.72;

				w[id] = ww;
				totalW += ww;
			}

			if (totalW <= 0) {
				// Fallback (shouldn't happen): uniform random.
				short id = (short) (1 + rnd.nextInt(30));
				if (cnt.getOrDefault(id, 0) >= 2) continue;
				picked.add(id);
				cnt.put(id, cnt.getOrDefault(id, 0) + 1);
				continue;
			}

			double r = rnd.nextDouble() * totalW;
			short chosen = 1;
			for (short id = 1; id <= 30; id++) {
				double ww = w[id];
				if (ww <= 0) continue;
				r -= ww;
				if (r <= 0) {
					chosen = id;
					break;
				}
			}

			picked.add(chosen);
			cnt.put(chosen, cnt.getOrDefault(chosen, 0) + 1);

			CardDefinition cd = defs != null ? defs.get(chosen) : null;
			if (cd != null) {
				// Increment tribe counts for convergence. Composite attributes increment all matching core tribes.
				for (String t : coreTribes) {
					if (CardAttributes.hasAttribute(cd, t)) {
						tribeCount.put(t, tribeCount.getOrDefault(t, 0) + 1);
					}
				}
			}
		}
		return picked;
	}

	private List<BattleCard> buildShuffledInstances(List<Short> ids, Random rnd) {
		List<BattleCard> deck = ids.stream()
				.map(id -> new BattleCard(UUID.randomUUID().toString(), id))
				.collect(Collectors.toCollection(ArrayList::new));
		Collections.shuffle(deck, rnd);
		return deck;
	}

	private void drawOne(List<BattleCard> deck, List<BattleCard> hand) {
		if (deck.isEmpty()) {
			return;
		}
		hand.add(0, deck.remove(0));
	}

	private void stagePendingDeployEffect(CpuBattleState st, boolean ownerHuman, CardDefinition mainDef, ZoneFighter zone) {
		if (st == null || mainDef == null || zone == null || zone.getMain() == null) return;
		String code = mainDef.getAbilityDeployCode();
		// 配置能力が無い（効果なし）場合でも、表示→解決→ノック判定へ進む必要がある
		st.setPendingEffect(new PendingEffect(ownerHuman, zone.getMain().getInstanceId(), zone.getMain().getCardId(), code, false));
		st.setPhase(ownerHuman ? BattlePhase.HUMAN_EFFECT_PENDING : BattlePhase.CPU_EFFECT_PENDING);
		st.setLastMessage("効果を処理中…");
	}

	public void resolvePendingEffectAndAdvance(CpuBattleState st, Map<Short, CardDefinition> defs, Random rnd) {
		if (st == null || st.isGameOver()) return;
		PendingEffect pe = st.getPendingEffect();
		if (pe == null) return;

		// 効果適用（選択待ちの間に二重適用しない）
		if (!pe.isApplied()) {
			// 相手の竜王、または相手のミスティンクルがいる場合は〈配置〉が無効（竜王は〈常時〉も無効）
			boolean deploySuppressed = opponentSuppressesDeployEffects(st, pe.isOwnerHuman());
			CardDefinition d = defs.get(pe.getCardId());
			ZoneFighter ownZone = pe.isOwnerHuman() ? st.getHumanBattle() : st.getCpuBattle();
			BattleCard deployedMain = null;
			if (ownZone != null && ownZone.getMain() != null && pe.getMainInstanceId() != null
					&& pe.getMainInstanceId().equals(ownZone.getMain().getInstanceId())) {
				deployedMain = ownZone.getMain();
			}
			if (!deploySuppressed && d != null) {
				if (pe.isOwnerHuman()) {
					applyDeployHuman(st, d, defs, deployedMain);
				} else if (st.isPvp()) {
					applyDeployHumanAsCpuSide(st, d, defs, deployedMain);
				} else {
					applyDeployCpu(st, d, defs, rnd != null ? rnd : new Random(), deployedMain);
				}
			}
			pe.setApplied(true);
			st.setPendingEffect(pe);
		}

		// 選択が必要なら、ここで止める
		PendingChoice pend0 = st.getPendingChoice();
		if (pend0 != null && (pend0.isForHuman() || pend0.isCpuSlotChooses())) {
			st.setPhase(BattlePhase.HUMAN_CHOICE);
			st.setLastMessage("選択してください");
			return;
		}

		// 配置能力の結果を含めて強さ条件を満たす必要がある
		if (pe.isOwnerHuman()) {
			if (st.getCpuBattle() != null) {
				int me = effectiveBattlePower(st.getHumanBattle(), true, st, defs);
				int opp = effectiveBattlePower(st.getCpuBattle(), false, st, defs);
				if (me < opp) {
					// 敗北確定の前に確認（キャンセルならスナップショットへ巻き戻す）
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_ACCEPT_LOSS,
							"能力をすべて適用しても強さが足りません。このまま進めますか？（進めると敗北になります）",
							true,
							"CONFIRM_ACCEPT_LOSS",
							0,
							List.of()
					));
					st.setPhase(BattlePhase.HUMAN_CHOICE);
					st.setLastMessage("確認してください");
					return;
				}
			}
		} else {
			if (st.getHumanBattle() != null) {
				int cpu = effectiveBattlePower(st.getCpuBattle(), false, st, defs);
				int hum = effectiveBattlePower(st.getHumanBattle(), true, st, defs);
				if (cpu < hum) {
					if (st.isPvp()) {
						st.setPendingChoice(new PendingChoice(
								ChoiceKind.CONFIRM_ACCEPT_LOSS,
								"能力をすべて適用しても強さが足りません。このまま進めますか？（進めると敗北になります）",
								false,
								"CONFIRM_ACCEPT_LOSS",
								0,
								List.of(),
								true
						));
						st.setPhase(BattlePhase.HUMAN_CHOICE);
						st.setLastMessage("確認してください");
						return;
					}
					st.setGameOver(true);
					st.setHumanWon(true);
					st.setPhase(BattlePhase.GAME_OVER);
					st.setLastMessage("勝利（CPUが能力後も相手以上になれません）");
					st.addLog("勝利: CPUが強さ条件を満たせない");
					return;
				}
			}
		}

		st.setPendingEffect(null);

		if (st.getPhase() == BattlePhase.HUMAN_EFFECT_PENDING) {
			resolveKnockAndDraw(st, true, defs);
			resetTurnBuffs(st);
			st.setHumansTurn(false);
			st.setPhase(BattlePhase.CPU_THINKING);
			// CPUのターン開始：ストーン付与（先攻1ターン目のみ獲得なし）
			beginTurnGainStone(st, false);
			st.setLastMessage(st.isPvp() ? "ゲストのターン" : "CPUのターン");
		} else if (st.getPhase() == BattlePhase.CPU_EFFECT_PENDING) {
			resolveKnockAndDraw(st, false, defs);
			resetTurnBuffs(st);
			st.setHumansTurn(true);
			st.setPhase(BattlePhase.HUMAN_INPUT);
			// 人間のターン開始：ストーン付与（先攻1ターン目のみ獲得なし）
			beginTurnGainStone(st, true);
			st.setLastMessage(st.isPvp() ? "ホストのターン" : "あなたのターン");
		}
	}

	public void applyHumanChoiceAndAdvance(CpuBattleState st, boolean confirm, List<String> pickedInstanceIds,
			Map<Short, CardDefinition> defs, Random rnd) {
		if (st == null || st.isGameOver()) return;
		PendingChoice pc = st.getPendingChoice();
		if (pc == null || !pc.isForHuman() || pc.isCpuSlotChooses()) return;

		switch (pc.getKind()) {
			case CONFIRM_ACCEPT_LOSS -> {
				if (confirm) {
					st.setGameOver(true);
					st.setHumanWon(false);
					st.setPhase(BattlePhase.GAME_OVER);
					st.setLastMessage("敗北（能力後も相手以上になれません）");
					st.addLog("敗北: 能力後も強さ条件を満たせない");
				} else {
					CpuBattleState snap = st.getConfirmAcceptLossSnapshot();
					if (snap != null) {
						// スナップショットへ復帰
						st.setCpuLevel(snap.getCpuLevel());
						st.setHumanGoesFirst(snap.isHumanGoesFirst());
						st.setHumansTurn(snap.isHumansTurn());
						st.setHumanTurnStarts(snap.getHumanTurnStarts());
						st.setCpuTurnStarts(snap.getCpuTurnStarts());
						st.setPhase(BattlePhase.HUMAN_INPUT);
						st.setPendingEffect(null);
						st.setPendingChoice(null);
						st.setHumanNextDeployBonus(snap.getHumanNextDeployBonus());
						st.setCpuNextDeployBonus(snap.getCpuNextDeployBonus());
						st.setHumanNextElfOnlyBonus(snap.getHumanNextElfOnlyBonus());
						st.setCpuNextElfOnlyBonus(snap.getCpuNextElfOnlyBonus());
						st.setHumanNextDeployCostBonusTimes(snap.getHumanNextDeployCostBonusTimes());
						st.setCpuNextDeployCostBonusTimes(snap.getCpuNextDeployCostBonusTimes());
						st.setHumanNextMechanicStacks(snap.getHumanNextMechanicStacks());
						st.setCpuNextMechanicStacks(snap.getCpuNextMechanicStacks());
						st.setPowerSwapActive(snap.isPowerSwapActive());
						st.setHumanKoryuBonus(snap.getHumanKoryuBonus());
						st.setCpuKoryuBonus(snap.getCpuKoryuBonus());

						st.setHumanDeck(copyCards(snap.getHumanDeck()));
						st.setHumanHand(copyCards(snap.getHumanHand()));
						st.setHumanRest(copyCards(snap.getHumanRest()));
						st.setHumanBattle(copyZone(snap.getHumanBattle()));
						st.setHumanStones(snap.getHumanStones());

						st.setCpuDeck(copyCards(snap.getCpuDeck()));
						st.setCpuHand(copyCards(snap.getCpuHand()));
						st.setCpuRest(copyCards(snap.getCpuRest()));
						st.setCpuBattle(copyZone(snap.getCpuBattle()));
						st.setCpuStones(snap.getCpuStones());

						st.setLastMessage("操作をキャンセルしました");
					} else {
						st.setPhase(BattlePhase.HUMAN_INPUT);
						st.setPendingEffect(null);
						st.setPendingChoice(null);
						st.setLastMessage("操作をキャンセルしました");
					}
					st.setGameOver(false);
					st.setHumanWon(false);
				}
				st.setConfirmAcceptLossSnapshot(null);
				return;
			}
			case CONFIRM_OPTIONAL_STONE -> {
				if (confirm && "CRYSTAKUL".equals(pc.getAbilityDeployCode())) {
					ensureNebulaStoneForCrystakulOptionalPayment(st, true, defs);
				}
				if (confirm && pc.getStoneCost() > 0 && st.getHumanStones() >= pc.getStoneCost()) {
					st.setHumanStones(st.getHumanStones() - pc.getStoneCost());
					st.addLog("ストーンを" + pc.getStoneCost() + "使用");
					// ability ごとの追加処理
					if ("SAMURAI".equals(pc.getAbilityDeployCode())) {
						// 対人戦: 相手（ゲスト）が選んで捨てる。CPU戦: CPUは自動で捨てる。
						if (st.isPvp()) {
							List<String> opts = new ArrayList<>();
							for (BattleCard c : st.getCpuHand()) opts.add(c.getInstanceId());
							if (!opts.isEmpty()) {
								st.setPendingChoice(new PendingChoice(
										ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
										"サムライ（捨てるカードを1枚選択）",
										false,
										"SAMURAI",
										0,
										opts,
										true
								));
							}
							return;
						}
						// CPU戦（簡易）: 最右＝末尾から1枚
						if (!st.getCpuHand().isEmpty()) {
							st.getCpuRest().add(st.getCpuHand().remove(st.getCpuHand().size() - 1));
							st.addLog("CPUは手札を1枚レストへ");
						}
					} else if ("KOSAKUIN".equals(pc.getAbilityDeployCode())) {
						// 交換対象を選ぶ
						List<String> opts = new ArrayList<>();
						for (BattleCard c : st.getHumanRest()) opts.add(c.getInstanceId());
						for (BattleCard c : st.getHumanHand()) opts.add(c.getInstanceId());
						if (!opts.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_SWAP_REST_AND_HAND,
									"交換するカードを2枚選択（レスト1枚＋手札1枚）",
									true,
									"KOSAKUIN",
									0,
									opts
							));
							return;
						}
					} else if ("MIKO".equals(pc.getAbilityDeployCode())) {
						st.setHumanNextDeployBonus(st.getHumanNextDeployBonus() + 1);
						st.addLog("エルフの巫女: 次の配置+1");
					} else if ("YOSEI".equals(pc.getAbilityDeployCode())) {
						st.setHumanNextElfOnlyBonus(st.getHumanNextElfOnlyBonus() + 3);
						st.addLog("ウッドエルフ: 次のエルフ配置+3");
					} else if ("SHOKIN".equals(pc.getAbilityDeployCode())) {
						st.setHumanNextDeployCostBonusTimes(st.getHumanNextDeployCostBonusTimes() + 1);
						st.addLog("隊長: 次の配置はコストぶん強化");
					} else if ("FUWAFUWA".equals(pc.getAbilityDeployCode())) {
						if (st.getHumanBattle() != null) {
							st.getHumanBattle().setReturnToHandOnKnock(true);
							st.addLog("ふわふわ: 次に手札へ戻る");
						}
					} else if ("NIDONEBI".equals(pc.getAbilityDeployCode())) {
						moveOneCardIdToDeckBottom(st.getHumanRest(), st.getHumanDeck(), (short) 18);
						st.addLog("ネクロマンサー: デッキ最下段へ");
					} else if ("RYUNOTAMAGO".equals(pc.getAbilityDeployCode())) {
						// ドラゴンを選ぶ
						List<String> opts = new ArrayList<>();
						for (BattleCard c : st.getHumanRest()) {
							if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "DRAGON")) opts.add(c.getInstanceId());
						}
						if (!opts.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
									"ドラゴンの卵（レストのドラゴンを選択）",
									true,
									"RYUNOTAMAGO",
									0,
									opts
							));
							return;
						}
					} else if ("KORYU".equals(pc.getAbilityDeployCode())) {
						int elves = countAttributeInRest(st.getHumanRest(), defs, "ELF");
						if (elves > 0 && st.getHumanBattle() != null) {
							st.setHumanKoryuBonus(elves);
							st.addLog("古竜: 次の相手ターン終了まで +" + elves);
						}
					} else if ("NOROWARETA".equals(pc.getAbilityDeployCode())) {
						// 呪われた亡者: ストーン1消費で「レストからランダム1枚→デッキへ戻してシャッフル」
						if (!st.getHumanRest().isEmpty()) {
							Random rr = rnd != null ? rnd : new Random();
							int r = rr.nextInt(st.getHumanRest().size());
							BattleCard c = st.getHumanRest().remove(r);
							st.getHumanDeck().add(0, c);
							Collections.shuffle(st.getHumanDeck(), rr);
							st.addLog("呪われた亡者: レストから1枚をデッキへ戻しシャッフル");
						}
					} else if ("CRYSTAKUL".equals(pc.getAbilityDeployCode())) {
						st.setHumanStones(st.getHumanStones() + 5);
						st.addLog("クリスタクル: ストーン+5");
					} else if ("FEZARIA".equals(pc.getAbilityDeployCode())) {
						List<String> opts = new ArrayList<>();
						for (BattleCard c : st.getHumanRest()) {
							if (isFezariaPickableCarbuncleInRest(c, defs)) {
								opts.add(c.getInstanceId());
							}
						}
						if (!opts.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_UP_TO_TWO_FROM_REST_TO_HAND,
									"フェザリア（フェザリア以外のカーバンクルを2枚まで）",
									true,
									"FEZARIA",
									0,
									opts
							));
							return;
						}
						st.addLog("フェザリア: レストに回収対象のカードがない");
					}
				} else {
					st.addLog("効果を使用しなかった");
				}
			}
			case SELECT_ONE_FROM_HAND_TO_REST -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) return;
				BattleCard c = removeByInstanceId(st.getHumanHand(), pickedInstanceIds.get(0));
				if (c != null) {
					st.getHumanRest().add(c);
					st.addLog("手札を1枚レストへ");
				}
			}
			case SELECT_TWO_FROM_HAND_TO_REST -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 2) return;
				BattleCard a = removeByInstanceId(st.getHumanHand(), pickedInstanceIds.get(0));
				BattleCard b = removeByInstanceId(st.getHumanHand(), pickedInstanceIds.get(1));
				if (a == null || b == null) {
					if (a != null) st.getHumanHand().add(0, a);
					if (b != null) st.getHumanHand().add(0, b);
					return;
				}
				st.getHumanRest().add(a);
				st.getHumanRest().add(b);
				st.addLog("手札を2枚レストへ");
			}
			case SELECT_ONE_FROM_REST_TO_HAND -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) return;
				BattleCard c = removeByInstanceId(st.getHumanRest(), pickedInstanceIds.get(0));
				if (c != null) {
					if ("TANKOFU".equals(pc.getAbilityDeployCode())) {
						c.setBlankEffects(true);
						st.addLog("炭鉱夫: 手札へ（効果なし）");
					} else if ("JOSHU".equals(pc.getAbilityDeployCode())) {
						st.addLog("助手: 手札へ");
					} else if ("ASTORIA".equals(pc.getAbilityDeployCode())) {
						c.setHandDeployCostModifier(-1);
						st.addLog("研究者アストリア: 手札へ（コスト-1）");
					} else {
						st.addLog("レストから手札へ");
					}
					st.getHumanHand().add(0, c);
				}
			}
			case SELECT_SWAP_REST_AND_HAND -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 2) return;
				String a = pickedInstanceIds.get(0);
				String b = pickedInstanceIds.get(1);
				BattleCard restC = removeByInstanceId(st.getHumanRest(), a);
				BattleCard handC = removeByInstanceId(st.getHumanHand(), b);
				if (restC == null || handC == null) {
					// 逆順を試す
					if (restC != null) st.getHumanRest().add(restC);
					if (handC != null) st.getHumanHand().add(handC);
					restC = removeByInstanceId(st.getHumanRest(), b);
					handC = removeByInstanceId(st.getHumanHand(), a);
				}
				if (restC != null && handC != null) {
					st.getHumanRest().add(handC);
					st.getHumanHand().add(0, restC);
					st.addLog("レストと手札を交換");
				}
			}
			case SELECT_UP_TO_TWO_FROM_REST_TO_HAND -> {
				if (pickedInstanceIds == null) {
					return;
				}
				if (pickedInstanceIds.size() > 2) {
					return;
				}
				Set<String> uniq = new HashSet<>(pickedInstanceIds);
				if (uniq.size() != pickedInstanceIds.size()) {
					return;
				}
				int n = 0;
				for (String id : pickedInstanceIds) {
					BattleCard c = removeByInstanceId(st.getHumanRest(), id);
					if (c != null && isFezariaPickableCarbuncleInRest(c, defs)) {
						st.getHumanHand().add(0, c);
						n++;
					}
				}
				st.addLog("フェザリア: レストから手札へ（" + n + "枚）");
			}
		}

		st.setPendingChoice(null);
		st.setPhase(st.isHumansTurn() ? BattlePhase.HUMAN_EFFECT_PENDING : BattlePhase.CPU_EFFECT_PENDING);
		st.setLastMessage("効果を処理中…");
		// choice は「配置効果の続き」なので、resolve をもう一度呼べば進む設計（UI側で即 resolve してもOK）
	}

	/** 対人戦: ゲスト（cpu スロット）の選択 */
	public void applyCpuSlotChoiceAndAdvance(CpuBattleState st, boolean confirm, List<String> pickedInstanceIds,
			Map<Short, CardDefinition> defs, Random rnd) {
		if (st == null || st.isGameOver()) return;
		PendingChoice pc = st.getPendingChoice();
		if (pc == null || !pc.isCpuSlotChooses()) return;

		switch (pc.getKind()) {
			case CONFIRM_ACCEPT_LOSS -> {
				if (confirm) {
					st.setGameOver(true);
					st.setHumanWon(true);
					st.setPhase(BattlePhase.GAME_OVER);
					st.setLastMessage("敗北（能力後も相手以上になれません）");
					st.addLog("敗北: 能力後も強さ条件を満たせない");
				} else {
					CpuBattleState snap = st.getConfirmAcceptLossSnapshot();
					if (snap != null) {
						st.setCpuLevel(snap.getCpuLevel());
						st.setPvp(snap.isPvp());
						st.setHumanGoesFirst(snap.isHumanGoesFirst());
						st.setHumansTurn(snap.isHumansTurn());
						st.setHumanTurnStarts(snap.getHumanTurnStarts());
						st.setCpuTurnStarts(snap.getCpuTurnStarts());
						st.setPhase(BattlePhase.CPU_THINKING);
						st.setPendingEffect(null);
						st.setPendingChoice(null);
						st.setHumanNextDeployBonus(snap.getHumanNextDeployBonus());
						st.setCpuNextDeployBonus(snap.getCpuNextDeployBonus());
						st.setHumanNextElfOnlyBonus(snap.getHumanNextElfOnlyBonus());
						st.setCpuNextElfOnlyBonus(snap.getCpuNextElfOnlyBonus());
						st.setHumanNextDeployCostBonusTimes(snap.getHumanNextDeployCostBonusTimes());
						st.setCpuNextDeployCostBonusTimes(snap.getCpuNextDeployCostBonusTimes());
						st.setHumanNextMechanicStacks(snap.getHumanNextMechanicStacks());
						st.setCpuNextMechanicStacks(snap.getCpuNextMechanicStacks());
						st.setPowerSwapActive(snap.isPowerSwapActive());
						st.setHumanKoryuBonus(snap.getHumanKoryuBonus());
						st.setCpuKoryuBonus(snap.getCpuKoryuBonus());

						st.setHumanDeck(copyCards(snap.getHumanDeck()));
						st.setHumanHand(copyCards(snap.getHumanHand()));
						st.setHumanRest(copyCards(snap.getHumanRest()));
						st.setHumanBattle(copyZone(snap.getHumanBattle()));
						st.setHumanStones(snap.getHumanStones());

						st.setCpuDeck(copyCards(snap.getCpuDeck()));
						st.setCpuHand(copyCards(snap.getCpuHand()));
						st.setCpuRest(copyCards(snap.getCpuRest()));
						st.setCpuBattle(copyZone(snap.getCpuBattle()));
						st.setCpuStones(snap.getCpuStones());

						st.setLastMessage("操作をキャンセルしました");
					} else {
						st.setPhase(BattlePhase.CPU_THINKING);
						st.setPendingEffect(null);
						st.setPendingChoice(null);
						st.setLastMessage("操作をキャンセルしました");
					}
					st.setGameOver(false);
					st.setHumanWon(false);
				}
				st.setConfirmAcceptLossSnapshot(null);
				return;
			}
			case CONFIRM_OPTIONAL_STONE -> {
				if (confirm && "CRYSTAKUL".equals(pc.getAbilityDeployCode())) {
					ensureNebulaStoneForCrystakulOptionalPayment(st, false, defs);
				}
				if (confirm && pc.getStoneCost() > 0 && st.getCpuStones() >= pc.getStoneCost()) {
					st.setCpuStones(st.getCpuStones() - pc.getStoneCost());
					st.addLog("ストーンを" + pc.getStoneCost() + "使用");
					if ("SAMURAI".equals(pc.getAbilityDeployCode())) {
						// 対人戦: 相手（ホスト）が選んで捨てる。CPU戦: 人間が選ぶ（CPUが出した場合は applyDeployCpu で処理）。
						if (st.isPvp()) {
							List<String> opts = new ArrayList<>();
							for (BattleCard c : st.getHumanHand()) opts.add(c.getInstanceId());
							if (!opts.isEmpty()) {
								st.setPendingChoice(new PendingChoice(
										ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
										"サムライ（捨てるカードを1枚選択）",
										true,
										"SAMURAI",
										0,
										opts
								));
							}
							return;
						}
						// 非PvP（保険）: 末尾から1枚
						if (!st.getHumanHand().isEmpty()) {
							st.getHumanRest().add(st.getHumanHand().remove(st.getHumanHand().size() - 1));
							st.addLog("相手は手札を1枚レストへ");
						}
					} else if ("KOSAKUIN".equals(pc.getAbilityDeployCode())) {
						List<String> opts = new ArrayList<>();
						for (BattleCard c : st.getCpuRest()) opts.add(c.getInstanceId());
						for (BattleCard c : st.getCpuHand()) opts.add(c.getInstanceId());
						if (!opts.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_SWAP_REST_AND_HAND,
									"交換するカードを2枚選択（レスト1枚＋手札1枚）",
									false,
									"KOSAKUIN",
									0,
									opts,
									true
							));
							return;
						}
					} else if ("MIKO".equals(pc.getAbilityDeployCode())) {
						st.setCpuNextDeployBonus(st.getCpuNextDeployBonus() + 1);
						st.addLog("エルフの巫女: 次の配置+1");
					} else if ("YOSEI".equals(pc.getAbilityDeployCode())) {
						st.setCpuNextElfOnlyBonus(st.getCpuNextElfOnlyBonus() + 3);
						st.addLog("ウッドエルフ: 次のエルフ配置+3");
					} else if ("SHOKIN".equals(pc.getAbilityDeployCode())) {
						st.setCpuNextDeployCostBonusTimes(st.getCpuNextDeployCostBonusTimes() + 1);
						st.addLog("隊長: 次の配置はコストぶん強化");
					} else if ("FUWAFUWA".equals(pc.getAbilityDeployCode())) {
						if (st.getCpuBattle() != null) {
							st.getCpuBattle().setReturnToHandOnKnock(true);
							st.addLog("ふわふわ: 次に手札へ戻る");
						}
					} else if ("NIDONEBI".equals(pc.getAbilityDeployCode())) {
						moveOneCardIdToDeckBottom(st.getCpuRest(), st.getCpuDeck(), (short) 18);
						st.addLog("ネクロマンサー: デッキ最下段へ");
					} else if ("RYUNOTAMAGO".equals(pc.getAbilityDeployCode())) {
						List<String> opts = new ArrayList<>();
						for (BattleCard c : st.getCpuRest()) {
							if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "DRAGON")) opts.add(c.getInstanceId());
						}
						if (!opts.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
									"ドラゴンの卵（レストのドラゴンを選択）",
									false,
									"RYUNOTAMAGO",
									0,
									opts,
									true
							));
							return;
						}
					} else if ("KORYU".equals(pc.getAbilityDeployCode())) {
						int elves = countAttributeInRest(st.getCpuRest(), defs, "ELF");
						if (elves > 0 && st.getCpuBattle() != null) {
							st.setCpuKoryuBonus(elves);
							st.addLog("古竜: 次の相手ターン終了まで +" + elves);
						}
					} else if ("NOROWARETA".equals(pc.getAbilityDeployCode())) {
						if (!st.getCpuRest().isEmpty()) {
							Random rr = rnd != null ? rnd : new Random();
							int r = rr.nextInt(st.getCpuRest().size());
							BattleCard c = st.getCpuRest().remove(r);
							st.getCpuDeck().add(0, c);
							Collections.shuffle(st.getCpuDeck(), rr);
							st.addLog("呪われた亡者: レストから1枚をデッキへ戻しシャッフル");
						}
					} else if ("CRYSTAKUL".equals(pc.getAbilityDeployCode())) {
						st.setCpuStones(st.getCpuStones() + 5);
						st.addLog("クリスタクル: ストーン+5");
					} else if ("FEZARIA".equals(pc.getAbilityDeployCode())) {
						List<String> opts = new ArrayList<>();
						for (BattleCard c : st.getCpuRest()) {
							if (isFezariaPickableCarbuncleInRest(c, defs)) {
								opts.add(c.getInstanceId());
							}
						}
						if (!opts.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_UP_TO_TWO_FROM_REST_TO_HAND,
									"フェザリア（フェザリア以外のカーバンクルを2枚まで）",
									false,
									"FEZARIA",
									0,
									opts,
									true
							));
							return;
						}
						st.addLog("フェザリア: レストに回収対象のカードがない");
					}
				} else {
					st.addLog("効果を使用しなかった");
				}
			}
			case SELECT_ONE_FROM_HAND_TO_REST -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) return;
				BattleCard c = removeByInstanceId(st.getCpuHand(), pickedInstanceIds.get(0));
				if (c != null) {
					st.getCpuRest().add(c);
					st.addLog("手札を1枚レストへ");
				}
			}
			case SELECT_TWO_FROM_HAND_TO_REST -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 2) return;
				BattleCard a = removeByInstanceId(st.getCpuHand(), pickedInstanceIds.get(0));
				BattleCard b = removeByInstanceId(st.getCpuHand(), pickedInstanceIds.get(1));
				if (a == null || b == null) {
					if (a != null) st.getCpuHand().add(0, a);
					if (b != null) st.getCpuHand().add(0, b);
					return;
				}
				st.getCpuRest().add(a);
				st.getCpuRest().add(b);
				st.addLog("手札を2枚レストへ");
			}
			case SELECT_ONE_FROM_REST_TO_HAND -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) return;
				BattleCard c = removeByInstanceId(st.getCpuRest(), pickedInstanceIds.get(0));
				if (c != null) {
					if ("TANKOFU".equals(pc.getAbilityDeployCode())) {
						c.setBlankEffects(true);
						st.addLog("炭鉱夫: 手札へ（効果なし）");
					} else if ("JOSHU".equals(pc.getAbilityDeployCode())) {
						st.addLog("助手: 手札へ");
					} else if ("ASTORIA".equals(pc.getAbilityDeployCode())) {
						c.setHandDeployCostModifier(-1);
						st.addLog("研究者アストリア: 手札へ（コスト-1）");
					} else {
						st.addLog("レストから手札へ");
					}
					st.getCpuHand().add(0, c);
				}
			}
			case SELECT_SWAP_REST_AND_HAND -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 2) return;
				String a = pickedInstanceIds.get(0);
				String b = pickedInstanceIds.get(1);
				BattleCard restC = removeByInstanceId(st.getCpuRest(), a);
				BattleCard handC = removeByInstanceId(st.getCpuHand(), b);
				if (restC == null || handC == null) {
					if (restC != null) st.getCpuRest().add(restC);
					if (handC != null) st.getCpuHand().add(handC);
					restC = removeByInstanceId(st.getCpuRest(), b);
					handC = removeByInstanceId(st.getCpuHand(), a);
				}
				if (restC != null && handC != null) {
					st.getCpuRest().add(handC);
					st.getCpuHand().add(0, restC);
					st.addLog("レストと手札を交換");
				}
			}
			case SELECT_UP_TO_TWO_FROM_REST_TO_HAND -> {
				if (pickedInstanceIds == null) {
					return;
				}
				if (pickedInstanceIds.size() > 2) {
					return;
				}
				Set<String> uniqGuest = new HashSet<>(pickedInstanceIds);
				if (uniqGuest.size() != pickedInstanceIds.size()) {
					return;
				}
				int ng = 0;
				for (String id : pickedInstanceIds) {
					BattleCard c = removeByInstanceId(st.getCpuRest(), id);
					if (c != null && isFezariaPickableCarbuncleInRest(c, defs)) {
						st.getCpuHand().add(0, c);
						ng++;
					}
				}
				st.addLog("フェザリア: レストから手札へ（" + ng + "枚）");
			}
		}

		st.setPendingChoice(null);
		st.setPhase(st.isHumansTurn() ? BattlePhase.HUMAN_EFFECT_PENDING : BattlePhase.CPU_EFFECT_PENDING);
		st.setLastMessage("効果を処理中…");
	}

	private void beginTurnGainStone(CpuBattleState st, boolean forHuman) {
		if (st == null || st.isGameOver()) {
			return;
		}
		// ターン開始：持ち時間カウント開始（ms）
		st.setTurnStartedAtMs(System.currentTimeMillis());

		// 「次の相手ターン終了まで」系の一時効果は、所有者の次ターン開始時に切れる
		if (forHuman) {
			st.setHumanKoryuBonus(0);
		} else {
			st.setCpuKoryuBonus(0);
		}

		boolean isFirstPlayersFirstTurn = forHuman
				? (st.isHumanGoesFirst() && st.getHumanTurnStarts() == 0)
				: (!st.isHumanGoesFirst() && st.getCpuTurnStarts() == 0);

		if (forHuman) {
			st.setHumanTurnStarts(st.getHumanTurnStarts() + 1);
		} else {
			st.setCpuTurnStarts(st.getCpuTurnStarts() + 1);
		}

		if (isFirstPlayersFirstTurn) {
			st.addLog(forHuman ? "あなたの先攻1ターン目: ストーン獲得なし"
					: opponentActorLogLabel(st) + "の先攻1ターン目: ストーン獲得なし");
			return;
		}

		if (forHuman) {
			st.setHumanStones(st.getHumanStones() + 1);
			st.addLog("ストーンを1つ得た");
		} else {
			st.setCpuStones(st.getCpuStones() + 1);
			st.addLog(opponentActorLogLabel(st) + "はストーンを1つ得た");
		}
	}

	public static int timeLimitSecForStage(int stage) {
		return switch (stage) {
			case 0 -> 90;
			case 1 -> 60;
			case 2 -> 30;
			case 3 -> 15;
			default -> 15;
		};
	}

	private CpuBattleState copyState(CpuBattleState st) {
		CpuBattleState ns = new CpuBattleState();
		ns.setPvp(st.isPvp());
		ns.setCpuLevel(st.getCpuLevel());
		ns.setHumanGoesFirst(st.isHumanGoesFirst());
		ns.setHumansTurn(st.isHumansTurn());
		ns.setHumanTurnStarts(st.getHumanTurnStarts());
		ns.setCpuTurnStarts(st.getCpuTurnStarts());
		ns.setPhase(st.getPhase());
		ns.setPendingEffect(st.getPendingEffect());
		ns.setPendingChoice(st.getPendingChoice());
		ns.setHumanStones(st.getHumanStones());
		ns.setCpuStones(st.getCpuStones());
		ns.setHumanNextDeployBonus(st.getHumanNextDeployBonus());
		ns.setCpuNextDeployBonus(st.getCpuNextDeployBonus());
		ns.setHumanNextElfOnlyBonus(st.getHumanNextElfOnlyBonus());
		ns.setCpuNextElfOnlyBonus(st.getCpuNextElfOnlyBonus());
		ns.setHumanNextDeployCostBonusTimes(st.getHumanNextDeployCostBonusTimes());
		ns.setCpuNextDeployCostBonusTimes(st.getCpuNextDeployCostBonusTimes());
		ns.setHumanNextMechanicStacks(st.getHumanNextMechanicStacks());
		ns.setCpuNextMechanicStacks(st.getCpuNextMechanicStacks());
		ns.setPowerSwapActive(st.isPowerSwapActive());
		ns.setHumanKoryuBonus(st.getHumanKoryuBonus());
		ns.setCpuKoryuBonus(st.getCpuKoryuBonus());
		ns.setLastMessage(st.getLastMessage());
		ns.setGameOver(st.isGameOver());
		ns.setHumanWon(st.isHumanWon());
		ns.setEventLog(new ArrayList<>(st.getEventLog()));

		ns.setHumanDeck(copyCards(st.getHumanDeck()));
		ns.setHumanHand(copyCards(st.getHumanHand()));
		ns.setHumanRest(copyCards(st.getHumanRest()));
		ns.setHumanBattle(copyZone(st.getHumanBattle()));

		ns.setCpuDeck(copyCards(st.getCpuDeck()));
		ns.setCpuHand(copyCards(st.getCpuHand()));
		ns.setCpuRest(copyCards(st.getCpuRest()));
		ns.setCpuBattle(copyZone(st.getCpuBattle()));
		ns.setActiveField(copyCard(st.getActiveField()));
		ns.setActiveFieldOwnerHuman(st.getActiveFieldOwnerHuman());
		return ns;
	}

	/**
	 * 〈フィールド〉を差し替える。以前の場のカードは配置者側のレストへ移す。
	 *
	 * @param newFieldPlacedByHost true=ホスト（{@code humanTurnInteractive}）、false=ゲスト（{@code opponentTurnInteractive}）
	 */
	private void replaceActiveField(CpuBattleState st, BattleCard newField, boolean newFieldPlacedByHost, Map<Short, CardDefinition> defs) {
		BattleCard old = st.getActiveField();
		Boolean prevOwner = st.getActiveFieldOwnerHuman();
		if (old != null && prevOwner != null) {
			CardDefinition oldDef = defs != null ? defs.get(old.getCardId()) : null;
			String oldName = oldDef != null && oldDef.getName() != null ? oldDef.getName() : "？";
			if (Boolean.TRUE.equals(prevOwner)) {
				st.getHumanRest().add(old);
				if (st.isPvp()) {
					st.addLog("〈フィールド〉「" + oldName + "」はホストのレストに置かれた");
				} else {
					st.addLog("〈フィールド〉「" + oldName + "」はあなたのレストに置かれた");
				}
			} else {
				st.getCpuRest().add(old);
				if (st.isPvp()) {
					st.addLog("〈フィールド〉「" + oldName + "」はゲストのレストに置かれた");
				} else {
					st.addLog("〈フィールド〉「" + oldName + "」は相手のレストに置かれた");
				}
			}
		}
		st.setActiveField(newField);
		st.setActiveFieldOwnerHuman(newField == null ? null : Boolean.valueOf(newFieldPlacedByHost));
	}

	public void humanTurn(CpuBattleState st, int levelUpRest, List<String> levelUpDiscardInstanceIds, int levelUpStones, boolean deploy, int deployHandIndex,
			Map<Short, CardDefinition> defs) {
		if (st.isGameOver() || !st.isHumansTurn()) {
			return;
		}
		if (st.getCpuBattle() != null && !canMakeLegalDeploy(st, true, defs)) {
			st.setGameOver(true);
			st.setHumanWon(false);
			st.setLastMessage("敗北（相手以上のファイターを出せません）");
			st.addLog("敗北: 相手以上のファイターを出せない");
			return;
		}
		if (levelUpStones < 0 || levelUpRest < 0 || levelUpStones > st.getHumanStones()) {
			st.setLastMessage("レベルアップ指定が不正です");
			return;
		}
		if (levelUpRest > st.getHumanHand().size()) {
			st.setLastMessage("手札が足りずレベルアップできません");
			return;
		}

		List<String> discIds = levelUpDiscardInstanceIds != null ? levelUpDiscardInstanceIds : List.of();
		long luDistinct = discIds.stream().distinct().count();
		if (luDistinct != discIds.size()) {
			st.setLastMessage("捨てるカード指定が重複しています");
			return;
		}
		if (levelUpRest > 0) {
			if (discIds.size() != levelUpRest) {
				st.setLastMessage("レベルアップで捨てるカードを指定してください");
				return;
			}
		} else if (!discIds.isEmpty()) {
			st.setLastMessage("レベルアップ指定が不正です");
			return;
		}

		List<BattleCard> simHand = new ArrayList<>(st.getHumanHand());
		for (String did : discIds) {
			BattleCard rm = removeByInstanceId(simHand, did);
			if (rm == null) {
				st.setLastMessage("捨てるカードが手札にありません");
				return;
			}
		}
		int deployBonus = 0;
		if (deploy) {
			if (deployHandIndex < 0 || deployHandIndex >= simHand.size()) {
				st.setLastMessage("手札の指定が不正です（レベルアップ後の位置で指定してください）");
				return;
			}
			BattleCard main = simHand.get(deployHandIndex);
			CardDefinition mainDef = defs.get(main.getCardId());
			if (mainDef != null && isFieldCard(mainDef)) {
				st.setLastMessage("フィールドはバトル画面の配置操作でのみ出せます");
				return;
			}
			deployBonus = levelUpRest * 2 + levelUpStones * 2;
			deployBonus += st.getHumanNextDeployBonus();
			if (st.getHumanNextElfOnlyBonus() > 0 && CardAttributes.hasAttribute(mainDef, "ELF")) {
				deployBonus += st.getHumanNextElfOnlyBonus();
			}
			if (st.getHumanNextDeployCostBonusTimes() > 0) {
				deployBonus += mainDef.getCost() * st.getHumanNextDeployCostBonusTimes();
			}
			deployBonus += 3 * st.getHumanNextMechanicStacks();
			if (!canDeployWithHand(simHand, deployHandIndex, defs, deployBonus, st, true)) {
				st.setLastMessage("配置条件（強さ・コスト）を満たせません");
				return;
			}
		}

		st.setHumanStones(st.getHumanStones() - levelUpStones);
		if (levelUpRest > 0 || levelUpStones > 0) {
			StringBuilder b = new StringBuilder("レベルアップ: ");
			if (levelUpRest > 0) b.append("カード").append(levelUpRest).append("枚");
			if (levelUpRest > 0 && levelUpStones > 0) b.append(" + ");
			if (levelUpStones > 0) b.append("ストーン").append(levelUpStones).append("個");
			st.addLog(b.toString());
		}
		for (String did : discIds) {
			BattleCard c = removeByInstanceId(st.getHumanHand(), did);
			if (c == null) {
				st.setLastMessage("捨てるカードが手札にありません");
				return;
			}
			st.getHumanRest().add(c);
		}

		if (deploy) {
			BattleCard main = st.getHumanHand().remove(deployHandIndex);
			CardDefinition mainDef = defs.get(main.getCardId());
			int cost = effectiveDeployCost(mainDef, main, defs, st.getHumanRest(), st.getHumanNextMechanicStacks());
			List<BattleCard> paid = new ArrayList<>();
			for (int i = 0; i < cost; i++) {
				paid.add(st.getHumanHand().remove(st.getHumanHand().size() - 1));
			}
			ZoneFighter z = new ZoneFighter();
			z.setMain(main);
			z.setCostUnder(paid);
			z.setCostPayCardCount(cost);
			z.setTemporaryPowerBonus(deployBonus);
			// 次回配置ボーナス消費
			st.setHumanNextDeployBonus(0);
			st.setHumanNextElfOnlyBonus(0);
			st.setHumanNextDeployCostBonusTimes(0);
			st.setHumanNextMechanicStacks(0);
			retireOwnBattleZoneBeforeNewDeploy(st, true, true);
			st.setHumanBattle(z);
			applyFieldNebulaWhenCarbuncleFighterDeployed(st, mainDef, true);
			applyDeployHuman(st, mainDef, defs, main);
			st.addLog("あなたは「" + mainDef.getName() + "」を配置した");

			// 配置効果まで反映した上で、強さ条件を満たせない場合は敗北（配置前の素の強さで弾かない）
			if (st.getCpuBattle() != null) {
				int me = effectiveBattlePower(st.getHumanBattle(), true, st, defs);
				int opp = effectiveBattlePower(st.getCpuBattle(), false, st, defs);
				if (me < opp) {
					st.setGameOver(true);
					st.setHumanWon(false);
					st.setPhase(BattlePhase.GAME_OVER);
					st.setLastMessage("敗北（能力後も相手以上になれません）");
					st.addLog("敗北: 能力後も強さ条件を満たせない");
					return;
				}
			}
		} else {
			st.addLog("あなたは配置をスキップした");
		}

		if (st.isGameOver()) {
			return;
		}

		resolveKnockAndDraw(st, true, defs);
		resetTurnBuffs(st);
		st.setHumansTurn(false);
		// CPUのターン開始：ストーン付与（先攻1ターン目のみ獲得なし）
		beginTurnGainStone(st, false);
		st.setLastMessage(st.isPvp() ? "ゲストのターン" : "CPUのターン");
	}

	/**
	 * クリックUI向け: 手札の instanceId で配置カード・支払いカードを指定し、配置コストは「カード/ストーン/分割」で支払える。
	 * levelUpRest はレストへ捨てる枚数（捨てるカードは levelUpDiscardInstanceIds で指定）、levelUpStones は強化回数（1回=+2、ストーン1消費）。
	 */
	public void humanTurnInteractive(CpuBattleState st, int levelUpRest, List<String> levelUpDiscardInstanceIds, int levelUpStones,
			String deployInstanceId, int payCostStones, List<String> payCostCardInstanceIds,
			Map<Short, CardDefinition> defs) {
		if (st == null || st.isGameOver() || !st.isHumansTurn() || st.getPhase() != BattlePhase.HUMAN_INPUT) {
			return;
		}
		if (st.getCpuBattle() != null && !canMakeLegalDeploy(st, true, defs)) {
			st.setGameOver(true);
			st.setHumanWon(false);
			st.setLastMessage("敗北（相手以上のファイターを出せません）");
			st.addLog("敗北: 相手以上のファイターを出せない");
			return;
		}
		if (levelUpRest < 0 || levelUpStones < 0) {
			st.setLastMessage("指定が不正です");
			return;
		}
		if (levelUpRest > st.getHumanHand().size()) {
			st.setLastMessage("手札が足りずレベルアップできません");
			return;
		}
		if (levelUpStones > st.getHumanStones()) {
			st.setLastMessage("ストーンが足りません");
			return;
		}

		int stonesAfterLevel = st.getHumanStones() - levelUpStones;
		if (payCostStones < 0 || payCostStones > stonesAfterLevel) {
			st.setLastMessage("コスト支払いストーンが不正です");
			return;
		}

		List<String> discIds = levelUpDiscardInstanceIds != null ? levelUpDiscardInstanceIds : List.of();
		long distinct = discIds.stream().distinct().count();
		if (distinct != discIds.size()) {
			st.setLastMessage("捨てるカード指定が重複しています");
			return;
		}
		if (levelUpRest > 0) {
			if (discIds.size() != levelUpRest) {
				st.setLastMessage("レベルアップで捨てるカードを指定してください");
				return;
			}
		} else if (!discIds.isEmpty()) {
			st.setLastMessage("レベルアップ指定が不正です");
			return;
		}

		// シミュレーション（レベルアップ後の手札）
		List<BattleCard> simHand = new ArrayList<>(st.getHumanHand());
		for (String did : discIds) {
			BattleCard c = removeByInstanceId(simHand, did);
			if (c == null) {
				st.setLastMessage("捨てるカードが手札にありません");
				return;
			}
		}

		int deployBonus = 0;
		BattleCard simMain = null;
		CardDefinition mainDef = null;
		int cost = 0;
		if (deployInstanceId != null && !deployInstanceId.isBlank()) {
			for (BattleCard c : simHand) {
				if (deployInstanceId.equals(c.getInstanceId())) {
					simMain = c;
					break;
				}
			}
			if (simMain == null) {
				st.setLastMessage("配置カードが見つかりません");
				return;
			}
			if (!discIds.isEmpty() && discIds.contains(simMain.getInstanceId())) {
				st.setLastMessage("配置カードを捨てることはできません");
				return;
			}
			mainDef = defs.get(simMain.getCardId());
			if (mainDef == null) {
				st.setLastMessage("カード定義が見つかりません");
				return;
			}
			cost = effectiveDeployCost(mainDef, simMain, defs, st.getHumanRest(), st.getHumanNextMechanicStacks());

			if (isFieldCard(mainDef)) {
				if (levelUpRest != 0 || levelUpStones != 0) {
					st.setLastMessage("フィールドはレベルアップと同時に出せません");
					return;
				}
				List<String> payIdsField = payCostCardInstanceIds != null ? payCostCardInstanceIds : List.of();
				if (!payIdsField.isEmpty()) {
					st.setLastMessage("フィールドのコストはストーンのみです");
					return;
				}
				if (payCostStones != cost) {
					st.setLastMessage("ストーンの数がコストと一致しません");
					return;
				}
				simHand.remove(simMain);
			} else {
				deployBonus = levelUpRest * 2 + levelUpStones * 2;
				deployBonus += st.getHumanNextDeployBonus();
				if (st.getHumanNextElfOnlyBonus() > 0 && CardAttributes.hasAttribute(mainDef, "ELF")) {
					deployBonus += st.getHumanNextElfOnlyBonus();
				}
				if (st.getHumanNextDeployCostBonusTimes() > 0) {
					deployBonus += (mainDef.getCost() != null ? mainDef.getCost() : 0) * st.getHumanNextDeployCostBonusTimes();
				}
				deployBonus += 3 * st.getHumanNextMechanicStacks();
				simHand.remove(simMain);

				// 支払いチェック
				List<String> payIds = payCostCardInstanceIds != null ? payCostCardInstanceIds : List.of();
				long payDistinct = payIds.stream().distinct().count();
				if (payDistinct != payIds.size()) {
					st.setLastMessage("支払いカードが重複しています");
					return;
				}
				if (payIds.size() + payCostStones != cost) {
					st.setLastMessage("コスト支払いが揃っていません");
					return;
				}
				for (String pid : payIds) {
					boolean ok = false;
					for (BattleCard c : simHand) {
						if (pid != null && pid.equals(c.getInstanceId())) {
							ok = true;
							break;
						}
					}
					if (!ok) {
						st.setLastMessage("支払いカードが手札にありません");
						return;
					}
				}

				// 支払いカードを除外（順序は問わない）
				for (String pid : payIds) {
					for (int i = 0; i < simHand.size(); i++) {
						if (pid.equals(simHand.get(i).getInstanceId())) {
							simHand.remove(i);
							break;
						}
					}
				}

				// 強さ条件は「配置効果・常時効果」反映後に確定判定する（配置前の素の強さでは弾かない）
			}
		}

		// 「能力後に相手以上になれない」確認でキャンセルした場合は、レベルアップ消費も含めて元に戻す必要がある。
		// そのため、消費を確定適用する前（この時点）でスナップショットを取っておく。
		if (simMain != null && mainDef != null && !isFieldCard(mainDef)) {
			st.setConfirmAcceptLossSnapshot(copyState(st));
		} else {
			st.setConfirmAcceptLossSnapshot(null);
		}

		// ここから確定適用
		st.setHumanStones(st.getHumanStones() - levelUpStones);
		List<BattleCard> levelUpCards = new ArrayList<>();
		for (String did : discIds) {
			BattleCard c = removeByInstanceId(st.getHumanHand(), did);
			if (c == null) {
				st.setLastMessage("捨てるカードが手札にありません");
				return;
			}
			levelUpCards.add(c);
		}

		if (!levelUpCards.isEmpty() || levelUpStones > 0) {
			StringBuilder b = new StringBuilder("レベルアップ: ");
			if (!levelUpCards.isEmpty()) b.append("カード").append(levelUpCards.size()).append("枚");
			if (!levelUpCards.isEmpty() && levelUpStones > 0) b.append(" + ");
			if (levelUpStones > 0) b.append("ストーン").append(levelUpStones).append("個");
			st.addLog(b.toString());
		}

		if (simMain != null && mainDef != null) {
			if (isFieldCard(mainDef)) {
				BattleCard main = removeByInstanceId(st.getHumanHand(), deployInstanceId);
				if (main == null) {
					st.setLastMessage("配置カードが見つかりません");
					return;
				}
				st.setHumanStones(st.getHumanStones() - payCostStones);
				replaceActiveField(st, main, true, defs);
				st.addLog("あなたは「" + mainDef.getName() + "」を〈場〉に置いた");
				st.setLastMessage("《フィールド》を配置しました（ターンは続きます）");
				return;
			}
			// 配置カードを実手札から取り出す
			BattleCard main = removeByInstanceId(st.getHumanHand(), deployInstanceId);
			if (main == null) {
				st.setLastMessage("配置カードが見つかりません");
				return;
			}

			// コスト支払い（ストーン）
			st.setHumanStones(st.getHumanStones() - payCostStones);

			// コスト支払い（カード）
			List<BattleCard> paid = new ArrayList<>();
			List<String> payIds = payCostCardInstanceIds != null ? payCostCardInstanceIds : List.of();
			for (String pid : payIds) {
				BattleCard p = removeByInstanceId(st.getHumanHand(), pid);
				if (p == null) {
					st.setLastMessage("支払いカードが見つかりません");
					return;
				}
				paid.add(p);
			}
			// レベルアップで使用したカードは、配置カードの下に重ねる（レストへは行かない）
			paid.addAll(levelUpCards);

			ZoneFighter z = new ZoneFighter();
			z.setMain(main);
			z.setCostUnder(paid);
			z.setCostPayCardCount(payIds.size());
			z.setTemporaryPowerBonus(deployBonus);
			st.setHumanNextDeployBonus(0);
			st.setHumanNextElfOnlyBonus(0);
			st.setHumanNextDeployCostBonusTimes(0);
			st.setHumanNextMechanicStacks(0);
			retireOwnBattleZoneBeforeNewDeploy(st, true, true);
			st.setHumanBattle(z);
			applyFieldNebulaWhenCarbuncleFighterDeployed(st, mainDef, true);
			st.addLog("あなたは「" + mainDef.getName() + "」を配置した");
			stagePendingDeployEffect(st, true, mainDef, z);
			return;
		} else {
			st.setConfirmAcceptLossSnapshot(null);
			st.addLog("あなたは配置をスキップした");
			// 配置しない場合、レベルアップで使ったカードはレストへ
			st.getHumanRest().addAll(levelUpCards);
			resolveKnockAndDraw(st, true, defs);
			resetTurnBuffs(st);
			st.setHumansTurn(false);
			st.setPhase(BattlePhase.CPU_THINKING);
			// CPUのターン開始：ストーン付与（先攻1ターン目のみ獲得なし）
			beginTurnGainStone(st, false);
		}
		st.setLastMessage(st.isPvp() ? "ゲストのターン" : "CPUのターン");
	}

	/**
	 * 対人戦: ゲスト（cpu スロット）のメインターン。{@link #humanTurnInteractive} と対称。
	 */
	public void opponentTurnInteractive(CpuBattleState st, int levelUpRest, List<String> levelUpDiscardInstanceIds, int levelUpStones,
			String deployInstanceId, int payCostStones, List<String> payCostCardInstanceIds,
			Map<Short, CardDefinition> defs) {
		if (st == null || st.isGameOver() || st.isHumansTurn() || st.getPhase() != BattlePhase.CPU_THINKING) {
			return;
		}
		if (st.getHumanBattle() != null && !canMakeLegalDeploy(st, false, defs)) {
			st.setGameOver(true);
			st.setHumanWon(true);
			st.setLastMessage("敗北（相手以上のファイターを出せません）");
			st.addLog("敗北: 相手以上のファイターを出せない");
			return;
		}
		if (levelUpRest < 0 || levelUpStones < 0) {
			st.setLastMessage("指定が不正です");
			return;
		}
		if (levelUpRest > st.getCpuHand().size()) {
			st.setLastMessage("手札が足りずレベルアップできません");
			return;
		}
		if (levelUpStones > st.getCpuStones()) {
			st.setLastMessage("ストーンが足りません");
			return;
		}

		final boolean guestIsFirstPlayer = !st.isHumanGoesFirst();
		final boolean guestIsFirstTurnAsFirstPlayer = guestIsFirstPlayer && st.getCpuTurnStarts() == 1;
		if (guestIsFirstTurnAsFirstPlayer && (levelUpRest > 0 || levelUpStones > 0)) {
			st.setLastMessage("先攻1ターン目はレベルアップできません");
			return;
		}

		int stonesAfterLevel = st.getCpuStones() - levelUpStones;
		if (payCostStones < 0 || payCostStones > stonesAfterLevel) {
			st.setLastMessage("コスト支払いストーンが不正です");
			return;
		}

		List<String> discIds = levelUpDiscardInstanceIds != null ? levelUpDiscardInstanceIds : List.of();
		long distinct = discIds.stream().distinct().count();
		if (distinct != discIds.size()) {
			st.setLastMessage("捨てるカード指定が重複しています");
			return;
		}
		if (levelUpRest > 0) {
			if (discIds.size() != levelUpRest) {
				st.setLastMessage("レベルアップで捨てるカードを指定してください");
				return;
			}
		} else if (!discIds.isEmpty()) {
			st.setLastMessage("レベルアップ指定が不正です");
			return;
		}

		List<BattleCard> simHand = new ArrayList<>(st.getCpuHand());
		for (String did : discIds) {
			BattleCard c = removeByInstanceId(simHand, did);
			if (c == null) {
				st.setLastMessage("捨てるカードが手札にありません");
				return;
			}
		}

		int deployBonus = 0;
		BattleCard simMain = null;
		CardDefinition mainDef = null;
		int cost = 0;
		if (deployInstanceId != null && !deployInstanceId.isBlank()) {
			for (BattleCard c : simHand) {
				if (deployInstanceId.equals(c.getInstanceId())) {
					simMain = c;
					break;
				}
			}
			if (simMain == null) {
				st.setLastMessage("配置カードが見つかりません");
				return;
			}
			if (!discIds.isEmpty() && discIds.contains(simMain.getInstanceId())) {
				st.setLastMessage("配置カードを捨てることはできません");
				return;
			}
			mainDef = defs.get(simMain.getCardId());
			if (mainDef == null) {
				st.setLastMessage("カード定義が見つかりません");
				return;
			}
			cost = effectiveDeployCost(mainDef, simMain, defs, st.getCpuRest(), st.getCpuNextMechanicStacks());

			if (isFieldCard(mainDef)) {
				if (levelUpRest != 0 || levelUpStones != 0) {
					st.setLastMessage("フィールドはレベルアップと同時に出せません");
					return;
				}
				List<String> payIdsField = payCostCardInstanceIds != null ? payCostCardInstanceIds : List.of();
				if (!payIdsField.isEmpty()) {
					st.setLastMessage("フィールドのコストはストーンのみです");
					return;
				}
				if (payCostStones != cost) {
					st.setLastMessage("ストーンの数がコストと一致しません");
					return;
				}
				simHand.remove(simMain);
			} else {
				deployBonus = levelUpRest * 2 + levelUpStones * 2;
				deployBonus += st.getCpuNextDeployBonus();
				if (st.getCpuNextElfOnlyBonus() > 0 && CardAttributes.hasAttribute(mainDef, "ELF")) {
					deployBonus += st.getCpuNextElfOnlyBonus();
				}
				if (st.getCpuNextDeployCostBonusTimes() > 0) {
					deployBonus += (mainDef.getCost() != null ? mainDef.getCost() : 0) * st.getCpuNextDeployCostBonusTimes();
				}
				deployBonus += 3 * st.getCpuNextMechanicStacks();
				simHand.remove(simMain);

				List<String> payIds = payCostCardInstanceIds != null ? payCostCardInstanceIds : List.of();
				long payDistinct = payIds.stream().distinct().count();
				if (payDistinct != payIds.size()) {
					st.setLastMessage("支払いカードが重複しています");
					return;
				}
				if (payIds.size() + payCostStones != cost) {
					st.setLastMessage("コスト支払いが揃っていません");
					return;
				}
				for (String pid : payIds) {
					boolean ok = false;
					for (BattleCard c : simHand) {
						if (pid != null && pid.equals(c.getInstanceId())) {
							ok = true;
							break;
						}
					}
					if (!ok) {
						st.setLastMessage("支払いカードが手札にありません");
						return;
					}
				}
				for (String pid : payIds) {
					for (int i = 0; i < simHand.size(); i++) {
						if (pid.equals(simHand.get(i).getInstanceId())) {
							simHand.remove(i);
							break;
						}
					}
				}
			}
		}

		if (simMain != null && mainDef != null && !isFieldCard(mainDef)) {
			st.setConfirmAcceptLossSnapshot(copyState(st));
		} else {
			st.setConfirmAcceptLossSnapshot(null);
		}

		st.setCpuStones(st.getCpuStones() - levelUpStones);
		List<BattleCard> levelUpCards = new ArrayList<>();
		for (String did : discIds) {
			BattleCard c = removeByInstanceId(st.getCpuHand(), did);
			if (c == null) {
				st.setLastMessage("捨てるカードが手札にありません");
				return;
			}
			levelUpCards.add(c);
		}

		if (!levelUpCards.isEmpty() || levelUpStones > 0) {
			StringBuilder b = new StringBuilder("レベルアップ: ");
			if (!levelUpCards.isEmpty()) b.append("カード").append(levelUpCards.size()).append("枚");
			if (!levelUpCards.isEmpty() && levelUpStones > 0) b.append(" + ");
			if (levelUpStones > 0) b.append("ストーン").append(levelUpStones).append("個");
			st.addLog(b.toString());
		}

		if (simMain != null && mainDef != null) {
			if (isFieldCard(mainDef)) {
				BattleCard main = removeByInstanceId(st.getCpuHand(), deployInstanceId);
				if (main == null) {
					st.setLastMessage("配置カードが見つかりません");
					return;
				}
				st.setCpuStones(st.getCpuStones() - payCostStones);
				replaceActiveField(st, main, false, defs);
				st.addLog("相手は「" + mainDef.getName() + "」を〈場〉に置いた");
				st.setLastMessage("《フィールド》を配置しました（ターンは続きます）");
				return;
			}
			BattleCard main = removeByInstanceId(st.getCpuHand(), deployInstanceId);
			if (main == null) {
				st.setLastMessage("配置カードが見つかりません");
				return;
			}

			st.setCpuStones(st.getCpuStones() - payCostStones);

			List<BattleCard> paid = new ArrayList<>();
			List<String> payIds = payCostCardInstanceIds != null ? payCostCardInstanceIds : List.of();
			for (String pid : payIds) {
				BattleCard p = removeByInstanceId(st.getCpuHand(), pid);
				if (p == null) {
					st.setLastMessage("支払いカードが見つかりません");
					return;
				}
				paid.add(p);
			}
			paid.addAll(levelUpCards);

			ZoneFighter z = new ZoneFighter();
			z.setMain(main);
			z.setCostUnder(paid);
			z.setCostPayCardCount(payIds.size());
			z.setTemporaryPowerBonus(deployBonus);
			st.setCpuNextDeployBonus(0);
			st.setCpuNextElfOnlyBonus(0);
			st.setCpuNextDeployCostBonusTimes(0);
			st.setCpuNextMechanicStacks(0);
			retireOwnBattleZoneBeforeNewDeploy(st, false, true);
			st.setCpuBattle(z);
			applyFieldNebulaWhenCarbuncleFighterDeployed(st, mainDef, false);
			st.addLog("相手は「" + mainDef.getName() + "」を配置した");
			stagePendingDeployEffect(st, false, mainDef, z);
			return;
		} else {
			st.setConfirmAcceptLossSnapshot(null);
			st.addLog("相手は配置をスキップした");
			st.getCpuRest().addAll(levelUpCards);
			resolveKnockAndDraw(st, false, defs);
			resetTurnBuffs(st);
			st.setHumansTurn(true);
			st.setPhase(BattlePhase.HUMAN_INPUT);
			beginTurnGainStone(st, true);
		}
		st.setLastMessage(st.isPvp() ? "ホストのターン" : "あなたのターン");
	}

	/** CPU戦: CPU 手番を「配置スキップ」で強制終了する（時間切れ用）。 */
	public void forceCpuSkipTurnDueToTimeout(CpuBattleState st, Map<Short, CardDefinition> defs) {
		if (st == null || st.isGameOver() || st.isHumansTurn() || st.getPhase() != BattlePhase.CPU_THINKING) {
			return;
		}
		st.addLog(opponentActorLogLabel(st) + "は時間切れで配置をスキップした");
		resolveKnockAndDraw(st, false, defs);
		resetTurnBuffs(st);
		st.setHumansTurn(true);
		st.setPhase(BattlePhase.HUMAN_INPUT);
		beginTurnGainStone(st, true);
		st.setLastMessage(st.isPvp() ? "ホストのターン" : "あなたのターン");
	}

	private static BattleCard removeByInstanceId(List<BattleCard> list, String instanceId) {
		if (instanceId == null) return null;
		for (int i = 0; i < list.size(); i++) {
			if (instanceId.equals(list.get(i).getInstanceId())) {
				return list.remove(i);
			}
		}
		return null;
	}

	private boolean canDeployWithHand(List<BattleCard> hand, int handIndex, Map<Short, CardDefinition> defs,
			int deployBonus, CpuBattleState st, boolean human) {
		BattleCard main = hand.get(handIndex);
		CardDefinition d = defs.get(main.getCardId());
		List<BattleCard> rest = human ? st.getHumanRest() : st.getCpuRest();
		int mech = human ? st.getHumanNextMechanicStacks() : st.getCpuNextMechanicStacks();
		int cost = effectiveDeployCost(d, main, defs, rest, mech);
		if (hand.size() - 1 < cost) {
			return false;
		}
		return true;
	}

	/**
	 * CPU の配置コスト支払い: ストーンをできるだけ使い、足りない分を手札から払う（CPU の自動処理）。
	 * @return カードで払う枚数。支払不可のときは -1
	 */
	private int cpuDeployPayCardCount(int cost, int stonesAvailable, int handSizeIncludingMain) {
		if (cost < 0) return -1;
		int payStones = Math.min(cost, Math.max(0, stonesAvailable));
		int payCards = cost - payStones;
		if (handSizeIncludingMain - 1 < payCards) {
			return -1;
		}
		return payCards;
	}

	private BattleCard copyCard(BattleCard c) {
		if (c == null) return null;
		BattleCard n = new BattleCard(c.getInstanceId(), c.getCardId(), c.isBlankEffects());
		n.setHandDeployCostModifier(c.getHandDeployCostModifier());
		return n;
	}

	private List<BattleCard> copyCards(List<BattleCard> src) {
		List<BattleCard> out = new ArrayList<>();
		if (src == null) return out;
		for (BattleCard c : src) {
			out.add(copyCard(c));
		}
		return out;
	}

	private ZoneFighter copyZone(ZoneFighter z) {
		if (z == null) return null;
		ZoneFighter nz = new ZoneFighter();
		nz.setMain(copyCard(z.getMain()));
		List<BattleCard> under = new ArrayList<>();
		if (z.getCostUnder() != null) {
			for (BattleCard c : z.getCostUnder()) {
				under.add(copyCard(c));
			}
		}
		nz.setCostUnder(under);
		nz.setTemporaryPowerBonus(z.getTemporaryPowerBonus());
		nz.setCostPayCardCount(z.getCostPayCardCount());
		nz.setReturnToHandOnKnock(z.isReturnToHandOnKnock());
		nz.setFieldNebulaStoneGrantedForThisDeploy(z.isFieldNebulaStoneGrantedForThisDeploy());
		return nz;
	}

	private CpuBattleState copyStateForCpuSim(CpuBattleState st) {
		CpuBattleState ns = new CpuBattleState();
		ns.setPvp(st.isPvp());
		ns.setCpuLevel(st.getCpuLevel());
		ns.setHumanGoesFirst(st.isHumanGoesFirst());
		ns.setHumansTurn(st.isHumansTurn());
		ns.setHumanTurnStarts(st.getHumanTurnStarts());
		ns.setHumanStones(st.getHumanStones());
		ns.setCpuStones(st.getCpuStones());
		ns.setHumanNextDeployBonus(st.getHumanNextDeployBonus());
		ns.setCpuNextDeployBonus(st.getCpuNextDeployBonus());
		ns.setHumanNextElfOnlyBonus(st.getHumanNextElfOnlyBonus());
		ns.setCpuNextElfOnlyBonus(st.getCpuNextElfOnlyBonus());
		ns.setHumanNextDeployCostBonusTimes(st.getHumanNextDeployCostBonusTimes());
		ns.setCpuNextDeployCostBonusTimes(st.getCpuNextDeployCostBonusTimes());
		ns.setHumanNextMechanicStacks(st.getHumanNextMechanicStacks());
		ns.setCpuNextMechanicStacks(st.getCpuNextMechanicStacks());
		ns.setLastMessage(st.getLastMessage());
		ns.setGameOver(st.isGameOver());
		ns.setHumanWon(st.isHumanWon());
		ns.setEventLog(new ArrayList<>(st.getEventLog()));

		ns.setHumanDeck(copyCards(st.getHumanDeck()));
		ns.setHumanHand(copyCards(st.getHumanHand()));
		ns.setHumanRest(copyCards(st.getHumanRest()));
		ns.setHumanBattle(copyZone(st.getHumanBattle()));

		ns.setCpuDeck(copyCards(st.getCpuDeck()));
		ns.setCpuHand(copyCards(st.getCpuHand()));
		ns.setCpuRest(copyCards(st.getCpuRest()));
		ns.setCpuBattle(copyZone(st.getCpuBattle()));
		ns.setActiveField(copyCard(st.getActiveField()));
		ns.setActiveFieldOwnerHuman(st.getActiveFieldOwnerHuman());
		return ns;
	}

	public void cpuTurn(CpuBattleState st, Map<Short, CardDefinition> defs, Random rnd) {
		if (st.isGameOver() || st.isHumansTurn() || st.getPhase() != BattlePhase.CPU_THINKING) {
			return;
		}
		// CPU先攻の初手はレベルアップを使わない（カード捨て・ストーン使用ともに禁止）
		// beginTurnGainStone 内で turnStarts がインクリメントされるため、初手は cpuTurnStarts == 1
		final boolean cpuIsFirstPlayer = !st.isHumanGoesFirst();
		final boolean cpuIsFirstTurnAsFirstPlayer = cpuIsFirstPlayer && st.getCpuTurnStarts() == 1;
		if (st.getHumanBattle() != null && !canMakeLegalDeploy(st, false, defs)) {
			st.setGameOver(true);
			st.setHumanWon(true);
			st.setLastMessage("勝利（CPUが相手以上のファイターを出せません）");
			st.addLog("勝利: CPUが相手以上のファイターを出せない");
			return;
		}

		// CPU は手札を吟味し、配置効果・常時効果・複数回レベルアップを考慮して
		// 相手バトルゾーン以上になれるなら必ず配置する。
		String bestInstanceId = null;
		int bestLevelUpRest = 0;
		int bestLevelUpStones = 0;
		int bestDeployBonus = 0;
		List<String> bestLevelUpDiscardIds = List.of();
		boolean hasOpp = st.getHumanBattle() != null;
		int bestScore = Integer.MIN_VALUE; // 相手がいないとき用
		int bestCpuEff = hasOpp ? Integer.MAX_VALUE : -1;
		int bestResource = Integer.MAX_VALUE;

		int maxRest = st.getCpuHand().size();
		int maxStones = Math.max(0, st.getCpuStones());
		if (cpuIsFirstTurnAsFirstPlayer) {
			maxRest = 0;
			maxStones = 0;
		}

		for (int levelUpRest = 0; levelUpRest <= maxRest; levelUpRest++) {
			List<List<String>> discardPlans = cpuDiscardPlans(st.getCpuHand(), levelUpRest);
			for (int levelUpStones = 0; levelUpStones <= maxStones; levelUpStones++) {
				for (BattleCard main : st.getCpuHand()) {
					CardDefinition mainDef = defs.get(main.getCardId());
					if (mainDef == null) continue;
					if (isFieldCard(mainDef)) continue;

					for (List<String> discIds : discardPlans) {
						if (discIds.contains(main.getInstanceId())) {
							continue; // 配置カードをレベルアップで捨てることはできない
						}

						int deployBonus = levelUpRest * 2 + levelUpStones * 2;
						deployBonus += st.getCpuNextDeployBonus();
						if (st.getCpuNextElfOnlyBonus() > 0 && CardAttributes.hasAttribute(mainDef, "ELF")) {
							deployBonus += st.getCpuNextElfOnlyBonus();
						}
						if (st.getCpuNextDeployCostBonusTimes() > 0) {
							deployBonus += mainDef.getCost() * st.getCpuNextDeployCostBonusTimes();
						}
						deployBonus += 3 * st.getCpuNextMechanicStacks();

						// シミュレーション：レベルアップ→配置→配置効果→常時計算（effectiveBattlePower）
						CpuBattleState simSt = copyStateForCpuSim(st);
						simSt.setHumansTurn(false);
						simSt.setGameOver(false);

						if (levelUpStones > simSt.getCpuStones()) continue;
						simSt.setCpuStones(simSt.getCpuStones() - levelUpStones);

						// レベルアップ捨て（CPUが選ぶ）
						for (String did : discIds) {
							BattleCard dc = removeByInstanceId(simSt.getCpuHand(), did);
							if (dc != null) {
								simSt.getCpuRest().add(dc);
							}
						}

						int cost = effectiveDeployCost(mainDef, main, defs, st.getCpuRest(), st.getCpuNextMechanicStacks());
						int payCards = cpuDeployPayCardCount(cost, simSt.getCpuStones(), simSt.getCpuHand().size());
						if (payCards < 0) {
							continue;
						}
						int payStones = cost - payCards;
						simSt.setCpuStones(simSt.getCpuStones() - payStones);

						// 配置カードを取り出す
						BattleCard simMain = removeByInstanceId(simSt.getCpuHand(), main.getInstanceId());
						if (simMain == null) continue;
						List<BattleCard> paid = new ArrayList<>();
						for (int i = 0; i < payCards; i++) {
							if (simSt.getCpuHand().isEmpty()) break;
							paid.add(simSt.getCpuHand().remove(simSt.getCpuHand().size() - 1));
						}
						ZoneFighter z = new ZoneFighter();
						z.setMain(simMain);
						z.setCostUnder(paid);
						z.setCostPayCardCount(payCards);
						z.setTemporaryPowerBonus(deployBonus);
						retireOwnBattleZoneBeforeNewDeploy(simSt, false, false);
						simSt.setCpuBattle(z);

						Random simRnd = new Random(31_337L ^ main.getCardId() ^ (levelUpRest * 31L) ^ (levelUpStones * 131L));
						applyDeployCpu(simSt, mainDef, defs, simRnd, simMain);

						int cpuEff = effectiveBattlePower(simSt.getCpuBattle(), false, simSt, defs);
						int oppEff = effectiveBattlePower(simSt.getHumanBattle(), true, simSt, defs);
						if (simSt.getHumanBattle() != null && cpuEff < oppEff) {
							continue;
						}

						if (hasOpp) {
							// 相手をレストにできる中で、
							// 1) まず「レベルアップ消費（捨て札/ストーン）」を最小化
							// 2) 次にレベルアップで「できるだけストーンから消費」
							// （配置コストは別途、ストーン優先・不足分は手札＝分割払い）
							// 3) 最後に「必要最低値の強さ（cpuEff）」を選ぶ
							int resource = levelUpRest + levelUpStones;
							if (resource < bestResource
									|| (resource == bestResource && (
											(levelUpStones > bestLevelUpStones)
											|| (levelUpStones == bestLevelUpStones && levelUpRest < bestLevelUpRest)
											|| (levelUpStones == bestLevelUpStones && levelUpRest == bestLevelUpRest && cpuEff < bestCpuEff)
									))) {
								bestCpuEff = cpuEff;
								bestResource = resource;
								bestInstanceId = main.getInstanceId();
								bestLevelUpRest = levelUpRest;
								bestLevelUpStones = levelUpStones;
								bestDeployBonus = deployBonus;
								bestLevelUpDiscardIds = discIds;
							}
						} else {
							// 相手がいないときは従来通り「強くなる」配置を優先
							int score = cpuEff - oppEff;
							int resource = levelUpRest + levelUpStones;
							if (resource < bestResource
									|| (resource == bestResource && (
											score > bestScore
											|| (score == bestScore && cpuEff > bestCpuEff)
											|| (score == bestScore && cpuEff == bestCpuEff && levelUpStones > bestLevelUpStones)
											|| (score == bestScore && cpuEff == bestCpuEff && levelUpStones == bestLevelUpStones && levelUpRest < bestLevelUpRest)
									))) {
								bestResource = resource;
								bestScore = score;
								bestCpuEff = cpuEff;
								bestInstanceId = main.getInstanceId();
								bestLevelUpRest = levelUpRest;
								bestLevelUpStones = levelUpStones;
								bestDeployBonus = deployBonus;
								bestLevelUpDiscardIds = discIds;
							}
						}
					}
				}
			}
		}

		boolean deployed = false;
		if (bestInstanceId != null) {
			// レベルアップ確定（CPUが選んだカードをレストへ）
			if (bestLevelUpStones <= st.getCpuStones()) {
				st.setCpuStones(st.getCpuStones() - bestLevelUpStones);
			}
			List<BattleCard> levelUpCards = new ArrayList<>();
			for (String did : bestLevelUpDiscardIds) {
				BattleCard dc = removeByInstanceId(st.getCpuHand(), did);
				if (dc != null) {
					levelUpCards.add(dc);
				}
			}
			if (!levelUpCards.isEmpty() || bestLevelUpStones > 0) {
				StringBuilder b = new StringBuilder("CPUレベルアップ: ");
				if (!levelUpCards.isEmpty()) b.append("カード").append(levelUpCards.size()).append("枚");
				if (!levelUpCards.isEmpty() && bestLevelUpStones > 0) b.append(" + ");
				if (bestLevelUpStones > 0) b.append("ストーン").append(bestLevelUpStones).append("個");
				st.addLog(b.toString());
			}

			CardDefinition bestDef = null;
			BattleCard deployCard = null;
			for (BattleCard c : st.getCpuHand()) {
				if (bestInstanceId.equals(c.getInstanceId())) {
					bestDef = defs.get(c.getCardId());
					deployCard = c;
					break;
				}
			}
			if (bestDef != null && deployCard != null) {
				int cost = effectiveDeployCost(bestDef, deployCard, defs, st.getCpuRest(), st.getCpuNextMechanicStacks());
				int payCards = cpuDeployPayCardCount(cost, st.getCpuStones(), st.getCpuHand().size());
				if (payCards >= 0) {
					int payCostStones = cost - payCards;
					BattleCard main = removeByInstanceId(st.getCpuHand(), bestInstanceId);
					if (main != null) {
						st.setCpuStones(st.getCpuStones() - payCostStones);
						List<BattleCard> paid = new ArrayList<>();
						for (int i = 0; i < payCards; i++) {
							if (st.getCpuHand().isEmpty()) break;
							paid.add(st.getCpuHand().remove(st.getCpuHand().size() - 1));
						}
						paid.addAll(levelUpCards);
						ZoneFighter z = new ZoneFighter();
						z.setMain(main);
						z.setCostUnder(paid);
						z.setCostPayCardCount(payCards);
						z.setTemporaryPowerBonus(bestDeployBonus);
						st.setCpuNextDeployBonus(0);
						st.setCpuNextElfOnlyBonus(0);
						st.setCpuNextDeployCostBonusTimes(0);
						st.setCpuNextMechanicStacks(0);
						retireOwnBattleZoneBeforeNewDeploy(st, false, true);
						st.setCpuBattle(z);
						applyFieldNebulaWhenCarbuncleFighterDeployed(st, bestDef, false);
						if (payCostStones > 0 && payCards > 0) {
							st.addLog("CPUは「" + bestDef.getName() + "」を配置（コスト: ストーン" + payCostStones + "＋カード" + payCards + "）");
						} else if (payCostStones > 0) {
							st.addLog("CPUは「" + bestDef.getName() + "」を配置（コスト: ストーン" + payCostStones + "）");
						} else {
							st.addLog("CPUは「" + bestDef.getName() + "」を配置した");
						}
						deployed = true;
						stagePendingDeployEffect(st, false, bestDef, z);
					}
				}
			}
			if (!deployed) {
				// 配置できなかった場合は、レベルアップで使ったカードはレストへ
				st.getCpuRest().addAll(levelUpCards);
			}
		}

		if (!deployed) {
			// CPU が「配置しない」を選んだ時点で、人間の勝利とする
			st.addLog("CPUは配置しなかった");
			st.setGameOver(true);
			st.setHumanWon(true);
			st.setPhase(BattlePhase.GAME_OVER);
			st.setLastMessage("勝利（CPUが配置しませんでした）");
			return;
		}
	}

	private void resolveKnockAndDraw(CpuBattleState st, boolean humanWasActing, Map<Short, CardDefinition> defs) {
		if (humanWasActing) {
			// 相手ファイターをレストへ（自分が配置している場合のみ）
			if (st.getHumanBattle() != null && st.getCpuBattle() != null) {
				boolean returned = moveZoneToRestOrReturnToHand(st.getCpuBattle(), st.getCpuRest(), st.getCpuHand());
				st.setCpuBattle(null);
				st.addLog(returned ? "相手ファイターは手札へ戻った" : "相手ファイターをレストへ");
			}
			while (st.getHumanHand().size() < 4 && !st.getHumanDeck().isEmpty()) {
				drawOne(st.getHumanDeck(), st.getHumanHand());
			}
		} else {
			if (st.getCpuBattle() != null && st.getHumanBattle() != null) {
				boolean returned = moveZoneToRestOrReturnToHand(st.getHumanBattle(), st.getHumanRest(), st.getHumanHand());
				st.setHumanBattle(null);
				st.addLog(returned ? "あなたのファイターは手札へ戻った" : "あなたのファイターがレストへ");
			}
			while (st.getCpuHand().size() < 4 && !st.getCpuDeck().isEmpty()) {
				drawOne(st.getCpuDeck(), st.getCpuHand());
			}
		}
	}

	private boolean moveZoneToRestOrReturnToHand(ZoneFighter z, List<BattleCard> rest, List<BattleCard> hand) {
		if (z == null || z.getMain() == null) return false;
		// コストカードは常にレストへ
		for (BattleCard c : z.getCostUnder()) {
			rest.add(c);
		}
		if (z.isReturnToHandOnKnock()) {
			hand.add(z.getMain());
			return true;
		} else {
			rest.add(z.getMain());
			return false;
		}
	}

	private void moveZoneToRest(ZoneFighter z, List<BattleCard> rest) {
		if (z == null || z.getMain() == null) {
			return;
		}
		for (BattleCard c : z.getCostUnder()) {
			rest.add(c);
		}
		rest.add(z.getMain());
	}

	/**
	 * 手札から新しいファイターを配置する直前に呼ぶ。既に自バトルゾーンにファイターがいる場合は、
	 * 旧ファイター（とコスト下）をレストへ送るか、手札へ戻す設定なら手札へ戻す。
	 * 置き換えで前のカードが状態から消えると、「レストにカーバンクルがある」系の〈配置〉が誤って不発になる。
	 */
	private void retireOwnBattleZoneBeforeNewDeploy(CpuBattleState st, boolean humanHost, boolean addLog) {
		if (st == null) {
			return;
		}
		ZoneFighter old = humanHost ? st.getHumanBattle() : st.getCpuBattle();
		if (old == null) {
			return;
		}
		if (humanHost) {
			boolean toHand = moveZoneToRestOrReturnToHand(old, st.getHumanRest(), st.getHumanHand());
			st.setHumanBattle(null);
			if (addLog) {
				st.addLog(toHand ? "あなたのファイターは手札へ戻った" : "あなたのファイターがレストへ");
			}
		} else {
			boolean toHand = moveZoneToRestOrReturnToHand(old, st.getCpuRest(), st.getCpuHand());
			st.setCpuBattle(null);
			if (addLog) {
				st.addLog(toHand
						? (opponentActorLogLabel(st) + "のファイターは手札へ戻った")
						: (opponentActorLogLabel(st) + "のファイターがレストへ"));
			}
		}
	}

	private void resetTurnBuffs(CpuBattleState st) {
		if (st.getHumanBattle() != null) {
			st.getHumanBattle().setTemporaryPowerBonus(0);
		}
		if (st.getCpuBattle() != null) {
			st.getCpuBattle().setTemporaryPowerBonus(0);
		}
		st.setPowerSwapActive(false);
	}

	public int effectiveBattlePower(ZoneFighter zf, boolean ownerIsHuman, CpuBattleState st,
			Map<Short, CardDefinition> defs) {
		if (st != null && st.isPowerSwapActive()) {
			int humanEff = effectiveBattlePowerNoSwap(st.getHumanBattle(), true, st, defs);
			int cpuEff = effectiveBattlePowerNoSwap(st.getCpuBattle(), false, st, defs);
			return ownerIsHuman ? cpuEff : humanEff;
		}
		return effectiveBattlePowerNoSwap(zf, ownerIsHuman, st, defs);
	}

	private int effectiveBattlePowerNoSwap(ZoneFighter zf, boolean ownerIsHuman, CpuBattleState st,
			Map<Short, CardDefinition> defs) {
		if (zf == null || zf.getMain() == null) {
			return 0;
		}
		short id = zf.getMain().getCardId();
		CardDefinition d = defs.get(id);
		int p = d.getBasePower() + zf.getTemporaryPowerBonus();
		p += fieldGloriaCarbunclePowerBonus(st, d, defs);

		boolean suppress = ownerIsHuman
				? hasRyuoh(st.getCpuBattle())
				: hasRyuoh(st.getHumanBattle());
		if (suppress) {
			return Math.max(0, p);
		}

		// 「竜王」が自分側にいる間、相手の配置/常時は無効（= 相手由来のデバフ等も発動しない）
		boolean suppressOpponentEffects = ownerIsHuman
				? hasRyuoh(st.getHumanBattle())
				: hasRyuoh(st.getCpuBattle());

		// 一時強化（古竜）
		if (st != null) {
			p += ownerIsHuman ? st.getHumanKoryuBonus() : st.getCpuKoryuBonus();
		}

		// 薬売り（常時）: 自分が所持しているストーン1つにつき、相手のファイター強さ-1（薬売りがバトルゾーンにいる間）
		if (!suppressOpponentEffects) {
			ZoneFighter oppZone = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (oppZone != null && oppZone.getMain() != null && oppZone.getMain().getCardId() == KUSURI_ID) {
				int debuff = ownerIsHuman ? st.getCpuStones() : st.getHumanStones();
				p -= debuff;
			}
		}

		if (zf.getMain().isBlankEffects()) {
			return Math.max(0, p);
		}

		if (id == ARCHER_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null) {
				CardDefinition od = defs.get(opp.getMain().getCardId());
				if (!CardAttributes.hasAttribute(od, "DRAGON")) {
					p += 1;
				}
			}
		}

		if (id == DRAGON_RIDER_ID && ownerIsHuman) {
			if (restContainsAttribute(st.getHumanRest(), defs, "DRAGON")) {
				p += 4;
			}
		}
		if (id == DRAGON_RIDER_ID && !ownerIsHuman) {
			if (restContainsAttribute(st.getCpuRest(), defs, "DRAGON")) {
				p += 4;
			}
		}

		if (id == GAIKOTSU_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), "ELF")) {
				p += 2;
			}
		}

		if (id == SHIREI_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && !CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), "HUMAN")) {
				p += 1;
			}
		}

		if (id == HONE_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			int undead = 0;
			for (BattleCard c : rest) {
				if (isTuckedUnderOwnFighter(zf, c)) {
					continue;
				}
				if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "UNDEAD")) {
					undead++;
				}
			}
			p += undead;
		}

		if (id == SHINY_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			int kinds = countDistinctCarbuncleTypesInRest(zf, rest, defs);
			p += kinds * 2;
		}

		if (id == FROSTKRUL_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			if (oppTurn && restContainsAttribute(rest, defs, "CARBUNCLE")) {
				p += 3;
			}
		}

		if (id == NEMURY_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			if (oppTurn) {
				p += 5;
			}
		}

		if (id == ARTHUR_ID) {
			BattleCard field = st != null ? st.getActiveField() : null;
			if (field != null && field.getCardId() == FIELD_KAMUI_ID) {
				p += 3;
			}
		}

		return Math.max(0, p);
	}

	/**
	 * 画面左（自分）／右（相手）列に表示している強さの内訳に対応する要因。
	 * {@link #effectiveBattlePower} のパワースワップと同じ視点。
	 */
	public List<BattlePowerModifierDto> explainDisplayedPowerContributors(boolean forHumanSide, CpuBattleState st,
			Map<Short, CardDefinition> defs) {
		if (st == null) {
			return List.of();
		}
		if (st.isPowerSwapActive()) {
			if (forHumanSide) {
				return explainPowerContributorsNoSwap(st.getCpuBattle(), false, st, defs);
			}
			return explainPowerContributorsNoSwap(st.getHumanBattle(), true, st, defs);
		}
		if (forHumanSide) {
			return explainPowerContributorsNoSwap(st.getHumanBattle(), true, st, defs);
		}
		return explainPowerContributorsNoSwap(st.getCpuBattle(), false, st, defs);
	}

	/** {@link #effectiveBattlePowerNoSwap} と同じ条件で、基礎強さからの増減要因だけを列挙する */
	private List<BattlePowerModifierDto> explainPowerContributorsNoSwap(ZoneFighter zf, boolean ownerIsHuman,
			CpuBattleState st, Map<Short, CardDefinition> defs) {
		List<BattlePowerModifierDto> out = new ArrayList<>();
		if (zf == null || zf.getMain() == null || defs == null) {
			return out;
		}
		short id = zf.getMain().getCardId();
		CardDefinition d = defs.get(id);
		if (d == null) {
			return out;
		}

		boolean suppress = ownerIsHuman
				? hasRyuoh(st.getCpuBattle())
				: hasRyuoh(st.getHumanBattle());
		if (suppress) {
			if (zf.getTemporaryPowerBonus() != 0) {
				out.add(new BattlePowerModifierDto(null, "レベルアップ（配置）"));
			}
			if (fieldGloriaCarbunclePowerBonus(st, d, defs) > 0) {
				out.add(new BattlePowerModifierDto(FIELD_GLORIA_ID, "〈宝石の地〉"));
			}
			return out;
		}

		boolean suppressOpponentEffects = ownerIsHuman
				? hasRyuoh(st.getHumanBattle())
				: hasRyuoh(st.getCpuBattle());

		if (zf.getTemporaryPowerBonus() != 0) {
			out.add(new BattlePowerModifierDto(null, "レベルアップ（配置）"));
		}

		if (fieldGloriaCarbunclePowerBonus(st, d, defs) > 0) {
			out.add(new BattlePowerModifierDto(FIELD_GLORIA_ID, "〈宝石の地〉"));
		}

		int koryu = ownerIsHuman ? st.getHumanKoryuBonus() : st.getCpuKoryuBonus();
		if (koryu > 0) {
			out.add(new BattlePowerModifierDto(KORYU_ID, null));
		}

		if (!suppressOpponentEffects) {
			ZoneFighter oppZone = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (oppZone != null && oppZone.getMain() != null && oppZone.getMain().getCardId() == KUSURI_ID) {
				int debuff = ownerIsHuman ? st.getCpuStones() : st.getHumanStones();
				if (debuff > 0) {
					out.add(new BattlePowerModifierDto(KUSURI_ID, "（相手の薬売り・ストーン" + debuff + "）"));
				}
			}
		}

		if (zf.getMain().isBlankEffects()) {
			return out;
		}

		if (id == ARCHER_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && opp.getMain() != null) {
				CardDefinition od = defs.get(opp.getMain().getCardId());
				if (!CardAttributes.hasAttribute(od, "DRAGON")) {
					out.add(new BattlePowerModifierDto(ARCHER_ID, null));
				}
			}
		}

		if (id == DRAGON_RIDER_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			if (restContainsAttribute(rest, defs, "DRAGON")) {
				out.add(new BattlePowerModifierDto(DRAGON_RIDER_ID, "（レストのドラゴン）"));
			}
		}

		if (id == GAIKOTSU_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && opp.getMain() != null
					&& CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), "ELF")) {
				out.add(new BattlePowerModifierDto(opp.getMain().getCardId(), null));
			}
		}

		if (id == SHIREI_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && opp.getMain() != null
					&& !CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), "HUMAN")) {
				out.add(new BattlePowerModifierDto(opp.getMain().getCardId(), null));
			}
		}

		if (id == HONE_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			for (BattleCard c : rest) {
				if (isTuckedUnderOwnFighter(zf, c)) {
					continue;
				}
				if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "UNDEAD")) {
					out.add(new BattlePowerModifierDto(c.getCardId(), null));
				}
			}
		}

		if (id == SHINY_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			Set<Short> seen = new HashSet<>();
			for (BattleCard c : rest) {
				if (isTuckedUnderOwnFighter(zf, c)) {
					continue;
				}
				if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "CARBUNCLE") && seen.add(c.getCardId())) {
					out.add(new BattlePowerModifierDto(c.getCardId(), "（種類+2）"));
				}
			}
		}

		if (id == FROSTKRUL_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			if (oppTurn && restContainsAttribute(rest, defs, "CARBUNCLE")) {
				out.add(new BattlePowerModifierDto(FROSTKRUL_ID, "（相手ターン・レスト条件）"));
			}
		}

		if (id == NEMURY_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			if (oppTurn) {
				out.add(new BattlePowerModifierDto(NEMURY_ID, "（相手ターン）"));
			}
		}

		if (id == ARTHUR_ID) {
			BattleCard field = st != null ? st.getActiveField() : null;
			if (field != null && field.getCardId() == FIELD_KAMUI_ID) {
				out.add(new BattlePowerModifierDto(FIELD_KAMUI_ID, "〈決戦の地 カムイ〉"));
			}
		}

		return out;
	}

	private boolean hasRyuoh(ZoneFighter z) {
		return z != null && z.getMain() != null && z.getMain().getCardId() == RYUOH_ID;
	}

	/** ミスティンクル（33）: 相手の〈配置〉のみ封じる（〈常時〉は対象外） */
	private static boolean hasMistyinkle(ZoneFighter z) {
		return z != null && z.getMain() != null && z.getMain().getCardId() == MISTYINKUL_ID;
	}

	/**
	 * 配置した側が {@code deployerIsHuman} のとき、相手バトルゾーンの竜王またはミスティンクルにより〈配置〉が無効になるか。
	 */
	private boolean opponentSuppressesDeployEffects(CpuBattleState st, boolean deployerIsHuman) {
		if (st == null) {
			return false;
		}
		ZoneFighter opp = deployerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
		return hasRyuoh(opp) || hasMistyinkle(opp);
	}

	/**
	 * 決戦の地 カムイ: 〈フィールド〉が場にあるとき、もとの強さ（{@link CardDefinition#getBasePower()}、レベルアップ・一時加算・常時以外の基礎値）が3のファイターは〈配置〉効果を使えない。
	 * 忍者の入れ替え後は {@code fighterDef} に入れ替え後のメインを渡すこと。
	 */
	private static boolean fieldKamuiSuppressesDeployEffects(CpuBattleState st, CardDefinition fighterDef) {
		if (st == null || fighterDef == null) {
			return false;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != FIELD_KAMUI_ID) {
			return false;
		}
		Short bp = fighterDef.getBasePower();
		return bp != null && bp == 3;
	}

	/**
	 * ミラージュクル: 相手バトルゾーンの〈配置〉コードをコピーして適用（相手もミラージュのときは無効）。
	 */
	private void applyMirageMirrorDeploy(CpuBattleState st, boolean mirageOwnerIsHuman, Map<Short, CardDefinition> defs,
			Random rnd) {
		ZoneFighter opp = mirageOwnerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
		if (opp == null || opp.getMain() == null) {
			return;
		}
		CardDefinition oppFighter = defs.get(opp.getMain().getCardId());
		if (oppFighter == null) {
			return;
		}
		String code = oppFighter.getAbilityDeployCode();
		if (code == null || code.isBlank()) {
			return;
		}
		if ("MIRAJUKUL".equals(code)) {
			st.addLog("ミラージュクル: 相手もミラージュクルのためコピーできない");
			return;
		}
		CardDefinition ghost = new CardDefinition();
		ghost.setName(oppFighter.getName());
		ghost.setAbilityDeployCode(code);
		if (mirageOwnerIsHuman) {
			applyDeployHuman(st, ghost, defs, null);
		} else if (st.isPvp()) {
			applyDeployHumanAsCpuSide(st, ghost, defs, null);
		} else {
			applyDeployCpu(st, ghost, defs, rnd != null ? rnd : new Random(), null);
		}
	}

	/**
	 * バトルゾーンのメインの下に差し込まれたカード（コスト・レベルアップ消費など）はレストゾーンにいない。
	 * 状態不整合で同一インスタンスがレスト一覧にも残っている場合、常時効果の「レストの枚数」から除外する。
	 */
	private static Set<String> battleCostUnderInstanceIds(ZoneFighter zf) {
		Set<String> out = new HashSet<>();
		if (zf == null || zf.getCostUnder() == null) {
			return out;
		}
		for (BattleCard c : zf.getCostUnder()) {
			if (c != null && c.getInstanceId() != null) {
				out.add(c.getInstanceId());
			}
		}
		return out;
	}

	private static boolean isTuckedUnderOwnFighter(ZoneFighter ownBattle, BattleCard restCard) {
		if (restCard == null) {
			return false;
		}
		String id = restCard.getInstanceId();
		if (id == null) {
			return false;
		}
		return battleCostUnderInstanceIds(ownBattle).contains(id);
	}

	/** 助手: カード名に「研究者」を含む（部分一致・全角そのまま） */
	private static boolean cardNameContainsKenkyusha(CardDefinition def) {
		if (def == null || def.getName() == null) {
			return false;
		}
		return def.getName().contains("研究者");
	}

	/**
	 * 忍者: メインとコスト支払い先頭カードを入れ替え、強さ-2（一時）。入れ替え後の〈配置〉は入れ替え先の能力で処理する。
	 * @return 入れ替え後のメインに対応する定義（入れ替えなしなら引数 d）
	 */
	private CardDefinition applyNinjaDeployRewrite(CpuBattleState st, Map<Short, CardDefinition> defs, CardDefinition d,
			boolean humanHost) {
		if (d == null || d.getAbilityDeployCode() == null || !"NINJA".equals(d.getAbilityDeployCode())) {
			return d;
		}
		ZoneFighter z = humanHost ? st.getHumanBattle() : st.getCpuBattle();
		if (z == null || z.getMain() == null) {
			return d;
		}
		z.setTemporaryPowerBonus(z.getTemporaryPowerBonus() - 2);
		if (z.getCostPayCardCount() > 0 && !z.getCostUnder().isEmpty()) {
			BattleCard paidCard = z.getCostUnder().get(0);
			BattleCard oldMain = z.getMain();
			z.setMain(paidCard);
			z.getCostUnder().set(0, oldMain);
			CardDefinition paidDef = defs.get(paidCard.getCardId());
			String paidName = paidDef != null && paidDef.getName() != null ? paidDef.getName() : "？";
			st.addLog("忍者: 「" + paidName + "」と入れ替え（強さ-2）");
			return defs.get(z.getMain().getCardId());
		}
		st.addLog("忍者: コストにカードがないため入れ替えなし（強さ-2）");
		return d;
	}

	private boolean restContainsAttribute(List<BattleCard> rest, Map<Short, CardDefinition> defs, String attr) {
		for (BattleCard c : rest) {
			if (CardAttributes.hasAttribute(defs.get(c.getCardId()), attr)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * クリスタクル〈配置〉の前提判定。レストにカーバンクルがあるか、今配置した自分バトルの
	 * コスト下（手札からの支払い・レベルアップで本体下に重ねたカード）にカーバンクルがあるか。
	 * 後者はレストに積まれないため、レストのみを見ると条件を満たしているのに発動しない不具合になる。
	 */
	private boolean crystakulCarbuncleSupportPresent(CpuBattleState st, boolean ownerIsHuman,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return false;
		}
		List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
		ZoneFighter z = ownerIsHuman ? st.getHumanBattle() : st.getCpuBattle();
		// シャイニ等と同様、バトルに重なって表示される分は「レストにいる」とみなさない
		for (BattleCard c : rest) {
			if (c == null || isTuckedUnderOwnFighter(z, c)) {
				continue;
			}
			if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "CARBUNCLE")) {
				return true;
			}
		}
		if (z == null || z.getCostUnder() == null) {
			return false;
		}
		for (BattleCard c : z.getCostUnder()) {
			if (c != null && CardAttributes.hasAttribute(defs.get(c.getCardId()), "CARBUNCLE")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * クリスタクル〈配置〉の「ストーン2つ使用」が可能か。
	 * 通常は deploy 直後に Nebula が加算済みで {@code stones >= 2}。
	 * 〈配置〉評価時点で Nebula 分が所持に未反映のとき、場がネビュラかつ配置がカーバンクルなら支払可能とみなす。
	 * 所持1が「ネビュラで 0→1 しただけ」のときはストーン2を払えない（{@link ZoneFighter#fieldNebulaStoneGrantedForThisDeploy} で区別）。
	 */
	private boolean crystakulCanAffordOptionalStonePay(CpuBattleState st, boolean deployerUsesHumanStones,
			Map<Short, CardDefinition> defs, BattleCard deployedMain) {
		if (st == null || defs == null) {
			return false;
		}
		int s = deployerUsesHumanStones ? st.getHumanStones() : st.getCpuStones();
		if (s >= 2) {
			return true;
		}
		if (s != 1 || deployedMain == null) {
			return false;
		}
		CardDefinition dm = defs.get(deployedMain.getCardId());
		if (dm == null || isFieldCard(dm) || !CardAttributes.hasAttribute(dm, "CARBUNCLE")) {
			return false;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != FIELD_NEBULA_ID) {
			return false;
		}
		ZoneFighter z = deployerUsesHumanStones ? st.getHumanBattle() : st.getCpuBattle();
		if (z != null && z.isFieldNebulaStoneGrantedForThisDeploy()) {
			return false;
		}
		return true;
	}

	/**
	 * クリスタクルの任意ストーン支払いを確定する直前に呼ぶ。
	 * Nebula 分が所持に未反映なら {@link #applyFieldNebulaWhenCarbuncleFighterDeployed} で補う。
	 */
	private void ensureNebulaStoneForCrystakulOptionalPayment(CpuBattleState st, boolean deployerUsesHumanStones,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		int s = deployerUsesHumanStones ? st.getHumanStones() : st.getCpuStones();
		if (s >= 2) {
			return;
		}
		ZoneFighter z = deployerUsesHumanStones ? st.getHumanBattle() : st.getCpuBattle();
		BattleCard main = z != null ? z.getMain() : null;
		if (!crystakulCanAffordOptionalStonePay(st, deployerUsesHumanStones, defs, main)) {
			return;
		}
		CardDefinition md = main != null ? defs.get(main.getCardId()) : null;
		if (md == null) {
			return;
		}
		if (z != null && z.isFieldNebulaStoneGrantedForThisDeploy()) {
			return;
		}
		applyFieldNebulaWhenCarbuncleFighterDeployed(st, md, deployerUsesHumanStones);
	}

	/** フェザリアの回収対象: カーバンクルかつ「フェザリア」カード自身は除く */
	private static boolean isFezariaPickableCarbuncleInRest(BattleCard c, Map<Short, CardDefinition> defs) {
		if (c == null || defs == null) {
			return false;
		}
		if (c.getCardId() == FEATHERIA_ID) {
			return false;
		}
		return CardAttributes.hasAttribute(defs.get(c.getCardId()), "CARBUNCLE");
	}

	private boolean restContainsFezariaPickableCarbuncle(List<BattleCard> rest, Map<Short, CardDefinition> defs) {
		if (rest == null) {
			return false;
		}
		for (BattleCard c : rest) {
			if (isFezariaPickableCarbuncleInRest(c, defs)) {
				return true;
			}
		}
		return false;
	}

	/** シャイニ: レストの「種族：カーバンクル」カードの種類数（同じカードIDの重複は1種類） */
	private int countDistinctCarbuncleTypesInRest(ZoneFighter ownBattle, List<BattleCard> rest,
			Map<Short, CardDefinition> defs) {
		if (rest == null || defs == null) {
			return 0;
		}
		Set<Short> kinds = new HashSet<>();
		for (BattleCard c : rest) {
			if (c == null || isTuckedUnderOwnFighter(ownBattle, c)) {
				continue;
			}
			if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "CARBUNCLE")) {
				kinds.add(c.getCardId());
			}
		}
		return kinds.size();
	}

	private void applyDeployHuman(CpuBattleState st, CardDefinition d, Map<Short, CardDefinition> defs,
			BattleCard deployedMain) {
		// 相手の竜王、またはミスティンクルがいる間は〈配置〉は発動しない
		if (st != null && opponentSuppressesDeployEffects(st, true)) {
			return;
		}
		d = applyNinjaDeployRewrite(st, defs, d, true);
		deployedMain = st.getHumanBattle() != null ? st.getHumanBattle().getMain() : null;
		if (deployedMain != null && deployedMain.isBlankEffects()) {
			return;
		}
		CardDefinition fighterForKamui = deployedMain != null ? defs.get(deployedMain.getCardId()) : null;
		if (fieldKamuiSuppressesDeployEffects(st, fighterForKamui)) {
			return;
		}
		String code = d != null ? d.getAbilityDeployCode() : null;
		if (code == null) {
			return;
		}
		switch (code) {
			case "SAKUSHI" -> {
				if (!st.getCpuDeck().isEmpty()) {
					st.getCpuRest().add(st.getCpuDeck().remove(0));
					st.addLog("策士: 相手デッキ上をレストへ");
				}
			}
			case "SAMURAI" -> {
				if (st.getHumanStones() >= 3 && !st.getCpuHand().isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"サムライ",
							true,
							"SAMURAI",
							3,
							List.of()
					));
				}
			}
			case "KOSAKUIN" -> {
				// 用心棒（旧: 工作員）に変更されたため効果なし
			}
			case "KAGAKUSHA" -> {
				if (st.getHumanBattle() != null && st.getCpuBattle() != null) {
					st.setPowerSwapActive(true);
					st.addLog("科学者: 強さを入れ替えた");
				}
			}
			case "OKAMI_OTOKO" -> {
				if (st.getHumanBattle() != null) {
					swapMainWithWolfIfPaid(st.getHumanBattle());
				}
			}
			case "MIKO" -> {
				// エルフの巫女: ストーン消費なしで、次回配置+1（任意選択なし）
				st.setHumanNextDeployBonus(st.getHumanNextDeployBonus() + 1);
				st.addLog("エルフの巫女: 次の配置+1");
			}
			case "YOSEI" -> {
				if (st.getHumanStones() >= 1) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ウッドエルフ",
							true,
							"YOSEI",
							1,
							List.of()
					));
				}
			}
			case "SHOKIN" -> {
				// 隊長: 次の配置はコストぶん強化（重ねがけ可）
				st.setHumanNextDeployCostBonusTimes(st.getHumanNextDeployCostBonusTimes() + 1);
				st.addLog("隊長: 次の配置はコストぶん強化");
			}
			case "MECHANIC" -> {
				st.setHumanNextMechanicStacks(st.getHumanNextMechanicStacks() + 1);
				st.addLog("メカニック: 次の配置はコスト+1、強さ+3");
			}
			case "KINOKO" -> {
				List<String> pixieOpts = new ArrayList<>();
				for (BattleCard c : st.getHumanRest()) {
					if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "ELF")) {
						pixieOpts.add(c.getInstanceId());
					}
				}
				if (!pixieOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"ピクシー（レストのエルフを1枚選択）",
							true,
							"KINOKO",
							0,
							pixieOpts
					));
				}
			}
			case "TANKOFU" -> {
				List<String> tankOpts = new ArrayList<>();
				ZoneFighter ownBattle = st.getHumanBattle();
				for (BattleCard c : st.getHumanRest()) {
					if (isTuckedUnderOwnFighter(ownBattle, c)) {
						continue;
					}
					if (isHumanFighterDef(defs.get(c.getCardId()))) {
						tankOpts.add(c.getInstanceId());
					}
				}
				if (!tankOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"炭鉱夫（レストの人間ファイターを1枚選択）",
							true,
							"TANKOFU",
							0,
							tankOpts
					));
				}
			}
			case "JOSHU" -> {
				List<String> joshuOpts = new ArrayList<>();
				ZoneFighter ownBattleJ = st.getHumanBattle();
				for (BattleCard c : st.getHumanRest()) {
					if (isTuckedUnderOwnFighter(ownBattleJ, c)) {
						continue;
					}
					if (cardNameContainsKenkyusha(defs.get(c.getCardId()))) {
						joshuOpts.add(c.getInstanceId());
					}
				}
				if (!joshuOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"助手（名前に「研究者」を含むカードを1枚選択）",
							true,
							"JOSHU",
							0,
							joshuOpts
					));
				}
			}
			case "ASTORIA" -> {
				List<String> astoriaOpts = new ArrayList<>();
				ZoneFighter ownBattleA = st.getHumanBattle();
				for (BattleCard c : st.getHumanRest()) {
					if (isTuckedUnderOwnFighter(ownBattleA, c)) {
						continue;
					}
					CardDefinition cd = defs.get(c.getCardId());
					if (cd != null && isFieldCard(cd)) {
						astoriaOpts.add(c.getInstanceId());
					}
				}
				if (!astoriaOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"研究者アストリア（レストの〈フィールド〉を1枚選択）",
							true,
							"ASTORIA",
							0,
							astoriaOpts
					));
				}
			}
			case "NOROWARETA" -> {
				if (st.getHumanStones() >= 1 && !st.getHumanRest().isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"呪われた亡者",
							true,
							"NOROWARETA",
							1,
							List.of()
					));
				}
			}
			case "FUWAFUWA" -> {
				if (st.getHumanStones() >= 1 && st.getHumanBattle() != null) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ふわふわゴースト",
							true,
							"FUWAFUWA",
							1,
							List.of()
					));
				}
			}
			case "NIDONEBI" -> {
				if (st.getHumanStones() >= 1 && restContainsCardId(st.getHumanRest(), (short) 18)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ネクロマンサー",
							true,
							"NIDONEBI",
							1,
							List.of()
					));
				}
			}
			case "RYUNOTAMAGO" -> {
				if (st.getHumanStones() >= 2 && restContainsAttribute(st.getHumanRest(), defs, "DRAGON")) {
					List<String> opts = new ArrayList<>();
					for (BattleCard c : st.getHumanRest()) {
						if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "DRAGON")) opts.add(c.getInstanceId());
					}
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ドラゴンの卵",
							true,
							"RYUNOTAMAGO",
							2,
							opts
					));
				}
			}
			case "KORYU" -> {
				int elves = countAttributeInRest(st.getHumanRest(), defs, "ELF");
				if (st.getHumanStones() >= 1 && st.getHumanBattle() != null && elves > 0) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"古竜",
							true,
							"KORYU",
							1,
							List.of()
					));
				}
			}
			case "KENTOSHI" -> {
				// 人間は選んで捨てる、CPUは自動で捨てる
				if (!st.getHumanHand().isEmpty()) {
					List<String> opts = new ArrayList<>();
					for (BattleCard c : st.getHumanHand()) opts.add(c.getInstanceId());
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
							"剣闘士（捨てるカードを選択）",
							true,
							"KENTOSHI",
							0,
							opts
					));
				}
				// CPU側は自動で1枚捨てる
				discardRightmost(st.getCpuHand(), st.getCpuRest());
				st.addLog("剣闘士: お互い手札を1枚レストへ");
			}
			case "KARYUDO" -> {
				if (!st.getCpuHand().isEmpty()) {
					int r = new Random().nextInt(st.getCpuHand().size());
					BattleCard c = st.getCpuHand().remove(r);
					st.getCpuDeck().add(0, c);
					st.addLog(st.isPvp() ? "狩人: 相手手札をデッキ上へ" : "狩人: CPU手札をデッキ上へ");
				}
			}
			case "KAENRYU" -> {
				if (st.getCpuBattle() != null) {
					moveZoneToRest(st.getCpuBattle(), st.getCpuRest());
					st.setCpuBattle(null);
					st.addLog("火炎竜: 相手ファイターをレストへ");
				}
			}
			case "DAKU_DORAGON" -> {
				st.setHumanStones(st.getHumanStones() + 2);
				st.addLog("ダークドラゴン: ストーン+2");
				if (st.getCpuBattle() != null
						&& CardAttributes.hasAttribute(defs.get(st.getCpuBattle().getMain().getCardId()), "DRAGON")) {
					moveZoneToRest(st.getCpuBattle(), st.getCpuRest());
					st.setCpuBattle(null);
					st.addLog("ダークドラゴン: 相手ドラゴンをレストへ");
				}
			}
			case "GURIFON" -> {
				if (st.getCpuStones() > 0) {
					st.setCpuStones(st.getCpuStones() - 1);
					st.addLog(d.getName() + ": " + opponentActorLogLabel(st) + "がストーンを1つ捨てた");
				}
			}
			case "KAZE_MAJIN" -> {
				st.setHumanStones(st.getHumanStones() + 2);
				st.addLog(d.getName() + ": ストーン+2");
			}
			case "NOXSKUL" -> {
				st.setHumanStones(st.getHumanStones() + 1);
				st.addLog("ノクスクル: ストーン+1");
			}
			case "CRYSTAKUL" -> {
				if (crystakulCanAffordOptionalStonePay(st, true, defs, deployedMain)
						&& crystakulCarbuncleSupportPresent(st, true, defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"クリスタクル（ストーン2使用で5獲得）",
							true,
							"CRYSTAKUL",
							2,
							List.of()
					));
				}
			}
			case "MIRAJUKUL" -> applyMirageMirrorDeploy(st, true, defs, new Random());
			case "STONIA" -> {
				if (st.getHumanBattle() != null) {
					int s = st.getHumanStones();
					ZoneFighter z = st.getHumanBattle();
					z.setTemporaryPowerBonus(z.getTemporaryPowerBonus() + s);
					st.addLog("ストーニア: 所持ストーン" + s + "のぶん強さ+" + s);
				}
			}
			case "LUMINANK" -> {
				int n = st.getHumanStones();
				st.setHumanStones(n * 2);
				st.addLog("ルミナンク: ストーンを" + n + "から" + (n * 2) + "に");
			}
			case "FEZARIA" -> {
				if (st.getHumanStones() >= 2 && restContainsFezariaPickableCarbuncle(st.getHumanRest(), defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"フェザリア（ストーン2・フェザリア以外のカーバンクルを回収）",
							true,
							"FEZARIA",
							2,
							List.of()
					));
				}
			}
			default -> {
			}
		}
	}

	/**
	 * 対人戦でゲストが配置したときの〈配置〉効果（{@link #applyDeployHuman} と配置者/相手を入れ替え）。
	 */
	private void applyDeployHumanAsCpuSide(CpuBattleState st, CardDefinition d, Map<Short, CardDefinition> defs,
			BattleCard deployedMain) {
		if (st != null && opponentSuppressesDeployEffects(st, false)) {
			return;
		}
		d = applyNinjaDeployRewrite(st, defs, d, false);
		deployedMain = st.getCpuBattle() != null ? st.getCpuBattle().getMain() : null;
		if (deployedMain != null && deployedMain.isBlankEffects()) {
			return;
		}
		CardDefinition fighterForKamuiGuest = deployedMain != null ? defs.get(deployedMain.getCardId()) : null;
		if (fieldKamuiSuppressesDeployEffects(st, fighterForKamuiGuest)) {
			return;
		}
		String code = d != null ? d.getAbilityDeployCode() : null;
		if (code == null) {
			return;
		}
		switch (code) {
			case "SAKUSHI" -> {
				if (!st.getHumanDeck().isEmpty()) {
					st.getHumanRest().add(st.getHumanDeck().remove(0));
					st.addLog("策士: 相手デッキ上をレストへ");
				}
			}
			case "SAMURAI" -> {
				if (st.getCpuStones() >= 3 && !st.getHumanHand().isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"サムライ",
							false,
							"SAMURAI",
							3,
							List.of(),
							true
					));
				}
			}
			case "KAGAKUSHA" -> {
				if (st.getHumanBattle() != null && st.getCpuBattle() != null) {
					st.setPowerSwapActive(true);
					st.addLog("科学者: 強さを入れ替えた");
				}
			}
			case "OKAMI_OTOKO" -> {
				if (st.getCpuBattle() != null) {
					swapMainWithWolfIfPaid(st.getCpuBattle());
				}
			}
			case "MIKO" -> {
				st.setCpuNextDeployBonus(st.getCpuNextDeployBonus() + 1);
				st.addLog("エルフの巫女: 次の配置+1");
			}
			case "YOSEI" -> {
				if (st.getCpuStones() >= 1) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ウッドエルフ",
							false,
							"YOSEI",
							1,
							List.of(),
							true
					));
				}
			}
			case "SHOKIN" -> {
				st.setCpuNextDeployCostBonusTimes(st.getCpuNextDeployCostBonusTimes() + 1);
				st.addLog("隊長: 次の配置はコストぶん強化");
			}
			case "MECHANIC" -> {
				st.setCpuNextMechanicStacks(st.getCpuNextMechanicStacks() + 1);
				st.addLog("メカニック: 次の配置はコスト+1、強さ+3");
			}
			case "KINOKO" -> {
				List<String> pixieGuestOpts = new ArrayList<>();
				for (BattleCard c : st.getCpuRest()) {
					if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "ELF")) {
						pixieGuestOpts.add(c.getInstanceId());
					}
				}
				if (!pixieGuestOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"ピクシー（レストのエルフを1枚選択）",
							false,
							"KINOKO",
							0,
							pixieGuestOpts,
							true
					));
				}
			}
			case "TANKOFU" -> {
				List<String> tankGuestOpts = new ArrayList<>();
				ZoneFighter ownBattle = st.getCpuBattle();
				for (BattleCard c : st.getCpuRest()) {
					if (isTuckedUnderOwnFighter(ownBattle, c)) {
						continue;
					}
					if (isHumanFighterDef(defs.get(c.getCardId()))) {
						tankGuestOpts.add(c.getInstanceId());
					}
				}
				if (!tankGuestOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"炭鉱夫（レストの人間ファイターを1枚選択）",
							false,
							"TANKOFU",
							0,
							tankGuestOpts,
							true
					));
				}
			}
			case "JOSHU" -> {
				List<String> joshuGuestOpts = new ArrayList<>();
				ZoneFighter ownBattleJg = st.getCpuBattle();
				for (BattleCard c : st.getCpuRest()) {
					if (isTuckedUnderOwnFighter(ownBattleJg, c)) {
						continue;
					}
					if (cardNameContainsKenkyusha(defs.get(c.getCardId()))) {
						joshuGuestOpts.add(c.getInstanceId());
					}
				}
				if (!joshuGuestOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"助手（名前に「研究者」を含むカードを1枚選択）",
							false,
							"JOSHU",
							0,
							joshuGuestOpts,
							true
					));
				}
			}
			case "ASTORIA" -> {
				List<String> astoriaGuestOpts = new ArrayList<>();
				ZoneFighter ownBattleAg = st.getCpuBattle();
				for (BattleCard c : st.getCpuRest()) {
					if (isTuckedUnderOwnFighter(ownBattleAg, c)) {
						continue;
					}
					CardDefinition cd = defs.get(c.getCardId());
					if (cd != null && isFieldCard(cd)) {
						astoriaGuestOpts.add(c.getInstanceId());
					}
				}
				if (!astoriaGuestOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"研究者アストリア（レストの〈フィールド〉を1枚選択）",
							false,
							"ASTORIA",
							0,
							astoriaGuestOpts,
							true
					));
				}
			}
			case "NOROWARETA" -> {
				if (st.getCpuStones() >= 1 && !st.getCpuRest().isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"呪われた亡者",
							false,
							"NOROWARETA",
							1,
							List.of(),
							true
					));
				}
			}
			case "FUWAFUWA" -> {
				if (st.getCpuStones() >= 1 && st.getCpuBattle() != null) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ふわふわゴースト",
							false,
							"FUWAFUWA",
							1,
							List.of(),
							true
					));
				}
			}
			case "NIDONEBI" -> {
				if (st.getCpuStones() >= 1 && restContainsCardId(st.getCpuRest(), (short) 18)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ネクロマンサー",
							false,
							"NIDONEBI",
							1,
							List.of(),
							true
					));
				}
			}
			case "RYUNOTAMAGO" -> {
				if (st.getCpuStones() >= 2 && restContainsAttribute(st.getCpuRest(), defs, "DRAGON")) {
					List<String> opts = new ArrayList<>();
					for (BattleCard c : st.getCpuRest()) {
						if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "DRAGON")) opts.add(c.getInstanceId());
					}
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ドラゴンの卵",
							false,
							"RYUNOTAMAGO",
							2,
							opts,
							true
					));
				}
			}
			case "KORYU" -> {
				int elves = countAttributeInRest(st.getCpuRest(), defs, "ELF");
				if (st.getCpuStones() >= 1 && st.getCpuBattle() != null && elves > 0) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"古竜",
							false,
							"KORYU",
							1,
							List.of(),
							true
					));
				}
			}
			case "KENTOSHI" -> {
				if (!st.getHumanHand().isEmpty()) {
					List<String> opts = new ArrayList<>();
					for (BattleCard c : st.getHumanHand()) opts.add(c.getInstanceId());
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
							"剣闘士（捨てるカードを選択）",
							true,
							"KENTOSHI",
							0,
							opts
					));
				}
				// CPU側は自動で1枚捨てる
				discardRightmost(st.getCpuHand(), st.getCpuRest());
				st.addLog("剣闘士: お互い手札を1枚レストへ");
			}
			case "KARYUDO" -> {
				if (!st.getHumanHand().isEmpty()) {
					int r = new Random().nextInt(st.getHumanHand().size());
					BattleCard c = st.getHumanHand().remove(r);
					st.getHumanDeck().add(0, c);
					st.addLog("狩人: 相手手札をデッキ上へ");
				}
			}
			case "KAENRYU" -> {
				if (st.getHumanBattle() != null) {
					moveZoneToRest(st.getHumanBattle(), st.getHumanRest());
					st.setHumanBattle(null);
					st.addLog("火炎竜: 相手ファイターをレストへ");
				}
			}
			case "DAKU_DORAGON" -> {
				st.setCpuStones(st.getCpuStones() + 2);
				st.addLog("ダークドラゴン: ストーン+2");
				if (st.getHumanBattle() != null
						&& CardAttributes.hasAttribute(defs.get(st.getHumanBattle().getMain().getCardId()), "DRAGON")) {
					moveZoneToRest(st.getHumanBattle(), st.getHumanRest());
					st.setHumanBattle(null);
					st.addLog("ダークドラゴン: 相手ドラゴンをレストへ");
				}
			}
			case "GURIFON" -> {
				if (st.getHumanStones() > 0) {
					st.setHumanStones(st.getHumanStones() - 1);
					st.addLog(d.getName() + ": 相手がストーンを1つ捨てた");
				}
			}
			case "KAZE_MAJIN" -> {
				st.setCpuStones(st.getCpuStones() + 2);
				st.addLog(d.getName() + ": ストーン+2");
			}
			case "NOXSKUL" -> {
				st.setCpuStones(st.getCpuStones() + 1);
				st.addLog("ノクスクル: ストーン+1");
			}
			case "CRYSTAKUL" -> {
				if (crystakulCanAffordOptionalStonePay(st, false, defs, deployedMain)
						&& crystakulCarbuncleSupportPresent(st, false, defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"クリスタクル（ストーン2使用で5獲得）",
							false,
							"CRYSTAKUL",
							2,
							List.of(),
							true
					));
				}
			}
			case "MIRAJUKUL" -> applyMirageMirrorDeploy(st, false, defs, new Random());
			case "STONIA" -> {
				if (st.getCpuBattle() != null) {
					int s = st.getCpuStones();
					ZoneFighter z = st.getCpuBattle();
					z.setTemporaryPowerBonus(z.getTemporaryPowerBonus() + s);
					st.addLog("ストーニア: 所持ストーン" + s + "のぶん強さ+" + s);
				}
			}
			case "LUMINANK" -> {
				int n = st.getCpuStones();
				st.setCpuStones(n * 2);
				st.addLog("ルミナンク: ストーンを" + n + "から" + (n * 2) + "に");
			}
			case "FEZARIA" -> {
				if (st.getCpuStones() >= 2 && restContainsFezariaPickableCarbuncle(st.getCpuRest(), defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"フェザリア（ストーン2・フェザリア以外のカーバンクルを回収）",
							false,
							"FEZARIA",
							2,
							List.of(),
							true
					));
				}
			}
			default -> {
			}
		}
	}

	private void applyDeployCpu(CpuBattleState st, CardDefinition d, Map<Short, CardDefinition> defs, Random rnd,
			BattleCard deployedMain) {
		// 相手の竜王、またはミスティンクルがいる間は〈配置〉は発動しない
		if (st != null && opponentSuppressesDeployEffects(st, false)) {
			return;
		}
		d = applyNinjaDeployRewrite(st, defs, d, false);
		deployedMain = st.getCpuBattle() != null ? st.getCpuBattle().getMain() : null;
		if (deployedMain != null && deployedMain.isBlankEffects()) {
			return;
		}
		CardDefinition fighterForKamuiCpu = deployedMain != null ? defs.get(deployedMain.getCardId()) : null;
		if (fieldKamuiSuppressesDeployEffects(st, fighterForKamuiCpu)) {
			return;
		}
		String code = d != null ? d.getAbilityDeployCode() : null;
		if (code == null) {
			return;
		}
		switch (code) {
			case "SAKUSHI" -> {
				if (!st.getHumanDeck().isEmpty()) {
					st.getHumanRest().add(st.getHumanDeck().remove(0));
					st.addLog("CPU策士: あなたのデッキ上をレストへ");
				}
			}
			case "SAMURAI" -> {
				// CPUは自動判断。ストーンがあり、相手手札があるなら使う（簡易）
				if (st.getCpuStones() >= 3 && !st.getHumanHand().isEmpty()) {
					st.setCpuStones(st.getCpuStones() - 3);
					List<String> opts = new ArrayList<>();
					for (BattleCard c : st.getHumanHand()) opts.add(c.getInstanceId());
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
							"サムライ（捨てるカードを1枚選択）",
							true,
							"SAMURAI",
							0,
							opts
					));
					st.addLog("CPUサムライ: ストーン3使用。手札を1枚レストへ（選択）");
				}
			}
			case "KENTOSHI" -> {
				// CPUが出した場合：人間は選んで捨てる、CPUは自動で捨てる
				if (!st.getHumanHand().isEmpty()) {
					List<String> opts = new ArrayList<>();
					for (BattleCard c : st.getHumanHand()) opts.add(c.getInstanceId());
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
							"剣闘士（捨てるカードを選択）",
							true,
							"KENTOSHI",
							0,
							opts
					));
				}
				// CPU側は自動で1枚捨てる
				discardRightmost(st.getCpuHand(), st.getCpuRest());
			}
			case "KARYUDO" -> {
				if (!st.getHumanHand().isEmpty()) {
					int r = rnd.nextInt(st.getHumanHand().size());
					BattleCard c = st.getHumanHand().remove(r);
					st.getHumanDeck().add(0, c);
				}
			}
			case "KAENRYU" -> {
				if (st.getHumanBattle() != null) {
					moveZoneToRest(st.getHumanBattle(), st.getHumanRest());
					st.setHumanBattle(null);
				}
			}
			case "DAKU_DORAGON" -> {
				st.setCpuStones(st.getCpuStones() + 2);
				st.addLog("CPUダークドラゴン: ストーン+2");
				if (st.getHumanBattle() != null
						&& CardAttributes.hasAttribute(defs.get(st.getHumanBattle().getMain().getCardId()), "DRAGON")) {
					moveZoneToRest(st.getHumanBattle(), st.getHumanRest());
					st.setHumanBattle(null);
					st.addLog("CPUダークドラゴン: 相手ドラゴンをレストへ");
				}
			}
			case "GURIFON" -> {
				if (st.getHumanStones() > 0) {
					st.setHumanStones(st.getHumanStones() - 1);
				}
			}
			case "KAZE_MAJIN" -> {
				st.setCpuStones(st.getCpuStones() + 2);
			}
			case "KOSAKUIN" -> {
				// 用心棒（旧: 工作員）に変更されたため効果なし
			}
			case "KAGAKUSHA" -> {
				if (st.getHumanBattle() != null && st.getCpuBattle() != null) {
					st.setPowerSwapActive(true);
					st.addLog("CPU科学者: 強さを入れ替えた");
				}
			}
			case "OKAMI_OTOKO" -> {
				if (st.getCpuBattle() != null) {
					swapMainWithWolfIfPaid(st.getCpuBattle());
				}
			}
			case "MIKO" -> {
				// エルフの巫女: ストーン消費なしで、次回配置+1
				st.setCpuNextDeployBonus(st.getCpuNextDeployBonus() + 1);
			}
			case "YOSEI" -> {
				// CPU: ストーンがあれば使う（ウッドエルフ：次のエルフ配置+3）
				if (st.getCpuStones() >= 1) {
					st.setCpuStones(st.getCpuStones() - 1);
					st.setCpuNextElfOnlyBonus(st.getCpuNextElfOnlyBonus() + 3);
				}
			}
			case "SHOKIN" -> {
				st.setCpuNextDeployCostBonusTimes(st.getCpuNextDeployCostBonusTimes() + 1);
			}
			case "MECHANIC" -> {
				st.setCpuNextMechanicStacks(st.getCpuNextMechanicStacks() + 1);
				st.addLog("CPUメカニック: 次の配置はコスト+1、強さ+3");
			}
			case "KINOKO" -> {
				for (int i = st.getCpuRest().size() - 1; i >= 0; i--) {
					BattleCard c = st.getCpuRest().get(i);
					if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "ELF")) {
						st.getCpuRest().remove(i);
						st.getCpuHand().add(0, c);
						st.addLog("CPUピクシー: エルフを手札へ");
						break;
					}
				}
			}
			case "TANKOFU" -> {
				ZoneFighter ownBattle = st.getCpuBattle();
				for (int i = st.getCpuRest().size() - 1; i >= 0; i--) {
					BattleCard c = st.getCpuRest().get(i);
					if (isTuckedUnderOwnFighter(ownBattle, c)) {
						continue;
					}
					if (isHumanFighterDef(defs.get(c.getCardId()))) {
						st.getCpuRest().remove(i);
						c.setBlankEffects(true);
						st.getCpuHand().add(0, c);
						st.addLog("CPU炭鉱夫: 人間ファイターを手札へ（効果なし）");
						break;
					}
				}
			}
			case "JOSHU" -> {
				ZoneFighter ownBattleJ = st.getCpuBattle();
				for (int i = st.getCpuRest().size() - 1; i >= 0; i--) {
					BattleCard c = st.getCpuRest().get(i);
					if (isTuckedUnderOwnFighter(ownBattleJ, c)) {
						continue;
					}
					if (cardNameContainsKenkyusha(defs.get(c.getCardId()))) {
						st.getCpuRest().remove(i);
						st.getCpuHand().add(0, c);
						st.addLog("CPU助手: 名前に「研究者」を含むカードを手札へ");
						break;
					}
				}
			}
			case "ASTORIA" -> {
				ZoneFighter ownBattleAs = st.getCpuBattle();
				for (int i = st.getCpuRest().size() - 1; i >= 0; i--) {
					BattleCard c = st.getCpuRest().get(i);
					if (isTuckedUnderOwnFighter(ownBattleAs, c)) {
						continue;
					}
					CardDefinition cd = defs.get(c.getCardId());
					if (cd != null && isFieldCard(cd)) {
						st.getCpuRest().remove(i);
						c.setHandDeployCostModifier(-1);
						st.getCpuHand().add(0, c);
						st.addLog("CPU研究者アストリア: 〈フィールド〉を手札へ（コスト-1）");
						break;
					}
				}
			}
			case "NOROWARETA" -> {
				if (st.getCpuStones() >= 1 && !st.getCpuRest().isEmpty()) {
					st.setCpuStones(st.getCpuStones() - 1);
					int r = rnd.nextInt(st.getCpuRest().size());
					BattleCard c = st.getCpuRest().remove(r);
					st.getCpuDeck().add(0, c);
					Collections.shuffle(st.getCpuDeck(), rnd);
					st.addLog("CPU呪われた亡者: ストーン1使用。レストから1枚をデッキへ戻しシャッフル");
				}
			}
			case "FUWAFUWA" -> {
				if (st.getCpuStones() >= 1 && st.getCpuBattle() != null) {
					// 簡易: 使う
					st.setCpuStones(st.getCpuStones() - 1);
					st.getCpuBattle().setReturnToHandOnKnock(true);
					st.addLog("CPUふわふわ: 次に手札へ戻る");
				}
			}
			case "NIDONEBI" -> {
				if (st.getCpuStones() >= 1 && restContainsCardId(st.getCpuRest(), (short) 18)) {
					st.setCpuStones(st.getCpuStones() - 1);
					moveOneCardIdToDeckBottom(st.getCpuRest(), st.getCpuDeck(), (short) 18);
					st.addLog("CPUネクロマンサー: デッキ最下段へ");
				}
			}
			case "RYUNOTAMAGO" -> {
				if (st.getCpuStones() >= 2) {
					// 簡易: 使う（ドラゴンがあるなら）
					for (int i = 0; i < st.getCpuRest().size(); i++) {
						BattleCard c = st.getCpuRest().get(i);
						if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "DRAGON")) {
							st.setCpuStones(st.getCpuStones() - 2);
							st.getCpuRest().remove(i);
							st.getCpuHand().add(0, c);
							break;
						}
					}
				}
			}
			case "KORYU" -> {
				if (st.getCpuStones() >= 1 && st.getCpuBattle() != null) {
					int elves = countAttributeInRest(st.getCpuRest(), defs, "ELF");
					if (elves > 0) {
						st.setCpuStones(st.getCpuStones() - 1);
						st.setCpuKoryuBonus(elves);
					}
				}
			}
			case "NOXSKUL" -> {
				st.setCpuStones(st.getCpuStones() + 1);
				st.addLog("CPUノクスクル: ストーン+1");
			}
			case "CRYSTAKUL" -> {
				if (crystakulCanAffordOptionalStonePay(st, false, defs, deployedMain)
						&& crystakulCarbuncleSupportPresent(st, false, defs)) {
					st.setCpuStones(st.getCpuStones() - 2);
					st.setCpuStones(st.getCpuStones() + 5);
					st.addLog("CPUクリスタクル: ストーン2使用、5獲得");
				}
			}
			case "MIRAJUKUL" -> applyMirageMirrorDeploy(st, false, defs, rnd);
			case "STONIA" -> {
				if (st.getCpuBattle() != null) {
					int s = st.getCpuStones();
					ZoneFighter z = st.getCpuBattle();
					z.setTemporaryPowerBonus(z.getTemporaryPowerBonus() + s);
					st.addLog("CPUストーニア: 所持ストーン" + s + "のぶん強さ+" + s);
				}
			}
			case "LUMINANK" -> {
				int n = st.getCpuStones();
				st.setCpuStones(n * 2);
				st.addLog("CPUルミナンク: ストーンを" + n + "から" + (n * 2) + "に");
			}
			case "FEZARIA" -> {
				if (st.getCpuStones() >= 2 && restContainsFezariaPickableCarbuncle(st.getCpuRest(), defs)) {
					st.setCpuStones(st.getCpuStones() - 2);
					int picked = 0;
					for (int i = st.getCpuRest().size() - 1; i >= 0 && picked < 2; i--) {
						BattleCard c = st.getCpuRest().get(i);
						if (isFezariaPickableCarbuncleInRest(c, defs)) {
							st.getCpuRest().remove(i);
							st.getCpuHand().add(0, c);
							picked++;
						}
					}
					st.addLog("CPUフェザリア: ストーン2使用、フェザリア以外のカーバンクル" + picked + "枚回収");
				}
			}
			default -> {
			}
		}
	}

	private void swapMainWithWolfIfPaid(ZoneFighter z) {
		if (z == null || z.getMain() == null) return;
		for (int i = 0; i < z.getCostUnder().size(); i++) {
			BattleCard c = z.getCostUnder().get(i);
			if (c != null && c.getCardId() == 21) {
				BattleCard oldMain = z.getMain();
				z.getCostUnder().set(i, oldMain);
				z.setMain(c);
				return;
			}
		}
	}

	private boolean restContainsCardId(List<BattleCard> rest, short cardId) {
		for (BattleCard c : rest) {
			if (c != null && c.getCardId() == cardId) return true;
		}
		return false;
	}

	private void moveOneCardIdToDeckBottom(List<BattleCard> rest, List<BattleCard> deck, short cardId) {
		for (int i = 0; i < rest.size(); i++) {
			if (rest.get(i).getCardId() == cardId) {
				BattleCard c = rest.remove(i);
				deck.add(c); // bottom
				return;
			}
		}
	}

	private int countAttributeInRest(List<BattleCard> rest, Map<Short, CardDefinition> defs, String attr) {
		int n = 0;
		for (BattleCard c : rest) {
			if (CardAttributes.hasAttribute(defs.get(c.getCardId()), attr)) n++;
		}
		return n;
	}

	/** ネムリィ: 自分のレストのカーバンクル1枚につきコスト-1（最低0）。炭鉱夫で無効化されたカードは割引なし。研究者アストリア: 手札補正。 */
	private int effectiveDeployCost(CardDefinition d, BattleCard deployedMain, Map<Short, CardDefinition> defs,
			List<BattleCard> ownersRestForDiscount, int mechanicExtraCost) {
		if (d == null) {
			return 0;
		}
		int base = d.getCost() != null ? d.getCost() : 0;
		int handAdj = deployedMain != null ? deployedMain.getHandDeployCostModifier() : 0;
		if (deployedMain != null && deployedMain.isBlankEffects()) {
			return Math.max(0, base + mechanicExtraCost + handAdj);
		}
		int core;
		if (d.getId() == null || d.getId() != NEMURY_ID) {
			core = base;
		} else {
			int disc = countAttributeInRest(ownersRestForDiscount, defs, "CARBUNCLE");
			core = Math.max(0, base - disc);
		}
		return Math.max(0, core + mechanicExtraCost + handAdj);
	}

	/** 〈宝石の地〉: 場にいる間、カーバンクル・ファイターは強さ+2（両プレイヤー・竜王の〈常時〉無効化後も適用） */
	private int fieldGloriaCarbunclePowerBonus(CpuBattleState st, CardDefinition fighterDef, Map<Short, CardDefinition> defs) {
		if (st == null || fighterDef == null || defs == null) {
			return 0;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != FIELD_GLORIA_ID) {
			return 0;
		}
		if (!CardAttributes.hasAttribute(fighterDef, "CARBUNCLE")) {
			return 0;
		}
		return 2;
	}

	/**
	 * 〈探鉱の洞窟〉: カーバンクル・ファイターを配置したプレイヤーはストーン+1（両者・配置確定時）。
	 */
	private void applyFieldNebulaWhenCarbuncleFighterDeployed(CpuBattleState st, CardDefinition mainDef, boolean deployerIsHuman) {
		if (st == null || mainDef == null) {
			return;
		}
		if (isFieldCard(mainDef)) {
			return;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != FIELD_NEBULA_ID) {
			return;
		}
		if (!CardAttributes.hasAttribute(mainDef, "CARBUNCLE")) {
			return;
		}
		if (deployerIsHuman) {
			st.setHumanStones(st.getHumanStones() + 1);
			st.addLog("ネビュラ坑道: ストーン+1");
			if (st.getHumanBattle() != null) {
				st.getHumanBattle().setFieldNebulaStoneGrantedForThisDeploy(true);
			}
		} else {
			st.setCpuStones(st.getCpuStones() + 1);
			st.addLog("ネビュラ坑道: ストーン+1");
			if (st.getCpuBattle() != null) {
				st.getCpuBattle().setFieldNebulaStoneGrantedForThisDeploy(true);
			}
		}
	}

	/** CPU の簡易処理用（プレイヤーのレベルアップ捨てルールとは無関係）。 */
	private void discardRightmost(List<BattleCard> hand, List<BattleCard> rest) {
		if (!hand.isEmpty()) {
			rest.add(hand.remove(hand.size() - 1));
		}
	}

	private List<List<String>> cpuDiscardPlans(List<BattleCard> hand, int k) {
		List<BattleCard> h = hand != null ? hand : List.of();
		int n = h.size();
		if (k <= 0) return List.of(List.of());
		if (k > n) return List.of();

		List<List<String>> out = new ArrayList<>();
		// n <= 4 の想定なのでビット全探索で十分
		int maxMask = 1 << n;
		for (int mask = 0; mask < maxMask; mask++) {
			if (Integer.bitCount(mask) != k) continue;
			List<String> ids = new ArrayList<>(k);
			for (int i = 0; i < n; i++) {
				if (((mask >> i) & 1) == 1) {
					ids.add(h.get(i).getInstanceId());
				}
			}
			out.add(ids);
		}
		return out;
	}

	/**
	 * 手番側が、相手バトルゾーンの強さ以上になる配置（レベルアップ・コスト支払い・配置効果のシミュレーション込み）が存在するか。
	 */
	public boolean canMakeLegalDeploy(CpuBattleState st, boolean forHuman, Map<Short, CardDefinition> defs) {
		ZoneFighter oppZone = forHuman ? st.getCpuBattle() : st.getHumanBattle();
		if (oppZone == null || oppZone.getMain() == null) {
			return true; // 相手バトルゾーンが空なら、そもそも「出せないと負け」の条件にならない
		}

		int oppEff = effectiveBattlePower(oppZone, !forHuman, st, defs);
		List<BattleCard> hand = forHuman ? st.getHumanHand() : st.getCpuHand();
		int stones = forHuman ? st.getHumanStones() : st.getCpuStones();

		// CPU先攻の初手はレベルアップ不可（{@link #cpuTurn} と同じ）
		int maxLuRest = hand.size();
		int maxLuSt = stones;
		if (!forHuman) {
			boolean cpuIsFirstPlayer = !st.isHumanGoesFirst();
			boolean cpuIsFirstTurnAsFirstPlayer = cpuIsFirstPlayer && st.getCpuTurnStarts() == 1;
			if (cpuIsFirstTurnAsFirstPlayer) {
				maxLuRest = 0;
				maxLuSt = 0;
			}
		}

		// 手札最大4枚想定なので全探索で十分。
		// レベルアップ捨ては任意のカードの組み合わせを考慮する（手札の並びに依存しない）。
		for (int luRest = 0; luRest <= maxLuRest; luRest++) {
			List<List<String>> discardPlans = cpuDiscardPlans(hand, luRest);
			for (int luSt = 0; luSt <= maxLuSt; luSt++) {
				for (List<String> discIds : discardPlans) {
					CpuBattleState sim = copyState(st);
					if (forHuman) {
						sim.setHumanStones(sim.getHumanStones() - luSt);
						for (String did : discIds) {
							BattleCard dc = removeByInstanceId(sim.getHumanHand(), did);
							if (dc != null) {
								sim.getHumanRest().add(dc);
							}
						}
					} else {
						sim.setCpuStones(sim.getCpuStones() - luSt);
						for (String did : discIds) {
							BattleCard dc = removeByInstanceId(sim.getCpuHand(), did);
							if (dc != null) {
								sim.getCpuRest().add(dc);
							}
						}
					}

					List<BattleCard> simHand = forHuman ? sim.getHumanHand() : sim.getCpuHand();
					int simStones = forHuman ? sim.getHumanStones() : sim.getCpuStones();
					if (simHand.size() != hand.size() - luRest) {
						continue;
					}

					for (BattleCard main : new ArrayList<>(simHand)) {
						CardDefinition mainDef = defs.get(main.getCardId());
						if (mainDef == null) continue;
						List<BattleCard> discountRest = forHuman ? st.getHumanRest() : st.getCpuRest();
						int mechStacks = forHuman ? sim.getHumanNextMechanicStacks() : sim.getCpuNextMechanicStacks();
						int cost = effectiveDeployCost(mainDef, main, defs, discountRest, mechStacks);

						for (int payStone = 0; payStone <= Math.min(cost, simStones); payStone++) {
							int needCards = cost - payStone;
							if (simHand.size() - 1 < needCards) continue;

							List<BattleCard> others = new ArrayList<>();
							for (BattleCard c : simHand) {
								if (!c.getInstanceId().equals(main.getInstanceId())) others.add(c);
							}
							if (others.size() < needCards) continue;

							int maxMask = 1 << others.size();
							for (int mask = 0; mask < maxMask; mask++) {
								if (Integer.bitCount(mask) != needCards) continue;

								CpuBattleState sim2 = copyState(sim);
								List<BattleCard> sim2Hand = forHuman ? sim2.getHumanHand() : sim2.getCpuHand();

								BattleCard pickedMain = removeByInstanceId(sim2Hand, main.getInstanceId());
								if (pickedMain == null) continue;

								if (forHuman) sim2.setHumanStones(sim2.getHumanStones() - payStone);
								else sim2.setCpuStones(sim2.getCpuStones() - payStone);

								List<BattleCard> paid = new ArrayList<>();
								for (int i = 0; i < others.size(); i++) {
									if (((mask >> i) & 1) == 1) {
										BattleCard p = removeByInstanceId(sim2Hand, others.get(i).getInstanceId());
										if (p != null) paid.add(p);
									}
								}
								if (paid.size() != needCards) continue;

								int deployBonus = (luRest * 2) + (luSt * 2);
								deployBonus += 3 * mechStacks;
								if (forHuman) sim2.setHumanNextMechanicStacks(0);
								else sim2.setCpuNextMechanicStacks(0);
								ZoneFighter z = new ZoneFighter();
								z.setMain(pickedMain);
								z.setCostUnder(paid);
								z.setCostPayCardCount(needCards);
								z.setTemporaryPowerBonus(deployBonus);
								retireOwnBattleZoneBeforeNewDeploy(sim2, forHuman, false);
								if (forHuman) sim2.setHumanBattle(z);
								else sim2.setCpuBattle(z);

								// 配置能力反映
								if (forHuman) applyDeployHuman(sim2, mainDef, defs, pickedMain);
								else applyDeployCpu(sim2, mainDef, defs, new Random(31_337L), pickedMain);

								int eff = effectiveBattlePower(forHuman ? sim2.getHumanBattle() : sim2.getCpuBattle(), forHuman, sim2, defs);
								if (eff >= oppEff) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
}
