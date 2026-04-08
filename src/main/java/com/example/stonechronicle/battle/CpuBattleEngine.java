package com.example.stonechronicle.battle;

import com.example.stonechronicle.card.CardAttributes;
import com.example.stonechronicle.domain.CardDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

	public CpuBattleState newBattle(List<Short> humanDeckCardIds, int cpuLevel, Random rnd,
			Map<Short, CardDefinition> defs) {
		var st = new CpuBattleState();
		st.setCpuLevel(cpuLevel);
		st.setHumanGoesFirst(rnd.nextBoolean());
		st.setHumansTurn(st.isHumanGoesFirst());
		st.setHumanStones(0);
		st.setCpuStones(0);

		st.setHumanDeck(buildShuffledInstances(humanDeckCardIds, rnd));
		st.setCpuDeck(buildShuffledInstances(buildCpuDeckIds(rnd), rnd));

		for (int i = 0; i < 4; i++) {
			drawOne(st.getHumanDeck(), st.getHumanHand());
			drawOne(st.getCpuDeck(), st.getCpuHand());
		}

		st.addLog(st.isHumanGoesFirst() ? "先攻: あなた" : "先攻: CPU");
		st.setLastMessage("バトル開始");
		return st;
	}

	private List<Short> buildCpuDeckIds(Random rnd) {
		List<Short> picked = new ArrayList<>();
		Map<Short, Integer> cnt = new HashMap<>();
		while (picked.size() < 8) {
			short id = (short) (1 + rnd.nextInt(30));
			if (cnt.getOrDefault(id, 0) >= 2) {
				continue;
			}
			picked.add(id);
			cnt.put(id, cnt.getOrDefault(id, 0) + 1);
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

	public void humanTurn(CpuBattleState st, int levelUpRest, int levelUpStones, boolean deploy, int deployHandIndex,
			Map<Short, CardDefinition> defs) {
		if (st.isGameOver() || !st.isHumansTurn()) {
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

		List<BattleCard> simHand = new ArrayList<>(st.getHumanHand());
		for (int i = 0; i < levelUpRest; i++) {
			simHand.remove(simHand.size() - 1);
		}
		int deployBonus = 0;
		if (deploy) {
			if (deployHandIndex < 0 || deployHandIndex >= simHand.size()) {
				st.setLastMessage("手札の指定が不正です（レベルアップ後の位置で指定してください）");
				return;
			}
			BattleCard main = simHand.get(deployHandIndex);
			CardDefinition mainDef = defs.get(main.getCardId());
			int perRest = "SHOKIN".equals(mainDef.getAbilityDeployCode()) ? 3 : 2;
			deployBonus = levelUpRest * perRest + levelUpStones * 2;
			if (!canDeployWithHand(simHand, deployHandIndex, defs, deployBonus, st, true)) {
				st.setLastMessage("配置条件（強さ・コスト）を満たせません");
				return;
			}
		}

		st.setHumanStones(st.getHumanStones() - levelUpStones);
		for (int i = 0; i < levelUpRest; i++) {
			BattleCard c = st.getHumanHand().remove(st.getHumanHand().size() - 1);
			st.getHumanRest().add(c);
		}

		if (deploy) {
			BattleCard main = st.getHumanHand().remove(deployHandIndex);
			CardDefinition mainDef = defs.get(main.getCardId());
			int cost = mainDef.getCost();
			List<BattleCard> paid = new ArrayList<>();
			for (int i = 0; i < cost; i++) {
				paid.add(st.getHumanHand().remove(st.getHumanHand().size() - 1));
			}
			ZoneFighter z = new ZoneFighter();
			z.setMain(main);
			z.setCostUnder(paid);
			z.setTemporaryPowerBonus(deployBonus);
			st.setHumanBattle(z);
			applyDeployHuman(st, mainDef, defs);
			st.addLog("あなたは「" + mainDef.getName() + "」を配置した");
		} else {
			st.addLog("あなたは配置をスキップした");
		}

		resolveKnockAndDraw(st, true, defs);
		resetTurnBuffs(st);
		st.setHumansTurn(false);
		st.setLastMessage("CPUのターン");
	}

	/**
	 * クリックUI向け: 手札の instanceId で配置カード・支払いカードを指定し、配置コストは「カード/ストーン/分割」で支払える。
	 * levelUpRest は右端からレストへ捨てる枚数、levelUpStones は強化回数（1回=+2、ストーン1消費）。
	 */
	public void humanTurnInteractive(CpuBattleState st, int levelUpRest, int levelUpStones,
			String deployInstanceId, int payCostStones, List<String> payCostCardInstanceIds,
			Map<Short, CardDefinition> defs) {
		if (st == null || st.isGameOver() || !st.isHumansTurn()) {
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

		// シミュレーション（レベルアップ後の手札）
		List<BattleCard> simHand = new ArrayList<>(st.getHumanHand());
		for (int i = 0; i < levelUpRest; i++) {
			simHand.remove(simHand.size() - 1);
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
			mainDef = defs.get(simMain.getCardId());
			if (mainDef == null) {
				st.setLastMessage("カード定義が見つかりません");
				return;
			}
			int perRest = "SHOKIN".equals(mainDef.getAbilityDeployCode()) ? 3 : 2;
			deployBonus = levelUpRest * perRest + levelUpStones * 2;
			cost = mainDef.getCost();
			simHand.remove(simMain);

			// 支払いチェック
			List<String> payIds = payCostCardInstanceIds != null ? payCostCardInstanceIds : List.of();
			long distinct = payIds.stream().distinct().count();
			if (distinct != payIds.size()) {
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

			// 強さ条件
			int eff = mainDef.getBasePower() + deployBonus;
			if (st.getCpuBattle() != null) {
				int opp = effectiveBattlePower(st.getCpuBattle(), false, st, defs);
				if (eff < opp) {
					st.setLastMessage("配置条件（強さ）を満たせません");
					return;
				}
			}
		}

		// ここから確定適用
		st.setHumanStones(st.getHumanStones() - levelUpStones);
		for (int i = 0; i < levelUpRest; i++) {
			BattleCard c = st.getHumanHand().remove(st.getHumanHand().size() - 1);
			st.getHumanRest().add(c);
		}

		if (simMain != null && mainDef != null) {
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

			ZoneFighter z = new ZoneFighter();
			z.setMain(main);
			z.setCostUnder(paid);
			z.setTemporaryPowerBonus(deployBonus);
			st.setHumanBattle(z);
			applyDeployHuman(st, mainDef, defs);
			st.addLog("あなたは「" + mainDef.getName() + "」を配置した");
		} else {
			st.addLog("あなたは配置をスキップした");
		}

		resolveKnockAndDraw(st, true, defs);
		resetTurnBuffs(st);
		st.setHumansTurn(false);
		st.setLastMessage("CPUのターン");
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
		int cost = d.getCost();
		if (hand.size() - 1 < cost) {
			return false;
		}
		int eff = d.getBasePower() + deployBonus;
		if (human && st.getCpuBattle() != null) {
			int opp = effectiveBattlePower(st.getCpuBattle(), false, st, defs);
			return eff >= opp;
		}
		if (!human && st.getHumanBattle() != null) {
			int opp = effectiveBattlePower(st.getHumanBattle(), true, st, defs);
			return eff >= opp;
		}
		return true;
	}

	private boolean canPayCostWithHand(List<BattleCard> hand, String mainInstanceId, Map<Short, CardDefinition> defs) {
		if (mainInstanceId == null) return false;
		BattleCard main = null;
		for (BattleCard c : hand) {
			if (mainInstanceId.equals(c.getInstanceId())) {
				main = c;
				break;
			}
		}
		if (main == null) return false;
		CardDefinition d = defs.get(main.getCardId());
		if (d == null) return false;
		int cost = d.getCost();
		return hand.size() - 1 >= cost;
	}

	private BattleCard copyCard(BattleCard c) {
		if (c == null) return null;
		return new BattleCard(c.getInstanceId(), c.getCardId());
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
		return nz;
	}

	private CpuBattleState copyStateForCpuSim(CpuBattleState st) {
		CpuBattleState ns = new CpuBattleState();
		ns.setCpuLevel(st.getCpuLevel());
		ns.setHumanGoesFirst(st.isHumanGoesFirst());
		ns.setHumansTurn(st.isHumansTurn());
		ns.setHumanTurnStarts(st.getHumanTurnStarts());
		ns.setHumanStones(st.getHumanStones());
		ns.setCpuStones(st.getCpuStones());
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
		return ns;
	}

	public void cpuTurn(CpuBattleState st, Map<Short, CardDefinition> defs, Random rnd) {
		if (st.isGameOver() || st.isHumansTurn()) {
			return;
		}

		// CPU は手札を吟味し、配置効果・常時効果・複数回レベルアップを考慮して
		// 相手バトルゾーン以上になれるなら必ず配置する。
		String bestInstanceId = null;
		int bestLevelUpRest = 0;
		int bestLevelUpStones = 0;
		int bestDeployBonus = 0;
		int bestScore = Integer.MIN_VALUE;
		int bestCpuEff = -1;

		int maxRest = st.getCpuHand().size();
		int maxStones = Math.max(0, st.getCpuStones());

		for (int levelUpRest = 0; levelUpRest <= maxRest; levelUpRest++) {
			for (int levelUpStones = 0; levelUpStones <= maxStones; levelUpStones++) {
				for (BattleCard main : st.getCpuHand()) {
					CardDefinition mainDef = defs.get(main.getCardId());
					if (mainDef == null) continue;

					int perRest = "SHOKIN".equals(mainDef.getAbilityDeployCode()) ? 3 : 2;
					int deployBonus = levelUpRest * perRest + levelUpStones * 2;

					// シミュレーション：レベルアップ→配置→配置効果→常時計算（effectiveBattlePower）
					CpuBattleState simSt = copyStateForCpuSim(st);
					simSt.setHumansTurn(false);
					simSt.setGameOver(false);

					if (levelUpStones > simSt.getCpuStones()) continue;
					simSt.setCpuStones(simSt.getCpuStones() - levelUpStones);
					for (int i = 0; i < levelUpRest; i++) {
						discardRightmost(simSt.getCpuHand(), simSt.getCpuRest());
					}

					if (!canPayCostWithHand(simSt.getCpuHand(), main.getInstanceId(), defs)) {
						continue;
					}

					// 配置カードを取り出す
					BattleCard simMain = removeByInstanceId(simSt.getCpuHand(), main.getInstanceId());
					if (simMain == null) continue;
					int cost = mainDef.getCost();
					List<BattleCard> paid = new ArrayList<>();
					for (int i = 0; i < cost; i++) {
						if (simSt.getCpuHand().isEmpty()) break;
						paid.add(simSt.getCpuHand().remove(simSt.getCpuHand().size() - 1));
					}
					ZoneFighter z = new ZoneFighter();
					z.setMain(simMain);
					z.setCostUnder(paid);
					z.setTemporaryPowerBonus(deployBonus);
					simSt.setCpuBattle(z);

					Random simRnd = new Random(31_337L ^ main.getCardId() ^ (levelUpRest * 31L) ^ (levelUpStones * 131L));
					applyDeployCpu(simSt, mainDef, defs, simRnd);

					int cpuEff = effectiveBattlePower(simSt.getCpuBattle(), false, simSt, defs);
					int oppEff = effectiveBattlePower(simSt.getHumanBattle(), true, simSt, defs);
					if (simSt.getHumanBattle() != null && cpuEff < oppEff) {
						continue;
					}

					int score = cpuEff - oppEff;
					if (score > bestScore || (score == bestScore && cpuEff > bestCpuEff)) {
						bestScore = score;
						bestCpuEff = cpuEff;
						bestInstanceId = main.getInstanceId();
						bestLevelUpRest = levelUpRest;
						bestLevelUpStones = levelUpStones;
						bestDeployBonus = deployBonus;
					}
				}
			}
		}

		boolean deployed = false;
		if (bestInstanceId != null) {
			// レベルアップ確定（右端からレストへ）
			if (bestLevelUpStones <= st.getCpuStones()) {
				st.setCpuStones(st.getCpuStones() - bestLevelUpStones);
			}
			for (int i = 0; i < bestLevelUpRest; i++) {
				discardRightmost(st.getCpuHand(), st.getCpuRest());
			}

			if (canPayCostWithHand(st.getCpuHand(), bestInstanceId, defs)) {
				BattleCard main = removeByInstanceId(st.getCpuHand(), bestInstanceId);
				if (main != null) {
					CardDefinition mainDef = defs.get(main.getCardId());
					if (mainDef != null) {
						int cost = mainDef.getCost();
						List<BattleCard> paid = new ArrayList<>();
						for (int i = 0; i < cost; i++) {
							if (st.getCpuHand().isEmpty()) break;
							paid.add(st.getCpuHand().remove(st.getCpuHand().size() - 1));
						}
						ZoneFighter z = new ZoneFighter();
						z.setMain(main);
						z.setCostUnder(paid);
						z.setTemporaryPowerBonus(bestDeployBonus);
						st.setCpuBattle(z);
						applyDeployCpu(st, mainDef, defs, rnd);
						st.addLog("CPUは「" + mainDef.getName() + "」を配置した");
						deployed = true;
					}
				}
			}
		}

		if (!deployed) {
			st.addLog("CPUは配置しなかった");
		}
		resolveKnockAndDraw(st, false, defs);
		resetTurnBuffs(st);
		st.setHumansTurn(true);
		st.setLastMessage("あなたのターン");
	}

	private void resolveKnockAndDraw(CpuBattleState st, boolean humanWasActing, Map<Short, CardDefinition> defs) {
		if (humanWasActing) {
			if (st.getCpuBattle() != null) {
				moveZoneToRest(st.getCpuBattle(), st.getCpuRest());
				st.setCpuBattle(null);
				st.setCpuStones(st.getCpuStones() + 1);
				st.addLog("相手ファイターをレストへ。CPUはストーンを1つ得た");
			}
			while (st.getHumanHand().size() < 4 && !st.getHumanDeck().isEmpty()) {
				drawOne(st.getHumanDeck(), st.getHumanHand());
			}
		} else {
			if (st.getHumanBattle() != null) {
				moveZoneToRest(st.getHumanBattle(), st.getHumanRest());
				st.setHumanBattle(null);
				st.setHumanStones(st.getHumanStones() + 1);
				st.addLog("あなたのファイターがレストへ。ストーンを1つ得た");
			}
			while (st.getCpuHand().size() < 4 && !st.getCpuDeck().isEmpty()) {
				drawOne(st.getCpuDeck(), st.getCpuHand());
			}
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

	private void resetTurnBuffs(CpuBattleState st) {
		if (st.getHumanBattle() != null) {
			st.getHumanBattle().setTemporaryPowerBonus(0);
		}
		if (st.getCpuBattle() != null) {
			st.getCpuBattle().setTemporaryPowerBonus(0);
		}
	}

	public int effectiveBattlePower(ZoneFighter zf, boolean ownerIsHuman, CpuBattleState st,
			Map<Short, CardDefinition> defs) {
		if (zf == null || zf.getMain() == null) {
			return 0;
		}
		short id = zf.getMain().getCardId();
		CardDefinition d = defs.get(id);
		int p = d.getBasePower() + zf.getTemporaryPowerBonus();

		boolean suppress = ownerIsHuman
				? hasRyuoh(st.getCpuBattle())
				: hasRyuoh(st.getHumanBattle());
		if (suppress) {
			return p;
		}

		if (id == KUSURI_ID && ownerIsHuman) {
			p -= st.getHumanStones();
		}
		if (id == KUSURI_ID && !ownerIsHuman) {
			p -= st.getCpuStones();
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
				if (CardAttributes.hasAttribute(defs.get(c.getCardId()), "UNDEAD")) {
					undead++;
				}
			}
			p += undead;
		}

		return Math.max(0, p);
	}

	private boolean hasRyuoh(ZoneFighter z) {
		return z != null && z.getMain() != null && z.getMain().getCardId() == RYUOH_ID;
	}

	private boolean restContainsAttribute(List<BattleCard> rest, Map<Short, CardDefinition> defs, String attr) {
		for (BattleCard c : rest) {
			if (CardAttributes.hasAttribute(defs.get(c.getCardId()), attr)) {
				return true;
			}
		}
		return false;
	}

	private void applyDeployHuman(CpuBattleState st, CardDefinition d, Map<Short, CardDefinition> defs) {
		String code = d.getAbilityDeployCode();
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
				if (!st.getCpuHand().isEmpty()) {
					int r = new Random().nextInt(st.getCpuHand().size());
					st.getCpuRest().add(st.getCpuHand().remove(r));
					st.addLog("サムライ: CPUの手札を1枚レストへ");
				}
			}
			case "KENTOSHI" -> {
				discardRightmost(st.getHumanHand(), st.getHumanRest());
				discardRightmost(st.getCpuHand(), st.getCpuRest());
				st.addLog("剣闘士: お互い手札を1枚レストへ");
			}
			case "KARYUDO" -> {
				if (!st.getCpuHand().isEmpty()) {
					int r = new Random().nextInt(st.getCpuHand().size());
					BattleCard c = st.getCpuHand().remove(r);
					st.getCpuDeck().add(0, c);
					st.addLog("狩人: CPU手札をデッキ上へ");
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
					st.addLog(d.getName() + ": CPUがストーンを1つ捨てた");
				}
			}
			case "KAZE_MAJIN" -> {
				st.setHumanStones(st.getHumanStones() + 2);
				st.addLog(d.getName() + ": ストーン+2");
			}
			default -> {
			}
		}
	}

	private void applyDeployCpu(CpuBattleState st, CardDefinition d, Map<Short, CardDefinition> defs, Random rnd) {
		String code = d.getAbilityDeployCode();
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
				if (!st.getHumanHand().isEmpty()) {
					int r = rnd.nextInt(st.getHumanHand().size());
					st.getHumanRest().add(st.getHumanHand().remove(r));
					st.addLog("CPUサムライ: あなたの手札を1枚レストへ");
				}
			}
			case "KENTOSHI" -> {
				discardRightmost(st.getHumanHand(), st.getHumanRest());
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
			case "GURIFON" -> {
				if (st.getHumanStones() > 0) {
					st.setHumanStones(st.getHumanStones() - 1);
				}
			}
			case "KAZE_MAJIN" -> {
				st.setCpuStones(st.getCpuStones() + 2);
			}
			default -> {
			}
		}
	}

	private void discardRightmost(List<BattleCard> hand, List<BattleCard> rest) {
		if (!hand.isEmpty()) {
			rest.add(hand.remove(hand.size() - 1));
		}
	}
}
