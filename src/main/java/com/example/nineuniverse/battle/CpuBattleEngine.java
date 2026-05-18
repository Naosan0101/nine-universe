package com.example.nineuniverse.battle;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.card.CardAttributes;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.web.dto.BattlePowerModifierDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
	/** アクアガーディアン〈常時〉: 自分レストの「種族：マーフォーク」1枚につき強さ+1 */
	private static final short AQUA_GUARDIAN_ID = 75;
	/** ポセイドン〈配置〉: 自分ターン終了までの強さ加算 */
	private static final int POSEIDON_DEPLOY_TEMPORARY_POWER = 3;
	/** ポセイドン〈配置〉: レストから手札に戻せる「ソードフィッシュ」の上限 */
	private static final int POSEIDON_DEPLOY_MAX_SWORDFISH_FROM_REST = 3;
	/** ミカエル〈配置〉: 発動に必要な手札の「奇跡」枚数 */
	private static final int MIKAEL_DEPLOY_HAND_MIRACLE_MIN = 3;
	/** ミカエル〈配置〉: ミカエルデッキからデッキ上に置く枚数 */
	private static final int MIKAEL_DEPLOY_DECK_TOP_COUNT = 2;
	private static final short GAIKOTSU_ID = 18;
	private static final short SHIREI_ID = 20;
	private static final short HONE_ID = 24;
	private static final short KORYU_ID = 29;
	private static final short SHINY_ID = 31;
	/** インクナイト（id=86） */
	private static final short INK_KNIGHT_ID = GameConstants.INK_KNIGHT_FIGHTER_CARD_ID;
	/** コミックヒーロー（id=91） */
	private static final short COMIC_HERO_ID = GameConstants.COMIC_HERO_FIGHTER_CARD_ID;
	/** エンジェルメイジ（id=105）〈常時〉: 自分レストに「エンジェルメイジ」があると強さ+2 */
	private static final short ANGEL_MAGE_ID = GameConstants.ANGEL_MAGE_FIGHTER_CARD_ID;
	/** ガブリエル（id=104）〈常時〉: 特性コストに「奇跡」が含まれると強さ+1 */
	private static final short GABRIEL_ID = GameConstants.GABRIEL_FIGHTER_CARD_ID;
	/** インクキング（id=111） */
	private static final short INK_KING_ID = GameConstants.INK_KING_FIGHTER_CARD_ID;
	/** キングメーカー（id=90） */
	private static final short KING_MAKER_ID = GameConstants.KING_MAKER_FIGHTER_CARD_ID;
	/** ドミニオン（id=102） */
	private static final short DOMINION_ID = GameConstants.DOMINION_FIGHTER_CARD_ID;
	/** ミニオンソルジャー（id=113） */
	private static final short MINION_SOLDIER_ID = GameConstants.MINION_SOLDIER_TOKEN_CARD_ID;
	/** ミニオンチャンピオン（id=114）〈常時〉相手ターン中+4 */
	private static final short MINION_CHAMPION_ID = GameConstants.MINION_CHAMPION_TOKEN_CARD_ID;
	/** ラミエル（id=99） */
	private static final short RAMIEL_ID = GameConstants.RAMIEL_FIGHTER_CARD_ID;
	private static final String SKETCHER_DEPLOY_CODE = "SKETCHER";
	private static final String COMIC_WITCH_DEPLOY_CODE = "COMIC_WITCH";
	private static final String ZADKIEL_DEPLOY_CODE = "ZADKIEL";
	private static final String CELESTIA_DEPLOY_CODE = "CELESTIA";
	private static final String VIRTUAL_DEPLOY_CODE = "VIRTUAL";
	private static final String SERAPHIM_DEPLOY_CODE = "SERAPHIM";
	private static final String DOMINION_DEPLOY_CODE = "DOMINION";
	private static final String MINION_SOLDIER_DEPLOY_CODE = "MINION_SOLDIER";
	private static final String RAMIEL_DEPLOY_CODE = "RAMIEL";
	private static final String LUCIFER_DEPLOY_CODE = "LUCIFER";
	private static final String FALLEN_ANGEL_LUCIFER_DEPLOY_CODE = "FALLEN_ANGEL_LUCIFER";
	/** ザドキエル: 「奇跡」を置いた後の次配置ファイターに付与する、相手ターン中の強さ加算 */
	private static final int ZADKIEL_OPPONENT_TURN_POWER_BONUS = 3;
	/** インクキング〈配置〉: 自分ターン終了までの強さ加算 */
	private static final int INK_KING_DEPLOY_TEMPORARY_POWER = 4;
	/** ミニオンソルジャー〈配置〉: 自分ターン終了までの強さ加算 */
	private static final int MINION_SOLDIER_DEPLOY_TEMPORARY_POWER = 3;
	/** ミニオンチャンピオン〈常時〉: 相手ターン中の強さ加算 */
	private static final int MINION_CHAMPION_OPPONENT_TURN_POWER_BONUS = 4;
	private static final short FROSTKRUL_ID = 32;
	private static final short MISTYINKUL_ID = 33;
	/** ワイバーン: ミスティンクルと同様、相手の〈配置〉のみ封じる（〈常時〉は対象外） */
	private static final short WYVERN_ID = 80;
	private static final short NEMURY_ID = 40;
	/** ネムリィ〈常時〉: 相手ターンの間に加算する強さ */
	private static final int NEMURY_OPPONENT_TURN_POWER_BONUS = 4;
	/** フェザリア（レスト回収で自分自身は選べない） */
	private static final short FEATHERIA_ID = 38;
	/** ノクスクル（id=37・能力コード STONIA）。〈配置〉相当の強さ加算（自分のターンの終わりまで所持ストーンぶん） */
	private static final short STONIA_ID = 37;
	/** 宝石の地 グロリア輝石台地 */
	private static final short FIELD_GLORIA_ID = 41;
	/** 探鉱の洞窟 ネビュラ坑道 */
	private static final short FIELD_NEBULA_ID = 42;
	/** クリスタクル（id=35） */
	private static final short CRYSTAKUL_ID = 35;
	/** ボットバイク（id=57） */
	private static final short BOT_BIKE_ID = 57;
	/** レッドアイ（id=58） */
	private static final short RED_EYE_ID = 58;
	/** ガラクタレッグ（id=61）: 相手のファイターは〈常時〉が使えない */
	private static final short GARAKUTA_LEG_ID = 61;
	/** 忍者（id=47）: 入れ替え後メインの強さペナルティ */
	private static final short NINJA_ID = 47;
	private static final int NINJA_SWAP_POWER_PENALTY = 2;
	/** SPEC-777（id=53） */
	private static final short SPEC_777_ID = 53;
	/** SPEC-666（id=54） */
	private static final short SPEC_666_ID = 54;
	/** SPEC-123（id=55） */
	private static final short SPEC_123_ID = 55;
	/** SPEC-1（id=56・旧名 SPEC-0） */
	private static final short SPEC_1_ID = 56;
	/** 森のハープ弾き（id=65） */
	private static final short HARP_PLAYER_ID = 65;
	/** 森のハープ弾き: 次のエルフ配置に与える強さ（ターン終了まで） */
	private static final int HARP_NEXT_ELF_POWER_BONUS = 3;
	/** 墓守神父（id=67） */
	private static final short GRAVE_PRIEST_ID = 67;
	/** 墓守神父: 選択した手札ファイターに付与する配置コスト補正（バトル終了まで） */
	private static final int GRAVE_PRIEST_HAND_COST_REDUCTION = 2;
	/** マーメイド〈配置〉: 手札の各「ソードフィッシュ」に付与する強さ（バトル終了まで） */
	private static final int MERMAID_SWORDFISH_POWER_BONUS_EACH = 2;
	/** 助手（JOSHU）〈配置〉: レストから手札にした「研究者」を名に含むカードの強さ（バトル終了まで） */
	private static final int JOSHU_ASSISTANT_RESEARCHER_BATTLE_END_POWER_BONUS = 2;
	/** 信奉者（id=50） */
	private static final short BELIEVER_ID = 50;
	/** 霊園教会 デスバウンス（id=68・〈フィールド〉） */
	private static final short DEATHBOUNCE_FIELD_ID = 68;
	/** 霊園教会 デスバウンス: 効果が持続するターン数（配置直後からカウントダウン） */
	private static final int DEATHBOUNCE_FIELD_INITIAL_TURNS = 6;
	/** ハーフエルフ（id=51） */
	private static final short HALF_ELF_ID = 51;
	/** 艦隊 HO-IVI-I3（id=62・〈フィールド〉） */
	private static final short FLEET_HO_IVI_FIELD_ID = 62;
	/** 廃棄工場 5C-R4P（id=63・〈フィールド〉） */
	private static final short SCRAPYARD_FIELD_ID = 63;
	/** 武器庫 VV-E4-PON（id=64・〈フィールド〉）: 種族：マシンのファイターはコスト1（〈フィールド〉カード自体は対象外） */
	private static final short WEAPON_DEPOT_FIELD_ID = 64;
	/** 神秘の大樹 スカイア（id=66・〈フィールド〉）: エルフの「カード効果による」ターン跨ぎの強さ加算が相手ターンでも持続（レベルアップ分は除く） */
	private static final short MYSTERIOUS_TREE_SKYAR_FIELD_ID = 66;
	/** 龍鱗海峡 ラグナロク（id=78・〈フィールド〉）: 効果「効果なし。」のファイターは強さ+3 */
	private static final short FIELD_RAGNAROK_STRAIT_ID = 78;
	/** 磁力合体デンジリオン（id=59） */
	private static final short DENZIRION_ID = 59;
	/** 化石（id=79）: バトル→レストで「化石（フィールド）」に変化 */
	private static final short FOSSIL_FIGHTER_ID = 79;
	/** 研究者フローラ（id=85） */
	private static final short RESEARCHER_FLORA_ID = 85;
	/** 漫画家（id=96） */
	private static final short MANGAKA_ID = GameConstants.MANGAKA_FIGHTER_CARD_ID;
	/** コミックダイナソー（id=97） */
	private static final short COMIC_DINOSAUR_ID = GameConstants.COMIC_DINOSAUR_FIGHTER_CARD_ID;
	/** ガラクタアーム（id=60） */
	private static final short GARAKUTA_ARM_ID = 60;
	/** クリスタクル〈配置〉: 任意で支払うストーン数 */
	private static final int CRYSTAKUL_OPTIONAL_STONE_COST = 2;
	/** フェザリア〈配置〉: 任意で支払うストーン数 */
	private static final int FEZARIA_OPTIONAL_STONE_COST = 3;
	/** クリスタクル〈配置〉: 次の配置に与える強さ（次の相手ターン終了まで） */
	private static final int CRYSTAKUL_NEXT_DEPLOY_POWER = 3;
	/** 剣闘士: 双方に手札があるとき、先に捨てる側の選択（相手の続きと連鎖する） */
	private static final String KENTOSHI_PAIR_FIRST_DEPLOY_CODE = "KENTOSHI_PAIR";
	/** 剣闘士: 片側のみ手札があるときのその1枚の選択（連鎖しない） */
	private static final String KENTOSHI_SOLO_DEPLOY_CODE = "KENTOSHI_SOLO";
	/** 剣闘士: 相手側の手札1枚選択（{@link #KENTOSHI_PAIR_FIRST_DEPLOY_CODE} の直後のみ） */
	private static final String KENTOSHI_OPPONENT_FOLLOWUP_DEPLOY_CODE = "KENTOSHI_OPP";

	/**
	 * レベルアップでレストへ捨てられるカード枚数の上限（配置するファイターを手札に残すため、手札枚数−1）。
	 */
	private static int maxLevelUpRestDiscard(int handSize) {
		return Math.max(0, handSize - 1);
	}

	/**
	 * 〈配置〉コードが DB で欠落していても id=35 ならクリスタクルとして扱う（確認モーダル・ミスティンクル例外のため）
	 */
	private static boolean isCrystakulCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("CRYSTAKUL".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == CRYSTAKUL_ID;
	}

	private static boolean isBotBikeCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("BOT_BIKE".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == BOT_BIKE_ID;
	}

	private static boolean isHarpPlayerCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("HARP_PLAYER".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == HARP_PLAYER_ID;
	}

	private static boolean isGravePriestCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("GRAVE_PRIEST".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == GRAVE_PRIEST_ID;
	}

	private static boolean isBelieverCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("BELIEVER".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == BELIEVER_ID;
	}

	private static boolean isHalfElfCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("HALF_ELF".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == HALF_ELF_ID;
	}

	private static boolean isResearcherFloraCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("RESEARCHER_FLORA".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == RESEARCHER_FLORA_ID;
	}

	private static boolean isMangakaCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("MANGAKA".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == MANGAKA_ID;
	}

	private static boolean isComicDinosaurCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("COMIC_DINOSAUR".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == COMIC_DINOSAUR_ID;
	}

	private static List<String> comicDinosaurPickableHandInstanceIds(CpuBattleState st, List<BattleCard> hand,
			Map<Short, CardDefinition> defs) {
		List<String> out = new ArrayList<>();
		if (st == null || hand == null || defs == null) {
			return out;
		}
		for (BattleCard hc : hand) {
			if (hc == null || hc.getInstanceId() == null) {
				continue;
			}
			CardDefinition hd = defs.get(hc.getCardId());
			if (hd != null && restCardHasTribe(st, hd, hc, "COMIC")) {
				out.add(hc.getInstanceId());
			}
		}
		return out;
	}

	private void finishComicDinosaurDeployFollowUp(CpuBattleState st, boolean ownerHuman,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		List<BattleCard> hand = ownerHuman ? st.getHumanHand() : st.getCpuHand();
		addCopiesOfCardIdToHand(hand, GameConstants.DRAGON_EGG_CARD_ID, 2, defs);
		if (ownerHuman) {
			st.addLog("コミックダイナソー: 「ドラゴンの卵」を2枚手札に加えた");
		} else if (st.isPvp()) {
			st.addLog("コミックダイナソー: 「ドラゴンの卵」を2枚手札に加えた");
		} else {
			st.addLog("CPUコミックダイナソー: 「ドラゴンの卵」を2枚手札に加えた");
		}
	}

	private void beginComicDinosaurDeployHandChoice(CpuBattleState st, boolean forHuman, boolean cpuSlotChooses,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		boolean ownerHuman = forHuman && !cpuSlotChooses;
		List<BattleCard> hand = ownerHuman ? st.getHumanHand() : st.getCpuHand();
		List<String> opts = comicDinosaurPickableHandInstanceIds(st, hand, defs);
		if (!opts.isEmpty()) {
			st.setPendingChoice(new PendingChoice(
					ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
					"コミックダイナソー（手札の「種族：コミック」を1枚レストへ）",
					forHuman,
					"COMIC_DINOSAUR",
					0,
					opts,
					cpuSlotChooses));
		} else {
			if (ownerHuman) {
				st.addLog("コミックダイナソー: 手札に「種族：コミック」がない");
			} else if (st.isPvp()) {
				st.addLog("コミックダイナソー: 手札に「種族：コミック」がない");
			} else {
				st.addLog("CPUコミックダイナソー: 手札に「種族：コミック」がない");
			}
			finishComicDinosaurDeployFollowUp(st, ownerHuman, defs);
		}
	}

	private void applyComicDinosaurDeployForCpu(CpuBattleState st, Map<Short, CardDefinition> defs, Random rnd) {
		if (st == null || defs == null) {
			return;
		}
		List<String> comics = comicDinosaurPickableHandInstanceIds(st, st.getCpuHand(), defs);
		if (!comics.isEmpty()) {
			List<BattleCard> pickPool = new ArrayList<>();
			for (BattleCard hc : st.getCpuHand()) {
				if (hc != null && hc.getInstanceId() != null && comics.contains(hc.getInstanceId())) {
					pickPool.add(hc);
				}
			}
			int ri = rnd != null ? rnd.nextInt(pickPool.size())
					: ThreadLocalRandom.current().nextInt(pickPool.size());
			BattleCard pick = pickPool.get(ri);
			st.getCpuHand().remove(pick);
			st.getCpuRest().add(pick);
		} else {
			st.addLog("CPUコミックダイナソー: 手札に「種族：コミック」がない");
		}
		finishComicDinosaurDeployFollowUp(st, false, defs);
	}

	private static boolean isBehemothCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("BEHEMOTH".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == GameConstants.BEHEMOTH_FIGHTER_CARD_ID;
	}

	private static boolean isFafnirCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("FAFNIR".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == GameConstants.FAFNIR_FIGHTER_CARD_ID;
	}

	private static boolean isBahamutCardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("BAHAMUT".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == GameConstants.BAHAMUT_FIGHTER_CARD_ID;
	}

	/**
	 * 墓守神父: 手札から選べる「種族：アンデッド」のファイター（〈フィールド〉除外）。
	 * 「墓守神父」自身は対象に含めない。
	 */
	private static boolean isGravePriestEligibleHandFighter(CardDefinition d, BattleCard c) {
		if (d == null || c == null) {
			return false;
		}
		if (isGravePriestCardDefinition(d)) {
			return false;
		}
		if (!isNonFieldFighterCardDef(d)) {
			return false;
		}
		return CardAttributes.hasAttribute(d, c, "UNDEAD");
	}

	/**
	 * 墓守神父の対象: アンデッド（墓守神父自身除外）かつ {@link #effectiveDeployCost} が 1 以上の手札ファイター。
	 */
	private List<String> gravePriestUndeadHandOptionIds(List<BattleCard> hand, Map<Short, CardDefinition> defs,
			CpuBattleState st, boolean humanSlot) {
		List<String> opts = new ArrayList<>();
		if (hand == null || defs == null || st == null) {
			return opts;
		}
		List<BattleCard> rest = humanSlot ? st.getHumanRest() : st.getCpuRest();
		int mech = humanSlot ? st.getHumanNextMechanicStacks() : st.getCpuNextMechanicStacks();
		for (BattleCard bc : hand) {
			CardDefinition cd = defs.get(bc.getCardId());
			if (!isGravePriestEligibleHandFighter(cd, bc)) {
				continue;
			}
			if (effectiveDeployCost(cd, bc, defs, rest, mech, st) <= 0) {
				continue;
			}
			opts.add(bc.getInstanceId());
		}
		return opts;
	}

	/**
	 * 信奉者: レストの「霊園教会 デスバウンス」（id=68）をすべて手札先頭へ。移動枚数を返す。
	 */
	private static int moveAllDeathbounceFromRestToHand(List<BattleCard> rest, List<BattleCard> hand) {
		if (rest == null || hand == null) {
			return 0;
		}
		int n = 0;
		for (int i = rest.size() - 1; i >= 0; i--) {
			BattleCard c = rest.get(i);
			if (c != null && c.getCardId() == DEATHBOUNCE_FIELD_ID) {
				rest.remove(i);
				hand.add(0, c);
				n++;
			}
		}
		return n;
	}

	/** {@link ZoneFighter#getCostUnder()} の先頭 {@link ZoneFighter#getCostPayCardCount()} 枚に指定カードがあるか */
	private static boolean zoneCharacteristicCostContainsCardId(ZoneFighter zf, short cardId) {
		if (zf == null) {
			return false;
		}
		int n = zf.getCostPayCardCount();
		List<BattleCard> under = zf.getCostUnder();
		if (under == null || n <= 0) {
			return false;
		}
		int limit = Math.min(n, under.size());
		for (int i = 0; i < limit; i++) {
			BattleCard c = under.get(i);
			if (c != null && c.getCardId() == cardId) {
				return true;
			}
		}
		return false;
	}

	/** {@code costUnder} の先頭 {@code costPayCardCount} 枚にメカニックが含まれるか */
	private static boolean zoneCostIncludesMechanicFighter(ZoneFighter z, Map<Short, CardDefinition> defs) {
		if (z == null || defs == null) {
			return false;
		}
		List<BattleCard> under = z.getCostUnder();
		if (under == null || under.isEmpty()) {
			return false;
		}
		int n = Math.min(z.getCostPayCardCount(), under.size());
		for (int i = 0; i < n; i++) {
			BattleCard c = under.get(i);
			if (c == null) {
				continue;
			}
			CardDefinition cd = defs.get(c.getCardId());
			if (cd != null && "MECHANIC".equals(cd.getAbilityDeployCode())) {
				return true;
			}
		}
		return false;
	}

	/** 決戦の地 カムイ（〈フィールド〉） */
	private static final short FIELD_KAMUI_ID = 49;
	private static final short ARTHUR_ID = 43;

	public CpuBattleState newBattle(List<Short> humanDeckCardIds, int cpuLevel, Random rnd,
			Map<Short, CardDefinition> defs) {
		return newBattle(humanDeckCardIds, cpuLevel, CpuBattleMode.ORIGIN, rnd, defs);
	}

	public CpuBattleState newBattle(List<Short> humanDeckCardIds, int cpuLevel, CpuBattleMode cpuBattleMode, Random rnd,
			Map<Short, CardDefinition> defs) {
		var st = new CpuBattleState();
		CpuBattleMode mode = cpuBattleMode != null ? cpuBattleMode : CpuBattleMode.ORIGIN;
		st.setCpuBattleMode(mode);
		st.setCpuLevel(cpuLevel);
		st.setHumanGoesFirst(rnd.nextBoolean());
		st.setHumansTurn(st.isHumanGoesFirst());
		st.setHumanStones(0);
		st.setCpuStones(0);
		st.setHumanTurnStarts(0);
		st.setCpuTurnStarts(0);

		st.setHumanDeck(buildShuffledInstances(humanDeckCardIds, rnd));
		st.setCpuDeck(buildShuffledInstances(
				mode == CpuBattleMode.ADVANCED ? buildCpuDeckIdsAdvanced(cpuLevel, rnd, defs) : buildCpuDeckIds(cpuLevel, rnd, defs),
				rnd));

		for (int i = 0; i < 4; i++) {
			drawOne(st.getHumanDeck(), st.getHumanHand());
			drawOne(st.getCpuDeck(), st.getCpuHand());
		}

		st.addLog(st.isHumanGoesFirst() ? "先攻: あなた" : "先攻: CPU");
		// ターン開始時点でストーン付与（先攻1ターン目のみ獲得なし）
		beginTurnGainStone(st, st.isHumansTurn(), defs);
		captureWorldRebuildOpenLayout(st);
		st.setLastMessage("バトル開始");
		return st;
	}

	/**
	 * CPU リーグ戦など: 人間・CPU 双方のデッキ構成を固定リストから組み立てる（CPU 側は {@link #buildCpuDeckIds} ではなく呼び出し元で決めた 8 枚）。
	 */
	public CpuBattleState newCpuBattleWithFixedCpuDeck(List<Short> humanDeckCardIds, List<Short> cpuDeckCardIds,
			int cpuLevel, CpuBattleMode cpuBattleMode, Random rnd, Map<Short, CardDefinition> defs) {
		var st = new CpuBattleState();
		CpuBattleMode mode = cpuBattleMode != null ? cpuBattleMode : CpuBattleMode.ORIGIN;
		st.setCpuBattleMode(mode);
		st.setCpuLevel(cpuLevel);
		st.setHumanGoesFirst(rnd.nextBoolean());
		st.setHumansTurn(st.isHumanGoesFirst());
		st.setHumanStones(0);
		st.setCpuStones(0);
		st.setHumanTurnStarts(0);
		st.setCpuTurnStarts(0);

		st.setHumanDeck(buildShuffledInstances(humanDeckCardIds, rnd));
		st.setCpuDeck(buildShuffledInstances(cpuDeckCardIds, rnd));

		for (int i = 0; i < 4; i++) {
			drawOne(st.getHumanDeck(), st.getHumanHand());
			drawOne(st.getCpuDeck(), st.getCpuHand());
		}

		st.addLog(st.isHumanGoesFirst() ? "先攻: あなた" : "先攻: CPU");
		beginTurnGainStone(st, st.isHumansTurn(), defs);
		captureWorldRebuildOpenLayout(st);
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
		beginTurnGainStone(st, st.isHumansTurn(), defs);
		captureWorldRebuildOpenLayout(st);
		st.setLastMessage("対人戦開始");
		return st;
	}

	/**
	 * 先攻1ターン目のストーン付与後の手札・デッキ並びを凍結する（世界の再構築・カウント0用）。
	 * ストーンは「先攻0・後攻1」のバトル開始想定。
	 */
	private void captureWorldRebuildOpenLayout(CpuBattleState st) {
		if (st == null) {
			return;
		}
		st.setWorldRebuildOpenHumanHand(copyCards(st.getHumanHand()));
		st.setWorldRebuildOpenHumanDeck(copyCards(st.getHumanDeck()));
		st.setWorldRebuildOpenCpuHand(copyCards(st.getCpuHand()));
		st.setWorldRebuildOpenCpuDeck(copyCards(st.getCpuDeck()));
		st.setWorldRebuildOpenHumanStones(st.isHumanGoesFirst() ? 0 : 1);
		st.setWorldRebuildOpenCpuStones(st.isHumanGoesFirst() ? 1 : 0);
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

	/** 〈化石（フィールド）〉が場にある間、両プレイヤーのレストのカードは種族判定が「マーフォーク」のみとなる。 */
	private static boolean fossilFieldMerfolkRestActive(CpuBattleState st) {
		if (st == null || st.getActiveField() == null) {
			return false;
		}
		Short fid = st.getActiveField().getCardId();
		return fid != null && fid.shortValue() == GameConstants.FOSSIL_FIELD_TRANSFORMS_TOKEN_CARD_ID;
	}

	private static boolean restCardHasTribe(CpuBattleState st, CardDefinition def, BattleCard c, String tribe) {
		if (fossilFieldMerfolkRestActive(st)) {
			return CardAttributes.hasAttribute("MERFOLK", tribe);
		}
		return CardAttributes.hasAttribute(def, c, tribe);
	}

	/**
	 * レストのファイターについて「種族」コード（複合はアンダースコア区切り）。
	 * 化石〈フィールド〉下は {@link #restCardHasTribe} と同様にマーフォークのみとして扱う。
	 */
	private static String effectiveRestFighterTribeAttributeCode(CpuBattleState st, CardDefinition def,
			BattleCard c) {
		if (fossilFieldMerfolkRestActive(st)) {
			return "MERFOLK";
		}
		if (c != null && c.getBattleTribeOverride() != null && !c.getBattleTribeOverride().isBlank()) {
			return c.getBattleTribeOverride();
		}
		return def != null && def.getAttribute() != null ? def.getAttribute() : "";
	}

	/**
	 * コミックヒーロー〈常時〉: 自分レストの〈フィールド〉以外のファイターが持つ種族セグメントの種類数
	 * （複合種族はセグメントごとに1種類。同じセグメントは複数枚でも1回）。
	 */
	private static int countDistinctTribeSegmentsFromRestFighters(CpuBattleState st, ZoneFighter ownBattle,
			List<BattleCard> rest, Map<Short, CardDefinition> defs) {
		if (rest == null || defs == null) {
			return 0;
		}
		Set<String> kinds = new HashSet<>();
		for (BattleCard c : rest) {
			if (c == null || isTuckedUnderOwnFighter(ownBattle, c)) {
				continue;
			}
			CardDefinition d = defs.get(c.getCardId());
			if (!isNonFieldFighterCardDef(d)) {
				continue;
			}
			kinds.addAll(CardAttributes.segments(effectiveRestFighterTribeAttributeCode(st, d, c)));
		}
		return kinds.size();
	}

	/**
	 * レストゾーン上のカードが「種族：人間」のファイターか（化石フィールド下の上書きを含む）。
	 */
	private static boolean isHumanFighterInRestSlot(CpuBattleState st, CardDefinition d, BattleCard c) {
		if (d == null || isFieldCard(d)) {
			return false;
		}
		if (d.getCardKind() == null || !"FIGHTER".equalsIgnoreCase(d.getCardKind().trim())) {
			return false;
		}
		return restCardHasTribe(st, d, c, "HUMAN");
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

	/**
	 * アーサー〈配置〉: 「決戦の地 カムイ」が〈フィールド〉にあるとき、自分レストの「種族：人間」ファイター候補。
	 */
	private static List<String> arthurKamuiHumanFighterRestOptionIds(CpuBattleState st, boolean deployerIsHuman,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return List.of();
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != FIELD_KAMUI_ID) {
			return List.of();
		}
		ZoneFighter ownBattle = deployerIsHuman ? st.getHumanBattle() : st.getCpuBattle();
		List<BattleCard> rest = deployerIsHuman ? st.getHumanRest() : st.getCpuRest();
		if (rest == null) {
			return List.of();
		}
		List<String> out = new ArrayList<>();
		for (BattleCard c : rest) {
			if (isTuckedUnderOwnFighter(ownBattle, c)) {
				continue;
			}
			if (isHumanFighterInRestSlot(st, defs.get(c.getCardId()), c) && c.getInstanceId() != null) {
				out.add(c.getInstanceId());
			}
		}
		return out;
	}

	/** 〈フィールド〉以外のファイターカードか */
	private static boolean isNonFieldFighterCardDef(CardDefinition d) {
		if (d == null || isFieldCard(d)) {
			return false;
		}
		return d.getCardKind() != null && "FIGHTER".equalsIgnoreCase(d.getCardKind().trim());
	}

	private static boolean isSpec1CardDefinition(CardDefinition d) {
		if (d == null) {
			return false;
		}
		if ("SPEC1".equals(d.getAbilityDeployCode()) || "SPEC0".equals(d.getAbilityDeployCode())) {
			return true;
		}
		return d.getId() != null && d.getId() == SPEC_1_ID;
	}

	/** SPEC-1: レストから選べる「もとの強さが1」の自分ファイター */
	private static boolean isSpec1EligibleRestFighter(BattleCard c, ZoneFighter ownBattle,
			Map<Short, CardDefinition> defs) {
		if (c == null || defs == null) {
			return false;
		}
		if (isTuckedUnderOwnFighter(ownBattle, c)) {
			return false;
		}
		CardDefinition d = defs.get(c.getCardId());
		if (!isNonFieldFighterCardDef(d)) {
			return false;
		}
		Short bp = d.getBasePower();
		return bp != null && bp.intValue() == 1;
	}

	/** バトルログ用。対人戦では相手プレイヤー相当を「相手」、CPU戦では「CPU」と表記する。 */
	private static String opponentActorLogLabel(CpuBattleState st) {
		return st != null && st.isPvp() ? "相手" : "CPU";
	}

	/** バトルログ: human スロット（先に出す側の UI 上の「自分」）— 対人戦ではホスト、CPU 戦では「あなた」。 */
	private static String humanSlotActorLogLabel(CpuBattleState st) {
		return st != null && st.isPvp() ? "ホスト" : "あなた";
	}

	/** バトルログ: cpu スロット — 対人戦ではゲスト、CPU 戦では「CPU」。 */
	private static String cpuSlotActorLogLabel(CpuBattleState st) {
		return st != null && st.isPvp() ? "ゲスト" : "CPU";
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

	/** CPU ランダムデッキ用の基準コスト（〈フィールド〉含む）。 */
	private static int cpuDeckBaseCost(CardDefinition d) {
		if (d == null || d.getCost() == null) {
			return 0;
		}
		return Math.max(0, d.getCost().intValue());
	}

	private static boolean originCanPickAnotherCostGe1(Map<Short, Integer> cnt, Set<Short> forbidden,
			Map<Short, CardDefinition> defs, int ge2Count) {
		if (defs == null) {
			return false;
		}
		Set<Short> ban = forbidden != null ? forbidden : Collections.emptySet();
		for (short id = 1; id <= 30; id++) {
			if (ban.contains(id)) {
				continue;
			}
			if (cnt.getOrDefault(id, 0) >= 2) {
				continue;
			}
			CardDefinition d = defs.get(id);
			if (d == null) {
				continue;
			}
			int c = cpuDeckBaseCost(d);
			if (c <= 0) {
				continue;
			}
			if (c >= 2 && ge2Count >= 1) {
				continue;
			}
			return true;
		}
		return false;
	}

	/**
	 * CPU オリジン／アドバンスドのランダムデッキ共通: コスト割合は強さに依らず
	 * 1コスト以上は4枚まで（取り得る範囲で優先的に入れる）、2コスト以上は1枚まで、残りは0コスト。
	 */
	public record CpuLeagueDeckPair(List<Short> cpuSlot1Deck, List<Short> cpuSlot2Deck) {
	}

	/**
	 * CPU リーグ戦用: カジュアルと同じ抽選ロジックで2デッキを作り、カード ID の重複はさせない（リーグ原則）。
	 */
	public CpuLeagueDeckPair buildCpuLeagueDeckPair(int cpuLevel, CpuBattleMode cpuBattleMode, Random rnd,
			Map<Short, CardDefinition> defs) {
		CpuBattleMode mode = cpuBattleMode != null ? cpuBattleMode : CpuBattleMode.ORIGIN;
		Random r = rnd != null ? rnd : new Random();
		List<Short> deck1 = mode == CpuBattleMode.ADVANCED
				? buildCpuDeckIdsAdvanced(cpuLevel, r, defs, Collections.emptySet())
				: buildCpuDeckIds(cpuLevel, r, defs, Collections.emptySet());
		Set<Short> used = new HashSet<>(deck1);
		List<Short> deck2 = mode == CpuBattleMode.ADVANCED
				? buildCpuDeckIdsAdvanced(cpuLevel, r, defs, used)
				: buildCpuDeckIds(cpuLevel, r, defs, used);
		return new CpuLeagueDeckPair(List.copyOf(deck1), List.copyOf(deck2));
	}

	private List<Short> buildCpuDeckIds(int cpuLevel, Random rnd, Map<Short, CardDefinition> defs) {
		return buildCpuDeckIds(cpuLevel, rnd, defs, Collections.emptySet());
	}

	private List<Short> buildCpuDeckIds(int cpuLevel, Random rnd, Map<Short, CardDefinition> defs, Set<Short> forbiddenIds) {
		final int lvl = clampInt(cpuLevel, 1, 3);
		final String[] coreTribes = new String[] {"HUMAN", "ELF", "UNDEAD", "DRAGON"};

		List<Short> picked = new ArrayList<>();
		Map<Short, Integer> cnt = new HashMap<>();
		Map<String, Integer> tribeCount = new HashMap<>();
		int ge1Count = 0;
		int ge2Count = 0;

		Set<Short> forbidden = forbiddenIds != null ? forbiddenIds : Collections.emptySet();

		// Deck "theme" tribe. Higher levels will adhere to it more often, but not always.
		final String theme = coreTribes[rnd.nextInt(coreTribes.length)];
		// Convergence strength: how much we bias toward the (theme or currently dominant) tribe.
		// Higher levels should converge much harder, but never become deterministic.
		final double convergeBoost = clampDouble(0.55 + 0.40 * (lvl - 1), 0.55, 4.6);
		// Rarity strength: higher levels more likely to pick high rarity.
		final double rarityFactor = clampDouble(0.22 + 0.12 * (lvl - 1), 0.22, 1.3);

		while (picked.size() < 8) {
			boolean zerosOnly = ge1Count >= 4 || !originCanPickAnotherCostGe1(cnt, forbidden, defs, ge2Count);

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
				if (forbidden.contains(id) || cnt.getOrDefault(id, 0) >= 2) {
					w[id] = 0.0;
					continue;
				}
				CardDefinition d = defs != null ? defs.get(id) : null;
				if (d == null) {
					w[id] = 0.0;
					continue;
				}
				int bc = cpuDeckBaseCost(d);
				if (zerosOnly) {
					if (bc != 0) {
						w[id] = 0.0;
						continue;
					}
				} else {
					if (bc <= 0) {
						w[id] = 0.0;
						continue;
					}
					if (bc >= 2 && ge2Count >= 1) {
						w[id] = 0.0;
						continue;
					}
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
				List<Short> zvalid = new ArrayList<>();
				for (short id = 1; id <= 30; id++) {
					if (forbidden.contains(id) || cnt.getOrDefault(id, 0) >= 2) {
						continue;
					}
					CardDefinition d = defs != null ? defs.get(id) : null;
					if (d == null || cpuDeckBaseCost(d) != 0) {
						continue;
					}
					zvalid.add(id);
				}
				if (!zvalid.isEmpty()) {
					short id = zvalid.get(rnd.nextInt(zvalid.size()));
					picked.add(id);
					cnt.put(id, cnt.getOrDefault(id, 0) + 1);
					CardDefinition cd = defs != null ? defs.get(id) : null;
					if (cd != null) {
						for (String t : coreTribes) {
							if (CardAttributes.hasAttribute(cd, t)) {
								tribeCount.put(t, tribeCount.getOrDefault(t, 0) + 1);
							}
						}
					}
					continue;
				}
				// Fallback (shouldn't happen): uniform random.
				short id = (short) (1 + rnd.nextInt(30));
				if (forbidden.contains(id) || cnt.getOrDefault(id, 0) >= 2) {
					continue;
				}
				picked.add(id);
				cnt.put(id, cnt.getOrDefault(id, 0) + 1);
				int bc = cpuDeckBaseCost(defs != null ? defs.get(id) : null);
				if (bc >= 2) {
					ge2Count++;
				}
				if (bc >= 1) {
					ge1Count++;
				}
				CardDefinition cd = defs != null ? defs.get(id) : null;
				if (cd != null) {
					for (String t : coreTribes) {
						if (CardAttributes.hasAttribute(cd, t)) {
							tribeCount.put(t, tribeCount.getOrDefault(t, 0) + 1);
						}
					}
				}
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
			int bcc = cpuDeckBaseCost(defs != null ? defs.get(chosen) : null);
			if (bcc >= 2) {
				ge2Count++;
			}
			if (bcc >= 1) {
				ge1Count++;
			}

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

	/** アドバンスド CPU: 同レベル帯でもオリジンより種族が揃いやすいよう、種族収束だけ強める */
	private static final double ADVANCED_TRIBE_CONVERGENCE_FACTOR = 1.42;
	/**
	 * アドバンスドの種族寄せに加える二次項（レベル1では0）。レベル5に近づくほど単線形より強く同種族が選ばれる。
	 */
	private static final double ADVANCED_TRIBE_CONVERGENCE_QUAD_PER_LEVEL_SQ = 0.13;
	/** L5: 多様化ピックで優勢種族に付けない重み倍率（メイン外を1〜2枚入れやすくする） */
	private static final double ADVANCED_L5_VARIETY_DOMINANT_WEIGHT_MUL = 0.22;
	/** L5: 多様化ピックで優勢種族以外を押し上げる倍率 */
	private static final double ADVANCED_L5_VARIETY_OFF_DOMINANT_WEIGHT_MUL = 2.4;
	/** アドバンスド CPU デッキに含められる〈フィールド〉カードの最大枚数（最小は 1 枚を別途保証） */
	private static final int ADVANCED_CPU_DECK_MAX_FIELD_CARDS = 2;
	/** アドバンスド CPU: 〈フィールド〉をデッキに1枚だけ入れる確率（残りは2枚） */
	private static final double ADVANCED_CPU_DECK_SINGLE_FIELD_PROB = 0.80;

	private static boolean isFieldCardKind(CardDefinition d) {
		if (d == null || d.getCardKind() == null) {
			return false;
		}
		return "FIELD".equalsIgnoreCase(d.getCardKind().trim());
	}

	private static void adjustCount(Map<Short, Integer> cnt, Short id, int delta) {
		if (id == null || cnt == null) {
			return;
		}
		int n = cnt.getOrDefault(id, 0) + delta;
		if (n <= 0) {
			cnt.remove(id);
		} else {
			cnt.put(id, n);
		}
	}

	private static Short pickAdvancedReplacementFighter(List<Short> candidateIds, Map<Short, Integer> cnt,
			Map<Short, CardDefinition> defs, Random rnd, Set<Short> excludedIds) {
		if (candidateIds == null || rnd == null || defs == null) {
			return null;
		}
		Set<Short> ex = excludedIds != null ? excludedIds : Collections.emptySet();
		List<Short> ok = new ArrayList<>();
		for (Short id : candidateIds) {
			if (ex.contains(id)) {
				continue;
			}
			if (cnt.getOrDefault(id, 0) >= 2) {
				continue;
			}
			CardDefinition d = defs.get(id);
			if (d == null || isFieldCardKind(d)) {
				continue;
			}
			ok.add(id);
		}
		if (ok.isEmpty()) {
			return null;
		}
		return ok.get(rnd.nextInt(ok.size()));
	}

	private static Short pickAdvancedReplacementField(List<Short> fieldCandidateIds, Map<Short, Integer> cnt, Random rnd,
			Set<Short> excludedIds) {
		if (fieldCandidateIds == null || rnd == null) {
			return null;
		}
		Set<Short> ex = excludedIds != null ? excludedIds : Collections.emptySet();
		List<Short> ok = new ArrayList<>();
		for (Short id : fieldCandidateIds) {
			if (ex.contains(id)) {
				continue;
			}
			if (cnt.getOrDefault(id, 0) < 2) {
				ok.add(id);
			}
		}
		if (ok.isEmpty()) {
			return null;
		}
		return ok.get(rnd.nextInt(ok.size()));
	}

	private static boolean advancedCanPickAnotherCostGe1(List<Short> candidateIds, Map<Short, Integer> cnt,
			Set<Short> forbidden, int fieldInDeck, Map<Short, CardDefinition> defs, int ge2Count) {
		if (candidateIds == null || defs == null) {
			return false;
		}
		Set<Short> ban = forbidden != null ? forbidden : Collections.emptySet();
		for (short id : candidateIds) {
			if (ban.contains(id) || cnt.getOrDefault(id, 0) >= 2) {
				continue;
			}
			CardDefinition d = defs.get(id);
			if (d == null) {
				continue;
			}
			if (isFieldCardKind(d) && fieldInDeck >= ADVANCED_CPU_DECK_MAX_FIELD_CARDS) {
				continue;
			}
			int bc = cpuDeckBaseCost(d);
			if (bc <= 0) {
				continue;
			}
			if (bc >= 2 && ge2Count >= 1) {
				continue;
			}
			return true;
		}
		return false;
	}

	/**
	 * 〈フィールド〉枚数調整後に崩れたコスト上限（1+ が4枚まで・2+ が1枚まで）を入れ替えで修復する。
	 */
	private static void repairAdvancedCpuDeckCostCaps(List<Short> picked, List<Short> candidateIds,
			List<Short> fieldCandidateIds, Random rnd, Map<Short, CardDefinition> defs, Set<Short> excludedIds) {
		if (picked == null || picked.size() != 8 || defs == null || candidateIds == null || rnd == null) {
			return;
		}
		Set<Short> ex = excludedIds != null ? excludedIds : Collections.emptySet();
		for (int guard = 0; guard < 48; guard++) {
			int ge2 = 0;
			int ge1 = 0;
			for (Short id : picked) {
				int c = cpuDeckBaseCost(defs.get(id));
				if (c >= 2) {
					ge2++;
				}
				if (c >= 1) {
					ge1++;
				}
			}
			if (ge2 <= 1 && ge1 <= 4) {
				return;
			}
			Map<Short, Integer> cnt = new HashMap<>();
			for (Short id : picked) {
				cnt.put(id, cnt.getOrDefault(id, 0) + 1);
			}
			int victimIdx = -1;
			for (int i = 0; i < picked.size(); i++) {
				int c = cpuDeckBaseCost(defs.get(picked.get(i)));
				if (ge2 > 1 && c >= 2) {
					victimIdx = i;
					break;
				}
			}
			if (victimIdx < 0 && ge1 > 4) {
				for (int i = picked.size() - 1; i >= 0; i--) {
					if (cpuDeckBaseCost(defs.get(picked.get(i))) >= 1) {
						victimIdx = i;
						break;
					}
				}
			}
			if (victimIdx < 0) {
				return;
			}
			Short oldId = picked.get(victimIdx);
			boolean wantField = isFieldCardKind(defs.get(oldId));
			Short nid = null;
			if (wantField && fieldCandidateIds != null) {
				List<Short> okF = new ArrayList<>();
				for (Short fid : fieldCandidateIds) {
					if (ex.contains(fid)) {
						continue;
					}
					if (cpuDeckBaseCost(defs.get(fid)) != 0) {
						continue;
					}
					int n = cnt.getOrDefault(fid, 0);
					if (fid.equals(oldId)) {
						n--;
					}
					if (n < 2) {
						okF.add(fid);
					}
				}
				if (!okF.isEmpty()) {
					nid = okF.get(rnd.nextInt(okF.size()));
				}
			}
			if (nid == null) {
				List<Short> okZ = new ArrayList<>();
				for (Short fid : candidateIds) {
					if (ex.contains(fid)) {
						continue;
					}
					CardDefinition d = defs.get(fid);
					if (d == null || isFieldCardKind(d) != wantField) {
						continue;
					}
					if (cpuDeckBaseCost(d) != 0) {
						continue;
					}
					int n = cnt.getOrDefault(fid, 0);
					if (fid.equals(oldId)) {
						n--;
					}
					if (n < 2) {
						okZ.add(fid);
					}
				}
				if (!okZ.isEmpty()) {
					nid = okZ.get(rnd.nextInt(okZ.size()));
				}
			}
			if (nid == null) {
				return;
			}
			picked.set(victimIdx, nid);
		}
	}

	/**
	 * 〈フィールド〉が 1〜2 枚になるよう入れ替える（重み抽選の端数で 0 枚や 3 枚以上になりうるため）。
	 * その後、1 枚:約 80%／2 枚:約 20% になるよう枚数を調整する。
	 */
	private static void enforceAdvancedCpuDeckFieldBounds(List<Short> picked, List<Short> candidateIds,
			List<Short> fieldCandidateIds, Random rnd, Map<Short, CardDefinition> defs, Set<Short> excludedIds) {
		if (picked == null || picked.size() != 8 || defs == null) {
			return;
		}
		Set<Short> ex = excludedIds != null ? excludedIds : Collections.emptySet();
		Map<Short, Integer> cnt = new HashMap<>();
		for (Short id : picked) {
			cnt.put(id, cnt.getOrDefault(id, 0) + 1);
		}
		for (int guard = 0; guard < 16; guard++) {
			int fieldN = 0;
			for (Short id : picked) {
				if (isFieldCardKind(defs.get(id))) {
					fieldN++;
				}
			}
			if (fieldN <= ADVANCED_CPU_DECK_MAX_FIELD_CARDS) {
				break;
			}
			List<Integer> fieldIdx = new ArrayList<>();
			for (int i = 0; i < picked.size(); i++) {
				if (isFieldCardKind(defs.get(picked.get(i)))) {
					fieldIdx.add(i);
				}
			}
			if (fieldIdx.isEmpty()) {
				break;
			}
			int i = fieldIdx.get(rnd.nextInt(fieldIdx.size()));
			Short oldId = picked.get(i);
			Short nid = pickAdvancedReplacementFighter(candidateIds, cnt, defs, rnd, ex);
			if (nid == null) {
				break;
			}
			adjustCount(cnt, oldId, -1);
			picked.set(i, nid);
			adjustCount(cnt, nid, 1);
		}
		for (int guard = 0; guard < 16; guard++) {
			int fieldN = 0;
			for (Short id : picked) {
				if (isFieldCardKind(defs.get(id))) {
					fieldN++;
				}
			}
			if (fieldN >= 1 || fieldCandidateIds == null || fieldCandidateIds.isEmpty()) {
				break;
			}
			List<Integer> fighterIdx = new ArrayList<>();
			for (int i = 0; i < picked.size(); i++) {
				if (!isFieldCardKind(defs.get(picked.get(i)))) {
					fighterIdx.add(i);
				}
			}
			if (fighterIdx.isEmpty()) {
				break;
			}
			int i = fighterIdx.get(rnd.nextInt(fighterIdx.size()));
			Short oldId = picked.get(i);
			Short nid = pickAdvancedReplacementField(fieldCandidateIds, cnt, rnd, ex);
			if (nid == null) {
				break;
			}
			adjustCount(cnt, oldId, -1);
			picked.set(i, nid);
			adjustCount(cnt, nid, 1);
		}
		// 強さにかかわらず〈フィールド〉1枚: 80%、2枚: 20%（上の補正で 1〜2 枚に収まっている前提）
		int targetFields = rnd.nextDouble() < ADVANCED_CPU_DECK_SINGLE_FIELD_PROB ? 1 : 2;
		for (int guard = 0; guard < 24; guard++) {
			int fieldN = 0;
			for (Short id : picked) {
				if (isFieldCardKind(defs.get(id))) {
					fieldN++;
				}
			}
			if (fieldN == targetFields) {
				break;
			}
			if (fieldN > targetFields) {
				List<Integer> fieldIdx = new ArrayList<>();
				for (int i = 0; i < picked.size(); i++) {
					if (isFieldCardKind(defs.get(picked.get(i)))) {
						fieldIdx.add(i);
					}
				}
				if (fieldIdx.isEmpty()) {
					break;
				}
				int i = fieldIdx.get(rnd.nextInt(fieldIdx.size()));
				Short oldId = picked.get(i);
				Short nid = pickAdvancedReplacementFighter(candidateIds, cnt, defs, rnd, ex);
				if (nid == null) {
					break;
				}
				adjustCount(cnt, oldId, -1);
				picked.set(i, nid);
				adjustCount(cnt, nid, 1);
			} else {
				if (fieldCandidateIds == null || fieldCandidateIds.isEmpty()) {
					break;
				}
				List<Integer> fighterIdx = new ArrayList<>();
				for (int i = 0; i < picked.size(); i++) {
					if (!isFieldCardKind(defs.get(picked.get(i)))) {
						fighterIdx.add(i);
					}
				}
				if (fighterIdx.isEmpty()) {
					break;
				}
				int i = fighterIdx.get(rnd.nextInt(fighterIdx.size()));
				Short oldId = picked.get(i);
				Short nid = pickAdvancedReplacementField(fieldCandidateIds, cnt, rnd, ex);
				if (nid == null) {
					break;
				}
				adjustCount(cnt, oldId, -1);
				picked.set(i, nid);
				adjustCount(cnt, nid, 1);
			}
		}
	}

	/**
	 * 〈フィールド〉を含む全ファイター定義から CPU デッキを構築。
	 * 強さは 1〜5 段階（レベルが上がるほど種族収束・レアリティが上がりやすい。種族は {@link #ADVANCED_TRIBE_CONVERGENCE_FACTOR} と
	 * {@link #ADVANCED_TRIBE_CONVERGENCE_QUAD_PER_LEVEL_SQ} でオリジンより寄せ、高レベルほど差をつける）。
	 * レベル5のみ、残り枚数に応じた多様化ピックでメイン外が1〜2枚入りやすい。
	 * 〈フィールド〉は必ず 1 枚以上、多くても 2 枚まで。強さにかかわらず 1 枚が約 80%、2 枚が約 20%。
	 * コスト割合はオリジン CPU と同様（1+ は4枚まで・2+ は1枚まで・残り0コスト）。
	 */
	private List<Short> buildCpuDeckIdsAdvanced(int cpuLevel, Random rnd, Map<Short, CardDefinition> defs) {
		return buildCpuDeckIdsAdvanced(cpuLevel, rnd, defs, Collections.emptySet());
	}

	private List<Short> buildCpuDeckIdsAdvanced(int cpuLevel, Random rnd, Map<Short, CardDefinition> defs,
			Set<Short> forbiddenIds) {
		final Set<Short> forbidden = forbiddenIds != null ? forbiddenIds : Collections.emptySet();
		if (defs == null || defs.isEmpty()) {
			return buildCpuDeckIds(Math.min(cpuLevel, 3), rnd, defs, forbidden);
		}
		List<Short> candidateIds = new ArrayList<>();
		for (CardDefinition d : defs.values()) {
			if (d == null || d.getId() == null) {
				continue;
			}
			if (GameConstants.excludedFromPackOpenAndLibraryListing(d.getId())) {
				continue;
			}
			String kind = d.getCardKind();
			if (kind == null) {
				continue;
			}
			String k = kind.trim();
			if ("FIGHTER".equalsIgnoreCase(k) || "FIELD".equalsIgnoreCase(k)) {
				candidateIds.add(d.getId());
			}
		}
		if (candidateIds.isEmpty()) {
			return buildCpuDeckIds(Math.min(cpuLevel, 3), rnd, defs, forbidden);
		}
		Collections.sort(candidateIds);

		List<Short> fieldCandidateIds = new ArrayList<>();
		for (short id : candidateIds) {
			CardDefinition fd = defs.get(id);
			if (fd != null && isFieldCardKind(fd)) {
				fieldCandidateIds.add(id);
			}
		}

		final int lvl = clampInt(cpuLevel, 1, 5);
		final String[] coreTribes = new String[] {"HUMAN", "ELF", "UNDEAD", "DRAGON"};

		List<Short> picked = new ArrayList<>();
		Map<Short, Integer> cnt = new HashMap<>();
		Map<String, Integer> tribeCount = new HashMap<>();
		int fieldInDeck = 0;
		int ge1Count = 0;
		int ge2Count = 0;

		final String theme = coreTribes[rnd.nextInt(coreTribes.length)];
		final int lvlMinus1 = lvl - 1;
		final double convergeBase = 0.55 + 0.40 * lvlMinus1
				+ ADVANCED_TRIBE_CONVERGENCE_QUAD_PER_LEVEL_SQ * lvlMinus1 * lvlMinus1;
		final double convergeBoost = clampDouble(convergeBase * ADVANCED_TRIBE_CONVERGENCE_FACTOR, 0.55, 7.0);
		final double rarityFactor = clampDouble(0.22 + 0.12 * (lvl - 1), 0.22, 1.45);
		// L5: メイン外を混ぜるピックの残り回数（0〜2）。全体として1〜2枚外しやすい確率になるよう初期化
		int l5VarietyRemaining = 0;
		if (lvl == 5) {
			double u = rnd.nextDouble();
			// 種族: 14% 完全一致、70% が1枚だけ別種族、16% が2枚だけ別種族（多様化ピック回数）
			if (u < 0.14) {
				l5VarietyRemaining = 0;
			} else if (u < 0.84) {
				l5VarietyRemaining = 1;
			} else {
				l5VarietyRemaining = 2;
			}
		}
		// L5 通常ピックのみ種族寄せを少し弱め、多様化と合わせて「ほぼ揃い＋外し1〜2」になりやすくする
		final double convergeBoostNormal = lvl == 5 ? convergeBoost * 0.88 : convergeBoost;

		while (picked.size() < 8) {
			boolean zerosOnly = ge1Count >= 4
					|| !advancedCanPickAnotherCostGe1(candidateIds, cnt, forbidden, fieldInDeck, defs, ge2Count);

			String dominant = theme;
			int best = -1;
			for (String t : coreTribes) {
				int c = tribeCount.getOrDefault(t, 0);
				if (c > best) {
					best = c;
					dominant = t;
				}
			}

			int slotsLeft = 8 - picked.size();
			boolean l5VarietyThisPick = false;
			if (lvl == 5 && l5VarietyRemaining > 0 && slotsLeft > 0) {
				if (l5VarietyRemaining > slotsLeft) {
					l5VarietyRemaining = slotsLeft;
				}
				if (l5VarietyRemaining >= slotsLeft) {
					l5VarietyThisPick = true;
					l5VarietyRemaining--;
				} else if (rnd.nextDouble() < (double) l5VarietyRemaining / slotsLeft) {
					l5VarietyThisPick = true;
					l5VarietyRemaining--;
				}
			}

			double totalW = 0.0;
			Map<Short, Double> w = new HashMap<>();
			for (short id : candidateIds) {
				if (forbidden.contains(id) || cnt.getOrDefault(id, 0) >= 2) {
					continue;
				}
				CardDefinition d = defs.get(id);
				if (d == null) {
					continue;
				}
				if (isFieldCardKind(d) && fieldInDeck >= ADVANCED_CPU_DECK_MAX_FIELD_CARDS) {
					continue;
				}
				int bc = cpuDeckBaseCost(d);
				if (zerosOnly) {
					if (bc != 0) {
						continue;
					}
				} else {
					if (bc <= 0) {
						continue;
					}
					if (bc >= 2 && ge2Count >= 1) {
						continue;
					}
				}
				double ww = 1.0;

				boolean hasTheme = CardAttributes.hasAttribute(d, theme);
				boolean hasDom = CardAttributes.hasAttribute(d, dominant);
				double themeMul = theme != null && theme.equals("DRAGON") ? 0.45 : 1.0;
				double domMul = dominant != null && dominant.equals("DRAGON") ? 0.55 : 1.0;
				if (l5VarietyThisPick) {
					if (hasDom) {
						ww *= ADVANCED_L5_VARIETY_DOMINANT_WEIGHT_MUL;
					} else {
						ww *= ADVANCED_L5_VARIETY_OFF_DOMINANT_WEIGHT_MUL;
					}
					if (hasTheme) {
						ww *= (1.0 + convergeBoostNormal * 0.35 * themeMul);
					}
				} else {
					if (hasTheme) ww *= (1.0 + convergeBoostNormal * 0.70 * themeMul);
					if (hasDom) ww *= (1.0 + convergeBoostNormal * 1.15 * domMul);
				}

				int rr = rarityRank(d.getRarity());
				ww *= (1.0 + rr * rarityFactor);
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
				if (lvl > 2) {
					if (rr >= 2) ww *= (1.0 + 0.35 * rarityFactor);
					if (rr >= 3) ww *= (1.0 + 0.55 * rarityFactor);
				}

				int already = cnt.getOrDefault(id, 0);
				if (already == 1) ww *= 0.72;

				w.put(id, ww);
				totalW += ww;
			}

			if (totalW <= 0) {
				List<Short> zvalid = new ArrayList<>();
				for (short id : candidateIds) {
					if (forbidden.contains(id) || cnt.getOrDefault(id, 0) >= 2) {
						continue;
					}
					CardDefinition d = defs.get(id);
					if (d == null) {
						continue;
					}
					if (isFieldCardKind(d) && fieldInDeck >= ADVANCED_CPU_DECK_MAX_FIELD_CARDS) {
						continue;
					}
					if (cpuDeckBaseCost(d) != 0) {
						continue;
					}
					zvalid.add(id);
				}
				if (!zvalid.isEmpty()) {
					short id = zvalid.get(rnd.nextInt(zvalid.size()));
					picked.add(id);
					cnt.put(id, cnt.getOrDefault(id, 0) + 1);
					CardDefinition addDef = defs.get(id);
					if (addDef != null && isFieldCardKind(addDef)) {
						fieldInDeck++;
					}
					if (addDef != null) {
						for (String t : coreTribes) {
							if (CardAttributes.hasAttribute(addDef, t)) {
								tribeCount.put(t, tribeCount.getOrDefault(t, 0) + 1);
							}
						}
					}
					continue;
				}
				List<Short> valid = new ArrayList<>();
				for (short id : candidateIds) {
					if (forbidden.contains(id) || cnt.getOrDefault(id, 0) >= 2) {
						continue;
					}
					CardDefinition d = defs.get(id);
					if (d == null) {
						continue;
					}
					if (isFieldCardKind(d) && fieldInDeck >= ADVANCED_CPU_DECK_MAX_FIELD_CARDS) {
						continue;
					}
					valid.add(id);
				}
				if (valid.isEmpty()) {
					for (short id : candidateIds) {
						if (forbidden.contains(id) || cnt.getOrDefault(id, 0) >= 2) {
							continue;
						}
						CardDefinition d = defs.get(id);
						if (d == null) {
							continue;
						}
						valid.add(id);
					}
				}
				if (valid.isEmpty()) {
					break;
				}
				short id = valid.get(rnd.nextInt(valid.size()));
				picked.add(id);
				cnt.put(id, cnt.getOrDefault(id, 0) + 1);
				CardDefinition addDef = defs.get(id);
				if (addDef != null && isFieldCardKind(addDef)) {
					fieldInDeck++;
				}
				int bcf = cpuDeckBaseCost(addDef);
				if (bcf >= 2) {
					ge2Count++;
				}
				if (bcf >= 1) {
					ge1Count++;
				}
				if (addDef != null) {
					for (String t : coreTribes) {
						if (CardAttributes.hasAttribute(addDef, t)) {
							tribeCount.put(t, tribeCount.getOrDefault(t, 0) + 1);
						}
					}
				}
				continue;
			}

			double r = rnd.nextDouble() * totalW;
			short chosen = candidateIds.get(0);
			for (short id : candidateIds) {
				Double ww = w.get(id);
				if (ww == null || ww <= 0) {
					continue;
				}
				r -= ww;
				if (r <= 0) {
					chosen = id;
					break;
				}
			}

			picked.add(chosen);
			cnt.put(chosen, cnt.getOrDefault(chosen, 0) + 1);

			CardDefinition cd = defs.get(chosen);
			if (cd != null) {
				int bcc = cpuDeckBaseCost(cd);
				if (bcc >= 2) {
					ge2Count++;
				}
				if (bcc >= 1) {
					ge1Count++;
				}
				if (isFieldCardKind(cd)) {
					fieldInDeck++;
				}
				for (String t : coreTribes) {
					if (CardAttributes.hasAttribute(cd, t)) {
						tribeCount.put(t, tribeCount.getOrDefault(t, 0) + 1);
					}
				}
			}
		}
		enforceAdvancedCpuDeckFieldBounds(picked, candidateIds, fieldCandidateIds, rnd, defs, forbidden);
		repairAdvancedCpuDeckCostCaps(picked, candidateIds, fieldCandidateIds, rnd, defs, forbidden);
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

	private static void addCopiesOfCardIdToHand(List<BattleCard> hand, short cardId, int count, Map<Short, CardDefinition> defs) {
		if (hand == null || count <= 0 || defs == null || defs.get(cardId) == null) {
			return;
		}
		for (int i = 0; i < count; i++) {
			hand.add(0, new BattleCard(UUID.randomUUID().toString(), cardId));
		}
	}

	/**
	 * ルシファー効果適用中は「奇跡」の代わりに「堕天使ルシファー」を手札へ加える（定義が無いときは奇跡のまま）。
	 */
	private static short miracleGrantCardId(CpuBattleState st, boolean handOwnerHuman, Map<Short, CardDefinition> defs) {
		if (defs == null) {
			return GameConstants.MIRACLE_TOKEN_CARD_ID;
		}
		boolean conv = handOwnerHuman ? st.isHumanMiraclesBecomeFallenLucifer() : st.isCpuMiraclesBecomeFallenLucifer();
		short fid = GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID;
		if (conv && defs.get(fid) != null) {
			return fid;
		}
		return GameConstants.MIRACLE_TOKEN_CARD_ID;
	}

	/** 天界門・セレスティア等が手札へ加える「奇跡」枠のカード定義が存在するか（ルシファー後は堕天使の定義で可）。 */
	private static boolean canGrantMiracleSlotCard(CpuBattleState st, boolean forHuman, Map<Short, CardDefinition> defs) {
		return defs != null && defs.get(miracleGrantCardId(st, forHuman, defs)) != null;
	}

	private static void addMiracleCopiesToHandForPlayer(List<BattleCard> hand, int count, CpuBattleState st,
			boolean handOwnerHuman, Map<Short, CardDefinition> defs) {
		short id = miracleGrantCardId(st, handOwnerHuman, defs);
		addCopiesOfCardIdToHand(hand, id, count, defs);
	}

	/** ルシファー〈配置〉: 当該プレイヤーゾーン内の「奇跡」をすべて「堕天使ルシファー」に置き換える（cardId のみ）。 */
	private static void replaceMiraclesWithFallenLuciferInPlayerZones(CpuBattleState st, boolean humanSlot,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null || defs.get(GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID) == null) {
			return;
		}
		short m = GameConstants.MIRACLE_TOKEN_CARD_ID;
		short f = GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID;
		List<BattleCard> deck = humanSlot ? st.getHumanDeck() : st.getCpuDeck();
		List<BattleCard> hand = humanSlot ? st.getHumanHand() : st.getCpuHand();
		List<BattleCard> rest = humanSlot ? st.getHumanRest() : st.getCpuRest();
		ZoneFighter battle = humanSlot ? st.getHumanBattle() : st.getCpuBattle();
		for (BattleCard c : deck) {
			if (c != null && c.getCardId() == m) {
				c.setCardId(f);
			}
		}
		for (BattleCard c : hand) {
			if (c != null && c.getCardId() == m) {
				c.setCardId(f);
			}
		}
		for (BattleCard c : rest) {
			if (c != null && c.getCardId() == m) {
				c.setCardId(f);
			}
		}
		if (battle != null) {
			if (battle.getMain() != null && battle.getMain().getCardId() == m) {
				battle.getMain().setCardId(f);
			}
			if (battle.getCostUnder() != null) {
				for (BattleCard c : battle.getCostUnder()) {
					if (c != null && c.getCardId() == m) {
						c.setCardId(f);
					}
				}
			}
		}
	}

	/** 天界門 ヘヴンズゲート〈フィールド〉: 〈場〉に置いたプレイヤーにのみ「奇跡」を1枚。 */
	private void grantHeavensGateDeployMiracleToPlacer(CpuBattleState st, boolean newFieldPlacedByHost,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		short mid = GameConstants.MIRACLE_TOKEN_CARD_ID;
		if (defs.get(mid) == null && defs.get(GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID) == null) {
			return;
		}
		if (newFieldPlacedByHost) {
			addMiracleCopiesToHandForPlayer(st.getHumanHand(), 1, st, true, defs);
		} else {
			addMiracleCopiesToHandForPlayer(st.getCpuHand(), 1, st, false, defs);
		}
		String who = newFieldPlacedByHost ? humanSlotActorLogLabel(st) : cpuSlotActorLogLabel(st);
		st.addLog("天界門 ヘヴンズゲート: " + who + "は「奇跡」を1枚手札に加えた");
	}

	/** 天界門 ヘヴンズゲート〈フィールド〉: ターン開始したプレイヤーにのみ「奇跡」1枚（先攻1ターン目のストーン例外と同様にスキップ後）。 */
	private static void grantHeavensGateTurnStartMiracleForTurnOwner(CpuBattleState st, boolean turnOwnerHuman,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != GameConstants.HEAVENS_GATE_FIELD_CARD_ID) {
			return;
		}
		short mid = GameConstants.MIRACLE_TOKEN_CARD_ID;
		if (defs.get(mid) == null && defs.get(GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID) == null) {
			return;
		}
		if (turnOwnerHuman) {
			addMiracleCopiesToHandForPlayer(st.getHumanHand(), 1, st, true, defs);
		} else {
			addMiracleCopiesToHandForPlayer(st.getCpuHand(), 1, st, false, defs);
		}
		String who = turnOwnerHuman ? humanSlotActorLogLabel(st) : cpuSlotActorLogLabel(st);
		st.addLog("天界門 ヘヴンズゲート: " + who + "は「奇跡」を1枚手札に加えた");
	}

	private void setPendingDeployEffectOnly(CpuBattleState st, boolean ownerHuman, CardDefinition mainDef, ZoneFighter zone,
			int mechanicStacksAtDeploy) {
		if (st == null || mainDef == null || zone == null || zone.getMain() == null) {
			return;
		}
		String code = mainDef.getAbilityDeployCode();
		PendingEffect pe = new PendingEffect();
		pe.setOwnerHuman(ownerHuman);
		pe.setMainInstanceId(zone.getMain().getInstanceId());
		pe.setCardId(zone.getMain().getCardId());
		pe.setAbilityDeployCode(code);
		pe.setApplied(false);
		pe.setCrystakulOptionalResolved(false);
		pe.setMechanicStacksAtDeploy(Math.max(0, mechanicStacksAtDeploy));
		st.setPendingEffect(pe);
	}

	private void stagePendingDeployEffect(CpuBattleState st, boolean ownerHuman, CardDefinition mainDef, ZoneFighter zone,
			int mechanicStacksAtDeploy) {
		setPendingDeployEffectOnly(st, ownerHuman, mainDef, zone, mechanicStacksAtDeploy);
		st.setPhase(ownerHuman ? BattlePhase.HUMAN_EFFECT_PENDING : BattlePhase.CPU_EFFECT_PENDING);
		st.setLastMessage("効果を処理中…");
	}

	/**
	 * クリック配置: クリスタクルの任意ストーン確認を「効果テキスト表示」より先に出す（サムライの任意ストーンと同様の順序）。
	 */
	private void stageInteractiveDeployEffectWithCrystakulOptionalFirst(CpuBattleState st, boolean ownerHuman,
			CardDefinition mainDef, ZoneFighter z, Map<Short, CardDefinition> defs, int mechanicStacksAtDeploy) {
		setPendingDeployEffectOnly(st, ownerHuman, mainDef, z, mechanicStacksAtDeploy);
		ensureCrystakulOptionalStonePromptAfterDeployEffect(st, defs, ownerHuman);
		if (st.getPendingChoice() != null) {
			st.setPhase(BattlePhase.HUMAN_CHOICE);
			st.setLastMessage("選択してください");
			return;
		}
		st.setPhase(ownerHuman ? BattlePhase.HUMAN_EFFECT_PENDING : BattlePhase.CPU_EFFECT_PENDING);
		st.setLastMessage("効果を処理中…");
	}

	private void markCrystakulOptionalResolvedFromPendingChoice(PendingChoice pc, CpuBattleState st) {
		if (pc == null || st == null) {
			return;
		}
		if ("CRYSTAKUL".equals(pc.getAbilityDeployCode()) && st.getPendingEffect() != null) {
			st.getPendingEffect().setCrystakulOptionalResolved(true);
		}
	}

	/** pendingEffect の main が今も人間側バトルゾーンにいるか（配置側の実体で判定） */
	private static boolean pendingDeployEffectOnHumanBattleSlot(CpuBattleState st, PendingEffect pe) {
		if (st == null || pe == null) {
			return false;
		}
		String mid = pe.getMainInstanceId();
		if (mid == null) {
			return false;
		}
		ZoneFighter hz = st.getHumanBattle();
		return hz != null && hz.getMain() != null && mid.equals(hz.getMain().getInstanceId());
	}

	/** pendingEffect の main が今も CPU 側バトルゾーンにいるか */
	private static boolean pendingDeployEffectOnCpuBattleSlot(CpuBattleState st, PendingEffect pe) {
		if (st == null || pe == null) {
			return false;
		}
		String mid = pe.getMainInstanceId();
		if (mid == null) {
			return false;
		}
		ZoneFighter cz = st.getCpuBattle();
		return cz != null && cz.getMain() != null && mid.equals(cz.getMain().getInstanceId());
	}

	/**
	 * 忍者入れ替え後の〈配置〉確認など、効果解決中に {@link BattlePhase#HUMAN_CHOICE} へ一旦移す処理から戻す。
	 * {@link #resolvePendingEffectAndAdvance} はフェーズが EFFECT_PENDING のときだけ knock/ドローでターンを進めるため必須。
	 */
	private static void restoreEffectPendingPhaseAfterEmbeddedHumanChoice(CpuBattleState st, PendingEffect pe,
			boolean pendingOnHumanSlot, boolean pendingOnCpuSlot) {
		if (st == null) {
			return;
		}
		BattlePhase ph;
		if (pendingOnHumanSlot) {
			ph = BattlePhase.HUMAN_EFFECT_PENDING;
		} else if (pendingOnCpuSlot) {
			ph = BattlePhase.CPU_EFFECT_PENDING;
		} else {
			ph = pe != null && pe.isOwnerHuman() ? BattlePhase.HUMAN_EFFECT_PENDING : BattlePhase.CPU_EFFECT_PENDING;
		}
		st.setPhase(ph);
	}

	/**
	 * クリスタクル: {@link #applyDeployHuman} / {@link #applyDeployHumanAsCpuSide} 内の早期 return で〈配置〉確認が付かない場合の補完。
	 * バトルゾーンの実カードが CRYSTAKUL で、ストーンが足りれば必ず任意ストーン確認を出す。
	 */
	private void ensureCrystakulOptionalStonePromptAfterDeployEffect(CpuBattleState st, Map<Short, CardDefinition> defs,
			boolean deployerActsAsHuman) {
		if (st == null || defs == null || st.getPendingChoice() != null) {
			return;
		}
		if (st.getPendingEffect() != null && st.getPendingEffect().isCrystakulOptionalResolved()) {
			return;
		}
		if (deployerActsAsHuman) {
			if (st.getHumanBattle() == null || st.getHumanBattle().getMain() == null) {
				return;
			}
			short cid = st.getHumanBattle().getMain().getCardId();
			CardDefinition md = defs.get(cid);
			if (md == null && cid == CRYSTAKUL_ID) {
				md = new CardDefinition();
				md.setId(CRYSTAKUL_ID);
				md.setAbilityDeployCode("CRYSTAKUL");
			}
			if (md == null || !isCrystakulCardDefinition(md)) {
				return;
			}
			if (deployAbilitySuppressedByOpponentLine(st, true, md)) {
				return;
			}
			if (st.getHumanStones() < CRYSTAKUL_OPTIONAL_STONE_COST) {
				return;
			}
			st.setPendingChoice(new PendingChoice(
					ChoiceKind.CONFIRM_OPTIONAL_STONE,
					"クリスタクル（ストーン2・次の配置+3／次の相手ターン終了まで）",
					true,
					"CRYSTAKUL",
					CRYSTAKUL_OPTIONAL_STONE_COST,
					List.of()));
		} else {
			if (st.getCpuBattle() == null || st.getCpuBattle().getMain() == null) {
				return;
			}
			short cid = st.getCpuBattle().getMain().getCardId();
			CardDefinition md = defs.get(cid);
			if (md == null && cid == CRYSTAKUL_ID) {
				md = new CardDefinition();
				md.setId(CRYSTAKUL_ID);
				md.setAbilityDeployCode("CRYSTAKUL");
			}
			if (md == null || !isCrystakulCardDefinition(md)) {
				return;
			}
			if (deployAbilitySuppressedByOpponentLine(st, false, md)) {
				return;
			}
			if (st.getCpuStones() < CRYSTAKUL_OPTIONAL_STONE_COST) {
				return;
			}
			st.setPendingChoice(new PendingChoice(
					ChoiceKind.CONFIRM_OPTIONAL_STONE,
					"クリスタクル（ストーン2・次の配置+3／次の相手ターン終了まで）",
					false,
					"CRYSTAKUL",
					CRYSTAKUL_OPTIONAL_STONE_COST,
					List.of(),
					true));
		}
	}

	public void resolvePendingEffectAndAdvance(CpuBattleState st, Map<Short, CardDefinition> defs, Random rnd) {
		if (st == null || st.isGameOver()) return;
		PendingEffect pe = st.getPendingEffect();
		if (pe == null) return;
		/*
		 * 〈配置〉本体は HUMAN_EFFECT_PENDING / CPU_EFFECT_PENDING のときだけ解決する。
		 * 忍者の入れ替え確認などで HUMAN_CHOICE 中に誤って /resolve が呼ばれると、入れ替え後メイン（例: 策士）の
		 * 〈配置〉が先に適用され、確認後の resolveNinjaSwappedDeployEffects で再度適用され、デッキ上が複数回レストへ送られる。
		 */
		BattlePhase phase = st.getPhase();
		if (phase != BattlePhase.HUMAN_EFFECT_PENDING && phase != BattlePhase.CPU_EFFECT_PENDING) {
			return;
		}

		boolean pendingOnHumanSlot = pendingDeployEffectOnHumanBattleSlot(st, pe);
		boolean pendingOnCpuSlot = pendingDeployEffectOnCpuBattleSlot(st, pe);
		// 実際にどちらのバトルゾーンに置かれたかで解決する（ownerHuman フラグだけだと誤って applyDeployCpu になり、クリスタクル等が相手ストーン基準になる）
		boolean deployerActsAsHuman = pendingOnHumanSlot ? true : (pendingOnCpuSlot ? false : pe.isOwnerHuman());

		// 効果適用（選択待ちの間に二重適用しない）
		if (!pe.isApplied()) {
			if (!pe.isNinjaSwapPhaseDone() && pe.getAbilityDeployCode() != null
					&& "NINJA".equals(pe.getAbilityDeployCode())) {
				boolean swappedWithCost = applyNinjaPhysicalSwap(st, defs, pendingOnHumanSlot);
				pe.setNinjaSwapPhaseDone(true);
				syncPendingEffectMainAfterNinjaSwap(pe, st, defs, pendingOnHumanSlot);
				st.setPendingEffect(pe);
				if (swappedWithCost && (pendingOnHumanSlot || (pendingOnCpuSlot && st.isPvp()))) {
					ZoneFighter zForName = pendingOnHumanSlot ? st.getHumanBattle() : st.getCpuBattle();
					CardDefinition swDef = zForName != null && zForName.getMain() != null
							? defs.get(zForName.getMain().getCardId()) : null;
					String swName = swDef != null && swDef.getName() != null ? swDef.getName() : "？";
					String ac = swDef != null && swDef.getAbilityDeployCode() != null ? swDef.getAbilityDeployCode() : "";
					boolean guest = pendingOnCpuSlot && st.isPvp();
					String ninjaDeployConfirmPrompt = "「" + swName + "」の〈配置〉効果を使用しますか？";
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_NINJA_SWAPPED_DEPLOY,
							ninjaDeployConfirmPrompt,
							!guest,
							ac,
							0,
							List.of(),
							guest));
					st.setPhase(BattlePhase.HUMAN_CHOICE);
					st.setLastMessage(ninjaDeployConfirmPrompt);
					return;
				}
			}

			CardDefinition d = defs.get(pe.getCardId());
			BattleCard deployedMain = null;
			if (pendingOnHumanSlot && st.getHumanBattle() != null) {
				deployedMain = st.getHumanBattle().getMain();
			} else if (pendingOnCpuSlot && st.getCpuBattle() != null) {
				deployedMain = st.getCpuBattle().getMain();
			}
			// 〈探鉱の洞窟〉: バトルゾーンへのファイター配置の直後、〈配置〉効果の前にストーン+1（竜王等で〈配置〉が無効でも配置自体は行われるため適用する）
			// フィールド判定は「実際に置かれたメイン」の定義を優先（pending とゾーンの不整合や忍者入れ替え後も正しく扱う）
			CardDefinition fighterDefForFieldTriggers = d;
			if (deployedMain != null) {
				CardDefinition fromZone = defs.get(deployedMain.getCardId());
				if (fromZone != null) {
					fighterDefForFieldTriggers = fromZone;
				}
			}
			if (fighterDefForFieldTriggers != null && !isFieldCard(fighterDefForFieldTriggers)) {
				applyFieldNebulaWhenCarbuncleFighterDeployed(st, fighterDefForFieldTriggers, deployedMain, deployerActsAsHuman);
			}
			// SPEC-666 の種族上書きは〈配置〉抑止（竜王等）とは無関係に適用する（抑止時は applyDeployHuman 自体が呼ばれないため）
			if (fighterDefForFieldTriggers != null && !isFieldCard(fighterDefForFieldTriggers)) {
				if (pendingOnHumanSlot) {
					applySpec666UndeadToDeployedFighterIfPending(st, true, defs);
				} else if (pendingOnCpuSlot) {
					applySpec666UndeadToDeployedFighterIfPending(st, false, defs);
				}
				int mechStacksAtDeploy = pe.getMechanicStacksAtDeploy();
				if (mechStacksAtDeploy > 0) {
					if (pendingOnHumanSlot) {
						applyMechanicMachineTribeToDeployedFighterIfUsed(st, true, mechStacksAtDeploy, defs);
					} else if (pendingOnCpuSlot) {
						applyMechanicMachineTribeToDeployedFighterIfUsed(st, false, mechStacksAtDeploy, defs);
					}
				}
			}
			// 相手の竜王、ミスティンクル／ワイバーン、または相手側フレイムガルド〈フィールド〉で〈配置〉が無効（クリスタクル任意ストーンは例外）
			boolean deploySuppressed = deployAbilitySuppressedByOpponentLine(st, deployerActsAsHuman, fighterDefForFieldTriggers);
			// pendingEffect の cardId と defs の不整合でも、ゾーン上の実カードで〈配置〉を解決する
			CardDefinition abilityDefToResolve = fighterDefForFieldTriggers != null ? fighterDefForFieldTriggers : d;
			if (!deploySuppressed && abilityDefToResolve != null) {
				if (pendingOnHumanSlot) {
					applyDeployHuman(st, abilityDefToResolve, defs, deployedMain);
				} else if (pendingOnCpuSlot && st.isPvp()) {
					applyDeployHumanAsCpuSide(st, abilityDefToResolve, defs, deployedMain);
				} else if (pendingOnCpuSlot) {
					applyDeployCpu(st, abilityDefToResolve, defs, rnd != null ? rnd : new Random(), deployedMain);
				} else if (pe.isOwnerHuman()) {
					applyDeployHuman(st, abilityDefToResolve, defs, deployedMain);
				} else if (st.isPvp()) {
					applyDeployHumanAsCpuSide(st, abilityDefToResolve, defs, deployedMain);
				} else {
					applyDeployCpu(st, abilityDefToResolve, defs, rnd != null ? rnd : new Random(), deployedMain);
				}
			}
			if (fighterDefForFieldTriggers != null && !isFieldCard(fighterDefForFieldTriggers)) {
				if (pendingOnHumanSlot) {
					applyChojuGigaTribeIfPending(st, true, defs);
				} else if (pendingOnCpuSlot) {
					applyChojuGigaTribeIfPending(st, false, defs);
				}
			}
			// deploySuppressed でもゾーン実体がクリスタクルなら補完で確認を付ける（上の分岐で付かなかった場合）
			if (st.getPendingChoice() == null) {
				ensureCrystakulOptionalStonePromptAfterDeployEffect(st, defs, deployerActsAsHuman);
			}
			pe.setApplied(true);
			st.setPendingEffect(pe);
		}

		if (st.isGameOver()) {
			return;
		}

		// 選択が必要なら、ここで止める
		PendingChoice pend0 = st.getPendingChoice();
		if (pend0 != null && (pend0.isForHuman() || pend0.isCpuSlotChooses())) {
			st.setPhase(BattlePhase.HUMAN_CHOICE);
			st.setLastMessage("選択してください");
			return;
		}

		// 配置能力の結果を含めて強さ条件を満たす必要がある
		if (deployerActsAsHuman) {
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
			resetTurnBuffs(st, defs);
			st.setHumansTurn(false);
			st.setPhase(BattlePhase.CPU_THINKING);
			// CPUのターン開始：ストーン付与（先攻1ターン目のみ獲得なし）
			beginTurnGainStone(st, false, defs);
			st.setLastMessage(st.isPvp() ? "ゲストのターン" : "CPUのターン");
		} else if (st.getPhase() == BattlePhase.CPU_EFFECT_PENDING) {
			resolveKnockAndDraw(st, false, defs);
			resetTurnBuffs(st, defs);
			st.setHumansTurn(true);
			st.setPhase(BattlePhase.HUMAN_INPUT);
			// 人間のターン開始：ストーン付与（先攻1ターン目のみ獲得なし）
			beginTurnGainStone(st, true, defs);
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
						st.setHumanNextCrystakulDeployBonus(snap.getHumanNextCrystakulDeployBonus());
						st.setCpuNextCrystakulDeployBonus(snap.getCpuNextCrystakulDeployBonus());
						st.setHumanCrystakulCombatBonus(snap.getHumanCrystakulCombatBonus());
						st.setCpuCrystakulCombatBonus(snap.getCpuCrystakulCombatBonus());
						st.setSpec666NextHumanUndead(snap.isSpec666NextHumanUndead());
						st.setSpec666NextCpuUndead(snap.isSpec666NextCpuUndead());

						st.setHumanPendingZadkielNextDeployOppTurnPower3(snap.isHumanPendingZadkielNextDeployOppTurnPower3());
						st.setCpuPendingZadkielNextDeployOppTurnPower3(snap.isCpuPendingZadkielNextDeployOppTurnPower3());

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
						st.setHumanSlotDeckId(snap.getHumanSlotDeckId());
						st.setCpuSlotDeckId(snap.getCpuSlotDeckId());

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
			case CONFIRM_MIRAJUKUL_MIRROR -> {
				if (confirm) {
					// 確認用の PendingChoice が残ったままだと直後の null 判定が常に真になり進行不能になる
					st.setPendingChoice(null);
					applyMirageMirrorDeploy(st, true, defs, rnd);
					if (st.getPendingChoice() != null) {
						return;
					}
				} else {
					st.addLog("ミラージュクル: 効果を使わなかった");
				}
			}
			case CONFIRM_NINJA_SWAPPED_DEPLOY -> {
				st.setPendingChoice(null);
				PendingEffect peff = st.getPendingEffect();
				if (peff == null) {
					return;
				}
				boolean pendHum = pendingDeployEffectOnHumanBattleSlot(st, peff);
				boolean pendCpu = pendingDeployEffectOnCpuBattleSlot(st, peff);
				boolean deployerActsAsHuman = pendHum ? true : (pendCpu ? false : peff.isOwnerHuman());
				if (confirm) {
					resolveNinjaSwappedDeployEffects(st, defs, rnd, pendHum, pendCpu);
					if (st.getPendingChoice() != null) {
						// 〈配置〉本体は resolveNinja 内で適用済み。applied を立てないと後続の resolve で再度〈配置〉が走る
						peff.setApplied(true);
						st.setPendingEffect(peff);
						return;
					}
					ensureCrystakulOptionalStonePromptAfterDeployEffect(st, defs, deployerActsAsHuman);
					if (st.getPendingChoice() != null) {
						peff.setApplied(true);
						st.setPendingEffect(peff);
						return;
					}
				} else {
					st.addLog("忍者: 入れ替え先の〈配置〉を使わなかった");
				}
				peff.setApplied(true);
				st.setPendingEffect(peff);
				restoreEffectPendingPhaseAfterEmbeddedHumanChoice(st, peff, pendHum, pendCpu);
				resolvePendingEffectAndAdvance(st, defs, rnd);
				return;
			}
			case CONFIRM_OPTIONAL_STONE -> {
				boolean humanMayResolveOptionalStone = confirm && (pc.getStoneCost() <= 0
						|| st.getHumanStones() >= pc.getStoneCost());
				if (humanMayResolveOptionalStone) {
					if (pc.getStoneCost() > 0) {
						st.setHumanStones(st.getHumanStones() - pc.getStoneCost());
						st.addLog("ストーンを" + pc.getStoneCost() + "使用");
					}
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
					} else if ("KORYU".equals(pc.getAbilityDeployCode())) {
						int elves = countAttributeInRest(st, st.getHumanRest(), defs, "ELF");
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
					} else if ("FEZARIA".equals(pc.getAbilityDeployCode())) {
						List<String> opts = new ArrayList<>();
						for (BattleCard c : st.getHumanRest()) {
							if (isFezariaPickableCarbuncleInRest(st, c, st.getHumanBattle(), defs)) {
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
					} else if ("CRYSTAKUL".equals(pc.getAbilityDeployCode())) {
						st.setHumanNextCrystakulDeployBonus(st.getHumanNextCrystakulDeployBonus() + CRYSTAKUL_NEXT_DEPLOY_POWER);
						st.addLog("クリスタクル: 次の配置+3（次の相手ターン終了まで）");
					} else if ("SEASERPENT".equals(pc.getAbilityDeployCode())) {
						addCopiesOfCardIdToHand(st.getHumanHand(), GameConstants.SWORDFISH_TOKEN_CARD_ID, 1, defs);
						st.addLog("シーサーペント: 「ソードフィッシュ」を1枚手札に加えた");
					} else if (RAMIEL_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						applyRamielDeployEffect(st, true, false);
					} else if (CELESTIA_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						if (canGrantMiracleSlotCard(st, true, defs)) {
							addMiracleCopiesToHandForPlayer(st.getHumanHand(), 2, st, true, defs);
							st.addLog("セレスティア: 「奇跡」を2枚手札に加えた");
						} else {
							st.addLog("セレスティア: 「奇跡」の定義がない");
						}
					} else if ("RESEARCHER_FLORA".equals(pc.getAbilityDeployCode())) {
						List<String> floraOpts = new ArrayList<>();
						for (BattleCard bc : st.getHumanRest()) {
							if (isTuckedUnderOwnFighter(st.getHumanBattle(), bc)) {
								continue;
							}
							if (restCardHasTribe(st, defs.get(bc.getCardId()), bc, "ELF")) {
								floraOpts.add(bc.getInstanceId());
							}
						}
						if (!floraOpts.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
									"研究者フローラ（レストの「種族：エルフ」を1枚手札へ）",
									true,
									"RESEARCHER_FLORA",
									0,
									floraOpts));
							return;
						}
						st.addLog("研究者フローラ: レストに「種族：エルフ」がない");
					} else if (COMIC_WITCH_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						if (defs.get(INK_KNIGHT_ID) == null) {
							st.addLog("コミックウィッチ: インクナイトの定義がない");
						} else {
							List<String> cwOpts = comicWitchPickableRestInstanceIds(st, true);
							if (!cwOpts.isEmpty()) {
								st.setPendingChoice(new PendingChoice(
										ChoiceKind.SELECT_UP_TO_TWO_FROM_REST_TO_HAND,
										"コミックウィッチ（レストから最大2枚を「インクナイト」に）",
										true,
										COMIC_WITCH_DEPLOY_CODE,
										0,
										cwOpts));
								return;
							}
							st.addLog("コミックウィッチ: レストに対象のカードがない");
						}
					} else if (ZADKIEL_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						List<String> mirZadAfterConfirm = new ArrayList<>();
						for (BattleCard hc : st.getHumanHand()) {
							if (hc != null && hc.getInstanceId() != null
									&& hc.getCardId() == GameConstants.MIRACLE_TOKEN_CARD_ID) {
								mirZadAfterConfirm.add(hc.getInstanceId());
							}
						}
						if (!mirZadAfterConfirm.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
									"ザドキエル（「奇跡」を1枚レストへ置いてもよい。置いたなら次に配置するファイターは相手ターン中強さ+3）",
									true,
									ZADKIEL_DEPLOY_CODE,
									0,
									mirZadAfterConfirm));
							return;
						}
						st.addLog("ザドキエル: 手札に「奇跡」がない");
					} else if (SERAPHIM_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						List<String> mirSerAfterConfirm = new ArrayList<>();
						for (BattleCard hc : st.getHumanHand()) {
							if (hc != null && hc.getInstanceId() != null
									&& hc.getCardId() == GameConstants.MIRACLE_TOKEN_CARD_ID) {
								mirSerAfterConfirm.add(hc.getInstanceId());
							}
						}
						if (!mirSerAfterConfirm.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
									"セラフィム（「奇跡」を1枚レストへ置いてもよい。置いたなら、自分のレストの「種族：エンジェル」を1枚手札に）",
									true,
									SERAPHIM_DEPLOY_CODE,
									0,
									mirSerAfterConfirm));
							return;
						}
						st.addLog("セラフィム: 手札に「奇跡」がない");
					}
				} else {
					st.addLog("効果を使用しなかった");
				}
				markCrystakulOptionalResolvedFromPendingChoice(pc, st);
			}
			case SELECT_ONE_FROM_HAND_TO_REST -> {
				if (ZADKIEL_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
					if (pickedInstanceIds == null) {
						return;
					}
					if (pickedInstanceIds.isEmpty()) {
						st.addLog("ザドキエル: 効果を使わなかった");
						break;
					}
					if (pickedInstanceIds.size() != 1) {
						return;
					}
					String zPick = pickedInstanceIds.get(0);
					if (pc.getOptionInstanceIds() == null || !pc.getOptionInstanceIds().contains(zPick)) {
						return;
					}
					BattleCard zc = removeByInstanceId(st.getHumanHand(), zPick);
					if (zc == null || zc.getCardId() != GameConstants.MIRACLE_TOKEN_CARD_ID) {
						return;
					}
					st.getHumanRest().add(zc);
					st.setHumanPendingZadkielNextDeployOppTurnPower3(true);
					st.addLog("ザドキエル: 「奇跡」をレストへ置いた");
					break;
				}
				if (SERAPHIM_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
					if (pickedInstanceIds == null) {
						return;
					}
					if (pickedInstanceIds.isEmpty()) {
						st.addLog("セラフィム: 効果を使わなかった");
						break;
					}
					if (pickedInstanceIds.size() != 1) {
						return;
					}
					String sPick = pickedInstanceIds.get(0);
					if (pc.getOptionInstanceIds() == null || !pc.getOptionInstanceIds().contains(sPick)) {
						return;
					}
					BattleCard sc = removeByInstanceId(st.getHumanHand(), sPick);
					if (sc == null || sc.getCardId() != GameConstants.MIRACLE_TOKEN_CARD_ID) {
						return;
					}
					st.getHumanRest().add(sc);
					st.addLog("セラフィム: 「奇跡」をレストへ置いた");
					List<String> serOpts = seraphimAngelRestOptionIds(st, true, defs);
					if (!serOpts.isEmpty()) {
						st.setPendingChoice(new PendingChoice(
								ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
								"セラフィム（レストの「種族：エンジェル」を1枚手札に）",
								true,
								SERAPHIM_DEPLOY_CODE,
								0,
								serOpts));
						return;
					}
					st.addLog("セラフィム: レストに「種族：エンジェル」がない");
					break;
				}
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) return;
				BattleCard c = removeByInstanceId(st.getHumanHand(), pickedInstanceIds.get(0));
				if (c != null) {
					st.getHumanRest().add(c);
					st.addLog("手札を1枚レストへ");
				}
				if (c != null && KENTOSHI_PAIR_FIRST_DEPLOY_CODE.equals(pc.getAbilityDeployCode())
						&& !st.getCpuHand().isEmpty()) {
					if (st.isPvp()) {
						List<String> opts = new ArrayList<>();
						for (BattleCard x : st.getCpuHand()) {
							opts.add(x.getInstanceId());
						}
						st.setPendingChoice(new PendingChoice(
								ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
								"剣闘士（捨てるカードを選択）",
								false,
								KENTOSHI_OPPONENT_FOLLOWUP_DEPLOY_CODE,
								0,
								opts,
								true));
						st.setPhase(BattlePhase.HUMAN_CHOICE);
						st.setLastMessage("選択してください");
						return;
					}
					kentoshiAiDiscardOneFromHand(st.getCpuHand(), st.getCpuRest(), defs);
					st.addLog("剣闘士: " + cpuSlotActorLogLabel(st) + "も手札を1枚レストへ");
				}
				if (c != null && "COMIC_DINOSAUR".equals(pc.getAbilityDeployCode())) {
					finishComicDinosaurDeployFollowUp(st, true, defs);
				}
			}
			case SELECT_ONE_FROM_HAND_FOR_SKETCHER_COPY -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) {
					return;
				}
				String skPick = pickedInstanceIds.get(0);
				if (pc.getOptionInstanceIds() == null || !pc.getOptionInstanceIds().contains(skPick)) {
					return;
				}
				if (!SKETCHER_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
					return;
				}
				BattleCard src = findByInstanceId(st.getHumanHand(), skPick);
				if (src == null) {
					return;
				}
				BattleCard cp = copyCard(src);
				cp.setInstanceId(UUID.randomUUID().toString());
				st.getHumanHand().add(0, cp);
				st.addLog("スケッチャー: 手札のカードのコピーを1枚手札に加えた");
			}
			case SELECT_ONE_UNDEAD_FIGHTER_FROM_HAND_FOR_COST -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) {
					return;
				}
				String gpPick = pickedInstanceIds.get(0);
				if (pc.getOptionInstanceIds() == null || !pc.getOptionInstanceIds().contains(gpPick)) {
					return;
				}
				if (findByInstanceId(st.getCpuHand(), gpPick) != null) {
					return;
				}
				BattleCard gpc = findByInstanceId(st.getHumanHand(), gpPick);
				if (gpc == null) {
					return;
				}
				CardDefinition gcd = defs.get(gpc.getCardId());
				if (!isGravePriestEligibleHandFighter(gcd, gpc)) {
					return;
				}
				if (effectiveDeployCost(gcd, gpc, defs, st.getHumanRest(), st.getHumanNextMechanicStacks(), st) <= 0) {
					return;
				}
				gpc.setHandDeployCostModifier(gpc.getHandDeployCostModifier() - GRAVE_PRIEST_HAND_COST_REDUCTION);
				st.addLog("墓守神父: 手札のファイターのコストを-" + GRAVE_PRIEST_HAND_COST_REDUCTION + "（バトル終了まで）");
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
					} else if ("ARTHUR".equals(pc.getAbilityDeployCode())) {
						st.addLog("アーサー: 人間ファイターを手札へ");
					} else if ("JOSHU".equals(pc.getAbilityDeployCode())) {
						c.setBattleEndPowerBonus(c.getBattleEndPowerBonus() + JOSHU_ASSISTANT_RESEARCHER_BATTLE_END_POWER_BONUS);
						st.addLog("助手: 手札へ（バトル終了まで強さ+" + JOSHU_ASSISTANT_RESEARCHER_BATTLE_END_POWER_BONUS + "）");
					} else if ("ASTORIA".equals(pc.getAbilityDeployCode())) {
						c.setHandDeployCostModifier(-1);
						st.addLog("研究者アストリア: 手札へ（コスト-1）");
					} else if ("RYUNOTAMAGO".equals(pc.getAbilityDeployCode())) {
						st.addLog("ドラゴンの卵: レストのドラゴンを手札へ");
					} else if ("RESEARCHER_FLORA".equals(pc.getAbilityDeployCode())) {
						st.addLog("研究者フローラ: レストの「種族：エルフ」を手札へ");
					} else if (SERAPHIM_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						if (pc.getOptionInstanceIds() == null
								|| !pc.getOptionInstanceIds().contains(pickedInstanceIds.get(0))
								|| !isSeraphimPickableAngelInRest(st, c, st.getHumanBattle(), defs)) {
							st.getHumanRest().add(c);
							return;
						}
						st.addLog("セラフィム: レストの「種族：エンジェル」を手札へ");
					} else {
						st.addLog("レストから手札へ");
					}
					st.getHumanHand().add(0, c);
				}
			}
			case SELECT_ONE_FROM_REST_TO_DECK_TOP -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) {
					return;
				}
				String pickId = pickedInstanceIds.get(0);
				if (pc.getOptionInstanceIds() == null || !pc.getOptionInstanceIds().contains(pickId)) {
					return;
				}
				BattleCard c = removeByInstanceId(st.getHumanRest(), pickId);
				if (c != null && isSpec1EligibleRestFighter(c, st.getHumanBattle(), defs)) {
					st.getHumanDeck().add(0, c);
					st.addLog("SPEC-1: レストのファイターをデッキの上に置いた");
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
				if ("LEVIATHAN".equals(pc.getAbilityDeployCode())) {
					List<String> allowed = pc.getOptionInstanceIds();
					for (String id : pickedInstanceIds) {
						if (allowed == null || !allowed.contains(id)) {
							return;
						}
					}
					int nl = 0;
					for (String id : pickedInstanceIds) {
						BattleCard c = removeByInstanceId(st.getHumanRest(), id);
						if (c == null) {
							continue;
						}
						if (isLeviathanPickableDragonOrMerfolkInRest(st, c, st.getHumanBattle(), defs)) {
							st.getHumanHand().add(0, c);
							nl++;
						} else {
							st.getHumanRest().add(c);
						}
					}
					st.addLog("リヴァイアサン: レストから手札へ（" + nl + "枚）");
					advanceActiveFieldCountForLeviathan(st, 2, defs);
				} else if (COMIC_WITCH_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
					List<String> allowedCw = pc.getOptionInstanceIds();
					for (String id : pickedInstanceIds) {
						if (allowedCw == null || !allowedCw.contains(id)) {
							return;
						}
					}
					if (defs.get(INK_KNIGHT_ID) == null) {
						return;
					}
					int nInk = 0;
					for (String id : pickedInstanceIds) {
						BattleCard c = removeByInstanceId(st.getHumanRest(), id);
						if (c == null) {
							continue;
						}
						st.getHumanHand().add(0, new BattleCard(UUID.randomUUID().toString(), INK_KNIGHT_ID));
						nInk++;
					}
					st.addLog("コミックウィッチ: 「インクナイト」に" + nInk + "枚変化して手札へ");
				} else {
					int n = 0;
					for (String id : pickedInstanceIds) {
						BattleCard c = removeByInstanceId(st.getHumanRest(), id);
						if (c == null) {
							continue;
						}
						if (isFezariaPickableCarbuncleInRest(st, c, st.getHumanBattle(), defs)) {
							st.getHumanHand().add(0, c);
							n++;
						} else {
							// 不整合時にレストから取り除いたままにしない（種族は定義ベースで選別済みのはず）
							st.getHumanRest().add(c);
						}
					}
					st.addLog("フェザリア: レストから手札へ（" + n + "枚）");
				}
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
						st.setHumanNextCrystakulDeployBonus(snap.getHumanNextCrystakulDeployBonus());
						st.setCpuNextCrystakulDeployBonus(snap.getCpuNextCrystakulDeployBonus());
						st.setHumanCrystakulCombatBonus(snap.getHumanCrystakulCombatBonus());
						st.setCpuCrystakulCombatBonus(snap.getCpuCrystakulCombatBonus());
						st.setSpec666NextHumanUndead(snap.isSpec666NextHumanUndead());
						st.setSpec666NextCpuUndead(snap.isSpec666NextCpuUndead());

						st.setHumanPendingZadkielNextDeployOppTurnPower3(snap.isHumanPendingZadkielNextDeployOppTurnPower3());
						st.setCpuPendingZadkielNextDeployOppTurnPower3(snap.isCpuPendingZadkielNextDeployOppTurnPower3());

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
						st.setHumanSlotDeckId(snap.getHumanSlotDeckId());
						st.setCpuSlotDeckId(snap.getCpuSlotDeckId());

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
			case CONFIRM_MIRAJUKUL_MIRROR -> {
				if (confirm) {
					st.setPendingChoice(null);
					applyMirageMirrorDeploy(st, false, defs, rnd);
					if (st.getPendingChoice() != null) {
						return;
					}
				} else {
					st.addLog("ミラージュクル: 効果を使わなかった");
				}
			}
			case CONFIRM_NINJA_SWAPPED_DEPLOY -> {
				st.setPendingChoice(null);
				PendingEffect peffG = st.getPendingEffect();
				if (peffG == null) {
					return;
				}
				boolean pendHumG = pendingDeployEffectOnHumanBattleSlot(st, peffG);
				boolean pendCpuG = pendingDeployEffectOnCpuBattleSlot(st, peffG);
				boolean deployerActsAsHumanG = pendHumG ? true : (pendCpuG ? false : peffG.isOwnerHuman());
				if (confirm) {
					resolveNinjaSwappedDeployEffects(st, defs, rnd, pendHumG, pendCpuG);
					if (st.getPendingChoice() != null) {
						// 〈配置〉本体は resolveNinja 内で適用済み。applied を立てないと後続の resolve で再度〈配置〉が走る
						peffG.setApplied(true);
						st.setPendingEffect(peffG);
						return;
					}
					ensureCrystakulOptionalStonePromptAfterDeployEffect(st, defs, deployerActsAsHumanG);
					if (st.getPendingChoice() != null) {
						peffG.setApplied(true);
						st.setPendingEffect(peffG);
						return;
					}
				} else {
					st.addLog("忍者: 入れ替え先の〈配置〉を使わなかった");
				}
				peffG.setApplied(true);
				st.setPendingEffect(peffG);
				restoreEffectPendingPhaseAfterEmbeddedHumanChoice(st, peffG, pendHumG, pendCpuG);
				resolvePendingEffectAndAdvance(st, defs, rnd);
				return;
			}
			case CONFIRM_OPTIONAL_STONE -> {
				boolean guestMayResolveOptionalStone = confirm && (pc.getStoneCost() <= 0
						|| st.getCpuStones() >= pc.getStoneCost());
				if (guestMayResolveOptionalStone) {
					if (pc.getStoneCost() > 0) {
						st.setCpuStones(st.getCpuStones() - pc.getStoneCost());
						st.addLog("ストーンを" + pc.getStoneCost() + "使用");
					}
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
					} else if ("KORYU".equals(pc.getAbilityDeployCode())) {
						int elves = countAttributeInRest(st, st.getCpuRest(), defs, "ELF");
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
					} else if ("FEZARIA".equals(pc.getAbilityDeployCode())) {
						List<String> opts = new ArrayList<>();
						for (BattleCard c : st.getCpuRest()) {
							if (isFezariaPickableCarbuncleInRest(st, c, st.getCpuBattle(), defs)) {
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
					} else if ("CRYSTAKUL".equals(pc.getAbilityDeployCode())) {
						st.setCpuNextCrystakulDeployBonus(st.getCpuNextCrystakulDeployBonus() + CRYSTAKUL_NEXT_DEPLOY_POWER);
						st.addLog("クリスタクル: 次の配置+3（次の相手ターン終了まで）");
					} else if ("SEASERPENT".equals(pc.getAbilityDeployCode())) {
						addCopiesOfCardIdToHand(st.getCpuHand(), GameConstants.SWORDFISH_TOKEN_CARD_ID, 1, defs);
						st.addLog("シーサーペント: 「ソードフィッシュ」を1枚手札に加えた");
					} else if (RAMIEL_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						applyRamielDeployEffect(st, false, false);
					} else if (CELESTIA_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						if (canGrantMiracleSlotCard(st, false, defs)) {
							addMiracleCopiesToHandForPlayer(st.getCpuHand(), 2, st, false, defs);
							st.addLog("セレスティア: 「奇跡」を2枚手札に加えた");
						} else {
							st.addLog("セレスティア: 「奇跡」の定義がない");
						}
					} else if ("RESEARCHER_FLORA".equals(pc.getAbilityDeployCode())) {
						List<String> floraGuestOpts = new ArrayList<>();
						for (BattleCard bc : st.getCpuRest()) {
							if (isTuckedUnderOwnFighter(st.getCpuBattle(), bc)) {
								continue;
							}
							if (restCardHasTribe(st, defs.get(bc.getCardId()), bc, "ELF")) {
								floraGuestOpts.add(bc.getInstanceId());
							}
						}
						if (!floraGuestOpts.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
									"研究者フローラ（レストの「種族：エルフ」を1枚手札へ）",
									false,
									"RESEARCHER_FLORA",
									0,
									floraGuestOpts,
									true));
							return;
						}
						st.addLog("研究者フローラ: レストに「種族：エルフ」がない");
					} else if (COMIC_WITCH_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						if (defs.get(INK_KNIGHT_ID) == null) {
							st.addLog("コミックウィッチ: インクナイトの定義がない");
						} else {
							List<String> cwGuestOpts = comicWitchPickableRestInstanceIds(st, false);
							if (!cwGuestOpts.isEmpty()) {
								st.setPendingChoice(new PendingChoice(
										ChoiceKind.SELECT_UP_TO_TWO_FROM_REST_TO_HAND,
										"コミックウィッチ（レストから最大2枚を「インクナイト」に）",
										false,
										COMIC_WITCH_DEPLOY_CODE,
										0,
										cwGuestOpts,
										true));
								return;
							}
							st.addLog("コミックウィッチ: レストに対象のカードがない");
						}
					} else if (ZADKIEL_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						List<String> mirZadGuestAfterConfirm = new ArrayList<>();
						for (BattleCard hc : st.getCpuHand()) {
							if (hc != null && hc.getInstanceId() != null
									&& hc.getCardId() == GameConstants.MIRACLE_TOKEN_CARD_ID) {
								mirZadGuestAfterConfirm.add(hc.getInstanceId());
							}
						}
						if (!mirZadGuestAfterConfirm.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
									"ザドキエル（「奇跡」を1枚レストへ置いてもよい。置いたなら次に配置するファイターは相手ターン中強さ+3）",
									false,
									ZADKIEL_DEPLOY_CODE,
									0,
									mirZadGuestAfterConfirm,
									true));
							return;
						}
						st.addLog("ザドキエル: 手札に「奇跡」がない");
					} else if (SERAPHIM_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						List<String> mirSerGuestAfterConfirm = new ArrayList<>();
						for (BattleCard hc : st.getCpuHand()) {
							if (hc != null && hc.getInstanceId() != null
									&& hc.getCardId() == GameConstants.MIRACLE_TOKEN_CARD_ID) {
								mirSerGuestAfterConfirm.add(hc.getInstanceId());
							}
						}
						if (!mirSerGuestAfterConfirm.isEmpty()) {
							st.setPendingChoice(new PendingChoice(
									ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
									"セラフィム（「奇跡」を1枚レストへ置いてもよい。置いたなら、自分のレストの「種族：エンジェル」を1枚手札に）",
									false,
									SERAPHIM_DEPLOY_CODE,
									0,
									mirSerGuestAfterConfirm,
									true));
							return;
						}
						st.addLog("セラフィム: 手札に「奇跡」がない");
					}
				} else {
					st.addLog("効果を使用しなかった");
				}
				markCrystakulOptionalResolvedFromPendingChoice(pc, st);
			}
			case SELECT_ONE_FROM_HAND_TO_REST -> {
				if (ZADKIEL_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
					if (pickedInstanceIds == null) {
						return;
					}
					if (pickedInstanceIds.isEmpty()) {
						st.addLog("ザドキエル: 効果を使わなかった");
						break;
					}
					if (pickedInstanceIds.size() != 1) {
						return;
					}
					String zPickG = pickedInstanceIds.get(0);
					if (pc.getOptionInstanceIds() == null || !pc.getOptionInstanceIds().contains(zPickG)) {
						return;
					}
					BattleCard zcg = removeByInstanceId(st.getCpuHand(), zPickG);
					if (zcg == null || zcg.getCardId() != GameConstants.MIRACLE_TOKEN_CARD_ID) {
						return;
					}
					st.getCpuRest().add(zcg);
					st.setCpuPendingZadkielNextDeployOppTurnPower3(true);
					st.addLog("ザドキエル: 「奇跡」をレストへ置いた");
					break;
				}
				if (SERAPHIM_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
					if (pickedInstanceIds == null) {
						return;
					}
					if (pickedInstanceIds.isEmpty()) {
						st.addLog("セラフィム: 効果を使わなかった");
						break;
					}
					if (pickedInstanceIds.size() != 1) {
						return;
					}
					String sPickG = pickedInstanceIds.get(0);
					if (pc.getOptionInstanceIds() == null || !pc.getOptionInstanceIds().contains(sPickG)) {
						return;
					}
					BattleCard scg = removeByInstanceId(st.getCpuHand(), sPickG);
					if (scg == null || scg.getCardId() != GameConstants.MIRACLE_TOKEN_CARD_ID) {
						return;
					}
					st.getCpuRest().add(scg);
					st.addLog("セラフィム: 「奇跡」をレストへ置いた");
					List<String> serGuestOpts = seraphimAngelRestOptionIds(st, false, defs);
					if (!serGuestOpts.isEmpty()) {
						st.setPendingChoice(new PendingChoice(
								ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
								"セラフィム（レストの「種族：エンジェル」を1枚手札に）",
								false,
								SERAPHIM_DEPLOY_CODE,
								0,
								serGuestOpts,
								true));
						return;
					}
					st.addLog("セラフィム: レストに「種族：エンジェル」がない");
					break;
				}
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) return;
				BattleCard c = removeByInstanceId(st.getCpuHand(), pickedInstanceIds.get(0));
				if (c != null) {
					st.getCpuRest().add(c);
					st.addLog("手札を1枚レストへ");
				}
				if (c != null && KENTOSHI_PAIR_FIRST_DEPLOY_CODE.equals(pc.getAbilityDeployCode())
						&& !st.getHumanHand().isEmpty()) {
					List<String> opts = new ArrayList<>();
					for (BattleCard x : st.getHumanHand()) {
						opts.add(x.getInstanceId());
					}
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
							"剣闘士（捨てるカードを選択）",
							true,
							KENTOSHI_OPPONENT_FOLLOWUP_DEPLOY_CODE,
							0,
							opts));
					st.setPhase(BattlePhase.HUMAN_CHOICE);
					st.setLastMessage("選択してください");
					return;
				}
				if (c != null && "COMIC_DINOSAUR".equals(pc.getAbilityDeployCode())) {
					finishComicDinosaurDeployFollowUp(st, false, defs);
				}
			}
			case SELECT_ONE_FROM_HAND_FOR_SKETCHER_COPY -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) {
					return;
				}
				String skPickG = pickedInstanceIds.get(0);
				if (pc.getOptionInstanceIds() == null || !pc.getOptionInstanceIds().contains(skPickG)) {
					return;
				}
				if (!SKETCHER_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
					return;
				}
				BattleCard srcG = findByInstanceId(st.getCpuHand(), skPickG);
				if (srcG == null) {
					return;
				}
				BattleCard cpG = copyCard(srcG);
				cpG.setInstanceId(UUID.randomUUID().toString());
				st.getCpuHand().add(0, cpG);
				st.addLog(cpuSlotActorLogLabel(st) + "のスケッチャー: 手札のカードのコピーを1枚手札に加えた");
			}
			case SELECT_ONE_UNDEAD_FIGHTER_FROM_HAND_FOR_COST -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) {
					return;
				}
				String gpPickG = pickedInstanceIds.get(0);
				if (pc.getOptionInstanceIds() == null || !pc.getOptionInstanceIds().contains(gpPickG)) {
					return;
				}
				if (findByInstanceId(st.getHumanHand(), gpPickG) != null) {
					return;
				}
				BattleCard gpcg = findByInstanceId(st.getCpuHand(), gpPickG);
				if (gpcg == null) {
					return;
				}
				CardDefinition gcdg = defs.get(gpcg.getCardId());
				if (!isGravePriestEligibleHandFighter(gcdg, gpcg)) {
					return;
				}
				if (effectiveDeployCost(gcdg, gpcg, defs, st.getCpuRest(), st.getCpuNextMechanicStacks(), st) <= 0) {
					return;
				}
				gpcg.setHandDeployCostModifier(gpcg.getHandDeployCostModifier() - GRAVE_PRIEST_HAND_COST_REDUCTION);
				st.addLog("墓守神父: 手札のファイターのコストを-" + GRAVE_PRIEST_HAND_COST_REDUCTION + "（バトル終了まで）");
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
					} else if ("ARTHUR".equals(pc.getAbilityDeployCode())) {
						st.addLog("アーサー: 人間ファイターを手札へ");
					} else if ("JOSHU".equals(pc.getAbilityDeployCode())) {
						c.setBattleEndPowerBonus(c.getBattleEndPowerBonus() + JOSHU_ASSISTANT_RESEARCHER_BATTLE_END_POWER_BONUS);
						st.addLog("助手: 手札へ（バトル終了まで強さ+" + JOSHU_ASSISTANT_RESEARCHER_BATTLE_END_POWER_BONUS + "）");
					} else if ("ASTORIA".equals(pc.getAbilityDeployCode())) {
						c.setHandDeployCostModifier(-1);
						st.addLog("研究者アストリア: 手札へ（コスト-1）");
					} else if ("RYUNOTAMAGO".equals(pc.getAbilityDeployCode())) {
						st.addLog("ドラゴンの卵: レストのドラゴンを手札へ");
					} else if ("RESEARCHER_FLORA".equals(pc.getAbilityDeployCode())) {
						st.addLog("研究者フローラ: レストの「種族：エルフ」を手札へ");
					} else if (SERAPHIM_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
						if (pc.getOptionInstanceIds() == null
								|| !pc.getOptionInstanceIds().contains(pickedInstanceIds.get(0))
								|| !isSeraphimPickableAngelInRest(st, c, st.getCpuBattle(), defs)) {
							st.getCpuRest().add(c);
							return;
						}
						st.addLog("セラフィム: レストの「種族：エンジェル」を手札へ");
					} else {
						st.addLog("レストから手札へ");
					}
					st.getCpuHand().add(0, c);
				}
			}
			case SELECT_ONE_FROM_REST_TO_DECK_TOP -> {
				if (pickedInstanceIds == null || pickedInstanceIds.size() != 1) {
					return;
				}
				String pickIdG = pickedInstanceIds.get(0);
				if (pc.getOptionInstanceIds() == null || !pc.getOptionInstanceIds().contains(pickIdG)) {
					return;
				}
				BattleCard c = removeByInstanceId(st.getCpuRest(), pickIdG);
				if (c != null && isSpec1EligibleRestFighter(c, st.getCpuBattle(), defs)) {
					st.getCpuDeck().add(0, c);
					st.addLog("SPEC-1: レストのファイターをデッキの上に置いた");
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
				if ("LEVIATHAN".equals(pc.getAbilityDeployCode())) {
					List<String> allowedG = pc.getOptionInstanceIds();
					for (String id : pickedInstanceIds) {
						if (allowedG == null || !allowedG.contains(id)) {
							return;
						}
					}
					int nlg = 0;
					for (String id : pickedInstanceIds) {
						BattleCard c = removeByInstanceId(st.getCpuRest(), id);
						if (c == null) {
							continue;
						}
						if (isLeviathanPickableDragonOrMerfolkInRest(st, c, st.getCpuBattle(), defs)) {
							st.getCpuHand().add(0, c);
							nlg++;
						} else {
							st.getCpuRest().add(c);
						}
					}
					st.addLog("リヴァイアサン: レストから手札へ（" + nlg + "枚）");
					advanceActiveFieldCountForLeviathan(st, 2, defs);
				} else if (COMIC_WITCH_DEPLOY_CODE.equals(pc.getAbilityDeployCode())) {
					List<String> allowedCwG = pc.getOptionInstanceIds();
					for (String id : pickedInstanceIds) {
						if (allowedCwG == null || !allowedCwG.contains(id)) {
							return;
						}
					}
					if (defs.get(INK_KNIGHT_ID) == null) {
						return;
					}
					int nInkG = 0;
					for (String id : pickedInstanceIds) {
						BattleCard c = removeByInstanceId(st.getCpuRest(), id);
						if (c == null) {
							continue;
						}
						st.getCpuHand().add(0, new BattleCard(UUID.randomUUID().toString(), INK_KNIGHT_ID));
						nInkG++;
					}
					st.addLog("コミックウィッチ: 「インクナイト」に" + nInkG + "枚変化して手札へ");
				} else {
					int ng = 0;
					for (String id : pickedInstanceIds) {
						BattleCard c = removeByInstanceId(st.getCpuRest(), id);
						if (c == null) {
							continue;
						}
						if (isFezariaPickableCarbuncleInRest(st, c, st.getCpuBattle(), defs)) {
							st.getCpuHand().add(0, c);
							ng++;
						} else {
							st.getCpuRest().add(c);
						}
					}
					st.addLog("フェザリア: レストから手札へ（" + ng + "枚）");
				}
			}
		}

		st.setPendingChoice(null);
		st.setPhase(st.isHumansTurn() ? BattlePhase.HUMAN_EFFECT_PENDING : BattlePhase.CPU_EFFECT_PENDING);
		st.setLastMessage("効果を処理中…");
	}

	private void applyAtlantisCount2OnDeploy(CpuBattleState st, boolean deployedByHost, Map<Short, CardDefinition> defs) {
		if (st == null || defs == null || defs.get(GameConstants.SWORDFISH_TOKEN_CARD_ID) == null) {
			return;
		}
		List<BattleCard> hand = deployedByHost ? st.getHumanHand() : st.getCpuHand();
		int before = hand != null ? hand.size() : 0;
		addCopiesOfCardIdToHand(hand, GameConstants.SWORDFISH_TOKEN_CARD_ID, 1, defs);
		int after = hand != null ? hand.size() : 0;
		if (after > before) {
			String who = deployedByHost
					? (st.isPvp() ? "ホスト" : "あなた")
					: (st.isPvp() ? "ゲスト" : opponentActorLogLabel(st));
			st.addLog("深海神殿 アトランティス: " + who + "は「ソードフィッシュ」を1枚手札に加えた");
		}
	}

	/**
	 * アトランティス〈フィールド〉のカウント0を解決し、場から所有者レストへ移す。
	 * ターン開始時と、リヴァイアサン等でカウントが 0 になった直後に呼ぶ。
	 *
	 * @param fieldOwnerIsHuman 〈フィールド〉を配置した側（human スロット=true）
	 */
	private void executeAtlantisFieldCount0Resolution(CpuBattleState st, boolean fieldOwnerIsHuman,
			Map<Short, CardDefinition> defs) {
		if (st == null || st.isGameOver() || defs == null) {
			return;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != GameConstants.ATLANTIS_FIELD_CARD_ID) {
			return;
		}
		st.setAtlantisFieldCounterDisplay(0);
		List<BattleCard> rest = fieldOwnerIsHuman ? st.getHumanRest() : st.getCpuRest();
		boolean merfolk = false;
		if (rest != null) {
			for (BattleCard c : rest) {
				if (c == null) {
					continue;
				}
				CardDefinition d = defs.get(c.getCardId());
				if (d != null && restCardHasTribe(st, d, c, "MERFOLK")) {
					merfolk = true;
					break;
				}
			}
		}
		if (merfolk) {
			if (fieldOwnerIsHuman) {
				st.setHumanStones(st.getHumanStones() + 2);
			} else {
				st.setCpuStones(st.getCpuStones() + 2);
			}
			st.addLog(fieldOwnerIsHuman
					? "深海神殿 アトランティス: レストにマーフォークがいたためストーンを2つ得た"
					: "深海神殿 アトランティス: " + opponentActorLogLabel(st) + "はレストにマーフォークがいたためストーンを2つ得た");
		} else {
			st.addLog(fieldOwnerIsHuman
					? "深海神殿 アトランティス: レストにマーフォークがいなかった"
					: "深海神殿 アトランティス: " + opponentActorLogLabel(st) + "はレストにマーフォークがいなかった");
		}
		st.setAtlantisAwaitingCount0(false);
		removeActiveAtlantisFieldToOwnerRestNow(st, defs);
	}

	/** アトランティス: カウント0解決後は効果の成否にかかわらず所有者のレストへ */
	private void removeActiveAtlantisFieldToOwnerRestNow(CpuBattleState st, Map<Short, CardDefinition> defs) {
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != GameConstants.ATLANTIS_FIELD_CARD_ID) {
			return;
		}
		Boolean ownerHuman = st.getActiveFieldOwnerHuman();
		if (ownerHuman == null) {
			return;
		}
		st.setAtlantisAwaitingCount0(false);
		st.setAtlantisFieldCounterDisplay(0);
		CardDefinition fd = defs != null ? defs.get(field.getCardId()) : null;
		String nm = fd != null && fd.getName() != null ? fd.getName() : "深海神殿 アトランティス";
		if (ownerHuman) {
			st.getHumanRest().add(field);
			st.addLog(st.isPvp()
					? "〈フィールド〉「" + nm + "」はホストのレストに置かれた"
					: "〈フィールド〉「" + nm + "」はあなたのレストに置かれた");
		} else {
			st.getCpuRest().add(field);
			st.addLog(st.isPvp()
					? "〈フィールド〉「" + nm + "」はゲストのレストに置かれた"
					: "〈フィールド〉「" + nm + "」は相手のレストに置かれた");
		}
		st.setActiveField(null);
		st.setActiveFieldOwnerHuman(null);
	}

	/**
	 * アトランティス〈フィールド〉のカウント0: フィールド所有者の「次の」ターン開始時（先攻1ターン目の例外後）。
	 */
	private void maybeResolveAtlantisFieldAtFieldOwnerTurnStart(CpuBattleState st, boolean forHuman, Map<Short, CardDefinition> defs) {
		if (st == null || st.isGameOver() || defs == null) {
			return;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != GameConstants.ATLANTIS_FIELD_CARD_ID) {
			return;
		}
		if (!st.isAtlantisAwaitingCount0()) {
			return;
		}
		Boolean ownerHost = st.getActiveFieldOwnerHuman();
		if (ownerHost == null || ownerHost.booleanValue() != forHuman) {
			return;
		}
		executeAtlantisFieldCount0Resolution(st, forHuman, defs);
	}

	private void tickWorldRebuildFieldAtTurnStart(CpuBattleState st, Map<Short, CardDefinition> defs) {
		if (st == null || st.isGameOver() || defs == null) {
			return;
		}
		BattleCard f = st.getActiveField();
		if (f == null || f.getCardId() != GameConstants.WORLD_REBUILD_FIELD_CARD_ID) {
			return;
		}
		int n = st.getWorldRebuildFieldCounterDisplay();
		if (n <= 0) {
			maybeExecuteWorldRebuildFieldCount0(st, defs);
			return;
		}
		int next = n - 1;
		st.setWorldRebuildFieldCounterDisplay(next);
		if (next <= 0) {
			maybeExecuteWorldRebuildFieldCount0(st, defs);
		}
	}

	private void maybeExecuteWorldRebuildFieldCount0(CpuBattleState st, Map<Short, CardDefinition> defs) {
		if (st == null || st.isGameOver() || defs == null) {
			return;
		}
		BattleCard f = st.getActiveField();
		if (f == null || f.getCardId() != GameConstants.WORLD_REBUILD_FIELD_CARD_ID) {
			return;
		}
		if (st.getWorldRebuildFieldCounterDisplay() > 0) {
			return;
		}
		Boolean ownerHuman = st.getActiveFieldOwnerHuman();
		if (ownerHuman == null) {
			return;
		}
		List<BattleCard> rest = ownerHuman.booleanValue() ? st.getHumanRest() : st.getCpuRest();
		ZoneFighter ownBattle = ownerHuman.booleanValue() ? st.getHumanBattle() : st.getCpuBattle();
		int comics = 0;
		if (rest != null) {
			for (BattleCard c : rest) {
				if (c == null) {
					continue;
				}
				if (isTuckedUnderOwnFighter(ownBattle, c)) {
					continue;
				}
				CardDefinition d = defs.get(c.getCardId());
				if (d != null && restCardHasTribe(st, d, c, "COMIC")) {
					comics++;
				}
			}
		}
		if (comics >= 3) {
			executeWorldRebuildResetToBattleOpen(st, ownerHuman.booleanValue(), defs);
			st.addLog("世界の再構築: カウント0 — バトル開始時の手札・デッキ・ストーンに戻した");
		} else {
			st.addLog("世界の再構築: レストに「種族：コミック」が3枚未満のため、バトル開始時の状態への復元は行われなかった");
		}
		removeActiveWorldRebuildFieldToOwnerRestNow(st, defs);
	}

	/** 世界の再構築: カウント0到達後は効果の成否にかかわらず所有者のレストへ */
	private void removeActiveWorldRebuildFieldToOwnerRestNow(CpuBattleState st, Map<Short, CardDefinition> defs) {
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != GameConstants.WORLD_REBUILD_FIELD_CARD_ID) {
			return;
		}
		Boolean ownerHuman = st.getActiveFieldOwnerHuman();
		if (ownerHuman == null) {
			return;
		}
		CardDefinition fd = defs != null ? defs.get(field.getCardId()) : null;
		String nm = fd != null && fd.getName() != null ? fd.getName() : "世界の再構築";
		if (ownerHuman) {
			st.getHumanRest().add(field);
			st.addLog(st.isPvp()
					? "〈フィールド〉「" + nm + "」はホストのレストに置かれた"
					: "〈フィールド〉「" + nm + "」はあなたのレストに置かれた");
		} else {
			st.getCpuRest().add(field);
			st.addLog(st.isPvp()
					? "〈フィールド〉「" + nm + "」はゲストのレストに置かれた"
					: "〈フィールド〉「" + nm + "」は相手のレストに置かれた");
		}
		st.setActiveField(null);
		st.setActiveFieldOwnerHuman(null);
		st.setWorldRebuildFieldCounterDisplay(0);
	}

	private void executeWorldRebuildResetToBattleOpen(CpuBattleState st, boolean ownerHuman, Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		final short wb = GameConstants.WORLD_REBUILD_FIELD_CARD_ID;
		List<BattleCard> snapHand = ownerHuman ? st.getWorldRebuildOpenHumanHand() : st.getWorldRebuildOpenCpuHand();
		List<BattleCard> snapDeck = ownerHuman ? st.getWorldRebuildOpenHumanDeck() : st.getWorldRebuildOpenCpuDeck();
		if (snapHand == null || snapDeck == null) {
			return;
		}
		boolean snapHasAny = false;
		for (BattleCard t : snapHand) {
			if (t != null && t.getCardId() != wb) {
				snapHasAny = true;
				break;
			}
		}
		if (!snapHasAny) {
			for (BattleCard t : snapDeck) {
				if (t != null && t.getCardId() != wb) {
					snapHasAny = true;
					break;
				}
			}
		}
		if (!snapHasAny) {
			return;
		}
		List<BattleCard> hand = ownerHuman ? st.getHumanHand() : st.getCpuHand();
		List<BattleCard> deck = ownerHuman ? st.getHumanDeck() : st.getCpuDeck();
		List<BattleCard> rest = ownerHuman ? st.getHumanRest() : st.getCpuRest();
		ZoneFighter zone = ownerHuman ? st.getHumanBattle() : st.getCpuBattle();
		List<BattleCard> pile = new ArrayList<>();
		if (hand != null) {
			pile.addAll(hand);
			hand.clear();
		}
		if (deck != null) {
			pile.addAll(deck);
			deck.clear();
		}
		if (rest != null) {
			pile.addAll(rest);
			rest.clear();
		}
		if (zone != null) {
			List<BattleCard> under = zone.getCostUnder();
			if (under != null && !under.isEmpty()) {
				pile.addAll(under);
				under.clear();
			}
			if (zone.getMain() != null) {
				pile.add(zone.getMain());
			}
			if (ownerHuman) {
				st.setHumanBattle(null);
			} else {
				st.setCpuBattle(null);
			}
		}
		BattleCard fieldCard = st.getActiveField();
		String fieldInst = fieldCard != null ? fieldCard.getInstanceId() : null;
		if (fieldInst != null) {
			pile.removeIf(c -> c != null && fieldInst.equals(c.getInstanceId()));
		}
		Map<Short, Integer> snapCnt = new HashMap<>();
		for (BattleCard t : snapHand) {
			if (t == null || t.getCardId() == wb) {
				continue;
			}
			snapCnt.merge(t.getCardId(), 1, Integer::sum);
		}
		for (BattleCard t : snapDeck) {
			if (t == null || t.getCardId() == wb) {
				continue;
			}
			snapCnt.merge(t.getCardId(), 1, Integer::sum);
		}
		Map<Short, Integer> pileCnt = new HashMap<>();
		for (BattleCard c : pile) {
			if (c == null) {
				continue;
			}
			pileCnt.merge(c.getCardId(), 1, Integer::sum);
		}
		for (BattleCard t : snapHand) {
			if (t == null || t.getCardId() == wb) {
				continue;
			}
			hand.add(new BattleCard(UUID.randomUUID().toString(), t.getCardId()));
		}
		for (BattleCard t : snapDeck) {
			if (t == null || t.getCardId() == wb) {
				continue;
			}
			deck.add(new BattleCard(UUID.randomUUID().toString(), t.getCardId()));
		}
		for (Map.Entry<Short, Integer> e : snapCnt.entrySet()) {
			short id = e.getKey();
			int surplus = Math.max(0, pileCnt.getOrDefault(id, 0) - e.getValue());
			for (int i = 0; i < surplus; i++) {
				deck.add(new BattleCard(UUID.randomUUID().toString(), id));
			}
		}
		if (ownerHuman) {
			st.setHumanStones(st.getWorldRebuildOpenHumanStones());
			st.setHumanNextDeployBonus(0);
			st.setHumanNextElfOnlyBonus(0);
			st.setHumanNextDeployCostBonusTimes(0);
			st.setHumanNextMechanicStacks(0);
			st.setHumanNextCrystakulDeployBonus(0);
			st.setHumanKoryuBonus(0);
			st.setHumanPendingZadkielNextDeployOppTurnPower3(false);
		} else {
			st.setCpuStones(st.getWorldRebuildOpenCpuStones());
			st.setCpuNextDeployBonus(0);
			st.setCpuNextElfOnlyBonus(0);
			st.setCpuNextDeployCostBonusTimes(0);
			st.setCpuNextMechanicStacks(0);
			st.setCpuNextCrystakulDeployBonus(0);
			st.setCpuKoryuBonus(0);
			st.setCpuPendingZadkielNextDeployOppTurnPower3(false);
		}
	}

	private void beginTurnGainStone(CpuBattleState st, boolean forHuman, Map<Short, CardDefinition> defs) {
		if (st == null || st.isGameOver()) {
			return;
		}
		// ターン開始：持ち時間カウント開始（ms）
		st.setTurnStartedAtMs(System.currentTimeMillis());

		// 「次の相手ターン終了まで」系の一時効果は、所有者の次ターン開始時に切れる
		if (forHuman) {
			st.setHumanKoryuBonus(0);
			st.setHumanCrystakulCombatBonus(0);
			if (st.getHumanBattle() != null) {
				st.getHumanBattle().setBotBikeMechanicPowerBonus(0);
				st.getHumanBattle().setZadkielOpponentTurnPowerBonus(0);
			}
		} else {
			st.setCpuKoryuBonus(0);
			st.setCpuCrystakulCombatBonus(0);
			if (st.getCpuBattle() != null) {
				st.getCpuBattle().setBotBikeMechanicPowerBonus(0);
				st.getCpuBattle().setZadkielOpponentTurnPowerBonus(0);
			}
		}

		boolean isFirstPlayersFirstTurn = forHuman
				? (st.isHumanGoesFirst() && st.getHumanTurnStarts() == 0)
				: (!st.isHumanGoesFirst() && st.getCpuTurnStarts() == 0);

		if (forHuman) {
			st.setHumanTurnStarts(st.getHumanTurnStarts() + 1);
		} else {
			st.setCpuTurnStarts(st.getCpuTurnStarts() + 1);
		}

		tickScrapyardFieldAtTurnStart(st, defs);
		tickDeathbounceFieldAtTurnStart(st, defs);
		tickWeeklyShonenCampFieldAtTurnStart(st, defs);
		tickWorldRebuildFieldAtTurnStart(st, defs);
		tickPaperCityFieldAtTurnStart(st, defs);
		expireActiveCountedFieldAtCountZero(st, defs);

		if (isFirstPlayersFirstTurn) {
			st.addLog(forHuman ? "あなたの先攻1ターン目: ストーン獲得なし"
					: opponentActorLogLabel(st) + "の先攻1ターン目: ストーン獲得なし");
			return;
		}

		maybeResolveAtlantisFieldAtFieldOwnerTurnStart(st, forHuman, defs);

		if (forHuman) {
			st.setHumanStones(st.getHumanStones() + 1);
			st.addLog("ストーンを1つ得た");
		} else {
			st.setCpuStones(st.getCpuStones() + 1);
			st.addLog(opponentActorLogLabel(st) + "はストーンを1つ得た");
		}
		grantHeavensGateTurnStartMiracleForTurnOwner(st, forHuman, defs);
		applyKrakenPendingAtTurnStart(st, forHuman, defs);
		applyRamielPendingAtTurnStart(st, forHuman, defs);
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
		ns.setCpuBattleMode(st.getCpuBattleMode() != null ? st.getCpuBattleMode() : CpuBattleMode.ORIGIN);
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
		ns.setHumanNextCrystakulDeployBonus(st.getHumanNextCrystakulDeployBonus());
		ns.setCpuNextCrystakulDeployBonus(st.getCpuNextCrystakulDeployBonus());
		ns.setHumanCrystakulCombatBonus(st.getHumanCrystakulCombatBonus());
		ns.setCpuCrystakulCombatBonus(st.getCpuCrystakulCombatBonus());
		ns.setSpec666NextHumanUndead(st.isSpec666NextHumanUndead());
		ns.setSpec666NextCpuUndead(st.isSpec666NextCpuUndead());
		ns.setHumanKrakenNextTurnSwordfishAdds(st.getHumanKrakenNextTurnSwordfishAdds());
		ns.setCpuKrakenNextTurnSwordfishAdds(st.getCpuKrakenNextTurnSwordfishAdds());
		ns.setHumanRamielNextTurnMiracleAdds(st.getHumanRamielNextTurnMiracleAdds());
		ns.setCpuRamielNextTurnMiracleAdds(st.getCpuRamielNextTurnMiracleAdds());
		ns.setHumanPendingZadkielNextDeployOppTurnPower3(st.isHumanPendingZadkielNextDeployOppTurnPower3());
		ns.setCpuPendingZadkielNextDeployOppTurnPower3(st.isCpuPendingZadkielNextDeployOppTurnPower3());
		ns.setHumanMiraclesBecomeFallenLucifer(st.isHumanMiraclesBecomeFallenLucifer());
		ns.setCpuMiraclesBecomeFallenLucifer(st.isCpuMiraclesBecomeFallenLucifer());
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
		ns.setScrapyardFieldTurnsRemaining(st.getScrapyardFieldTurnsRemaining());
		ns.setDeathbounceFieldTurnsRemaining(st.getDeathbounceFieldTurnsRemaining());
		ns.setAtlantisFieldCounterDisplay(st.getAtlantisFieldCounterDisplay());
		ns.setAtlantisAwaitingCount0(st.isAtlantisAwaitingCount0());
		ns.setWeeklyShonenCampFieldCounterDisplay(st.getWeeklyShonenCampFieldCounterDisplay());
		ns.setWeeklyShonenCampCount2ComicBonus(st.isWeeklyShonenCampCount2ComicBonus());
		ns.setWeeklyShonenCampGlobalDeployCostPlusOneThisTurn(st.isWeeklyShonenCampGlobalDeployCostPlusOneThisTurn());
		ns.setWorldRebuildFieldCounterDisplay(st.getWorldRebuildFieldCounterDisplay());
		ns.setPaperCityFieldCounterDisplay(st.getPaperCityFieldCounterDisplay());
		ns.setChojuGigaPendingHumanSlotNextDeployDragon(st.isChojuGigaPendingHumanSlotNextDeployDragon());
		ns.setChojuGigaPendingCpuSlotNextDeployHuman(st.isChojuGigaPendingCpuSlotNextDeployHuman());
		ns.setWorldRebuildOpenHumanHand(copyCards(st.getWorldRebuildOpenHumanHand()));
		ns.setWorldRebuildOpenHumanDeck(copyCards(st.getWorldRebuildOpenHumanDeck()));
		ns.setWorldRebuildOpenCpuHand(copyCards(st.getWorldRebuildOpenCpuHand()));
		ns.setWorldRebuildOpenCpuDeck(copyCards(st.getWorldRebuildOpenCpuDeck()));
		ns.setWorldRebuildOpenHumanStones(st.getWorldRebuildOpenHumanStones());
		ns.setWorldRebuildOpenCpuStones(st.getWorldRebuildOpenCpuStones());
		ns.setHumanSlotDeckId(st.getHumanSlotDeckId());
		ns.setCpuSlotDeckId(st.getCpuSlotDeckId());
		ns.setBattleMainLineSeqCounter(st.getBattleMainLineSeqCounter());
		return ns;
	}

	/**
	 * 〈フィールド〉を差し替える。以前の場のカードは配置者側のレストへ移す。
	 *
	 * @param newFieldPlacedByHost true=ホスト（{@code humanTurnInteractive}）、false=ゲスト（{@code opponentTurnInteractive}）
	 */
	private void replaceActiveField(CpuBattleState st, BattleCard newField, boolean newFieldPlacedByHost, Map<Short, CardDefinition> defs) {
		BattleCard old = st.getActiveField();
		boolean oldWasSkya = old != null && old.getCardId() == MYSTERIOUS_TREE_SKYAR_FIELD_ID;
		boolean newIsSkya = newField != null && newField.getCardId() == MYSTERIOUS_TREE_SKYAR_FIELD_ID;
		boolean oldWasDeathbounce = old != null && old.getCardId() == DEATHBOUNCE_FIELD_ID;
		boolean newIsDeathbounce = newField != null && newField.getCardId() == DEATHBOUNCE_FIELD_ID;
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
		if (newField != null && newField.getCardId() == SCRAPYARD_FIELD_ID) {
			st.setScrapyardFieldTurnsRemaining(4);
			st.setDeathbounceFieldTurnsRemaining(0);
			st.setAtlantisFieldCounterDisplay(0);
			st.setAtlantisAwaitingCount0(false);
			st.setWorldRebuildFieldCounterDisplay(0);
			st.setPaperCityFieldCounterDisplay(0);
			clearWeeklyShonenCampFieldTracking(st);
		} else if (newField != null && newField.getCardId() == DEATHBOUNCE_FIELD_ID) {
			st.setScrapyardFieldTurnsRemaining(0);
			st.setDeathbounceFieldTurnsRemaining(DEATHBOUNCE_FIELD_INITIAL_TURNS);
			st.setAtlantisFieldCounterDisplay(0);
			st.setAtlantisAwaitingCount0(false);
			st.setWorldRebuildFieldCounterDisplay(0);
			st.setPaperCityFieldCounterDisplay(0);
			clearWeeklyShonenCampFieldTracking(st);
		} else if (newField != null && newField.getCardId() == GameConstants.ATLANTIS_FIELD_CARD_ID) {
			st.setScrapyardFieldTurnsRemaining(0);
			st.setDeathbounceFieldTurnsRemaining(0);
			st.setAtlantisFieldCounterDisplay(2);
			st.setAtlantisAwaitingCount0(true);
			st.setWorldRebuildFieldCounterDisplay(0);
			st.setPaperCityFieldCounterDisplay(0);
			clearWeeklyShonenCampFieldTracking(st);
			applyAtlantisCount2OnDeploy(st, newFieldPlacedByHost, defs);
		} else if (newField != null && newField.getCardId() == GameConstants.WEEKLY_SHONEN_CAMP_FIELD_CARD_ID) {
			st.setScrapyardFieldTurnsRemaining(0);
			st.setDeathbounceFieldTurnsRemaining(0);
			st.setAtlantisFieldCounterDisplay(0);
			st.setAtlantisAwaitingCount0(false);
			st.setWorldRebuildFieldCounterDisplay(0);
			st.setPaperCityFieldCounterDisplay(0);
			st.setWeeklyShonenCampFieldCounterDisplay(6);
			st.setWeeklyShonenCampCount2ComicBonus(false);
			st.setWeeklyShonenCampGlobalDeployCostPlusOneThisTurn(false);
			st.addLog("週刊少年 CAMP: カウント6 — 種族：コミックの強さ+2（場にある間）");
		} else if (newField != null && newField.getCardId() == GameConstants.WORLD_REBUILD_FIELD_CARD_ID) {
			st.setScrapyardFieldTurnsRemaining(0);
			st.setDeathbounceFieldTurnsRemaining(0);
			st.setAtlantisFieldCounterDisplay(0);
			st.setAtlantisAwaitingCount0(false);
			st.setPaperCityFieldCounterDisplay(0);
			clearWeeklyShonenCampFieldTracking(st);
			st.setWorldRebuildFieldCounterDisplay(4);
			st.addLog("世界の再構築: カウント4");
		} else if (newField != null && newField.getCardId() == GameConstants.HEAVENS_GATE_FIELD_CARD_ID) {
			st.setScrapyardFieldTurnsRemaining(0);
			st.setDeathbounceFieldTurnsRemaining(0);
			st.setAtlantisFieldCounterDisplay(0);
			st.setAtlantisAwaitingCount0(false);
			st.setWorldRebuildFieldCounterDisplay(0);
			st.setPaperCityFieldCounterDisplay(0);
			clearWeeklyShonenCampFieldTracking(st);
			grantHeavensGateDeployMiracleToPlacer(st, newFieldPlacedByHost, defs);
		} else if (newField != null && newField.getCardId() == GameConstants.PAPER_CITY_FIELD_CARD_ID) {
			st.setScrapyardFieldTurnsRemaining(0);
			st.setDeathbounceFieldTurnsRemaining(0);
			st.setAtlantisFieldCounterDisplay(0);
			st.setAtlantisAwaitingCount0(false);
			st.setWorldRebuildFieldCounterDisplay(0);
			clearWeeklyShonenCampFieldTracking(st);
			st.setPaperCityFieldCounterDisplay(6);
			applyPaperCityInkKnightToFieldOwner(st, newFieldPlacedByHost, defs);
			st.addLog("ペーパーシティ: カウント6 — 「インクナイト」を1枚手札に加えた");
		} else {
			st.setScrapyardFieldTurnsRemaining(0);
			st.setDeathbounceFieldTurnsRemaining(0);
			st.setAtlantisFieldCounterDisplay(0);
			st.setAtlantisAwaitingCount0(false);
			st.setWorldRebuildFieldCounterDisplay(0);
			st.setPaperCityFieldCounterDisplay(0);
			clearWeeklyShonenCampFieldTracking(st);
		}
		boolean chojuGigaActive = newField != null && newField.getCardId() == GameConstants.CHOJU_GIGA_FIELD_CARD_ID;
		st.setChojuGigaPendingHumanSlotNextDeployDragon(chojuGigaActive);
		st.setChojuGigaPendingCpuSlotNextDeployHuman(chojuGigaActive);
		if (chojuGigaActive) {
			st.addLog("鳥獣戯画: 次のホスト側ファイターをバトル終了まで種族・ドラゴンにし、次のゲスト／CPU側ファイターをバトル終了まで種族・人間にする");
		}
		if (oldWasSkya && !newIsSkya) {
			stripSkyaPersistedElfDeployBonusesOnFieldLoss(st, defs);
		}
		if (oldWasDeathbounce && !newIsDeathbounce) {
			stripDeathbouncePersistedHandPenalties(st);
		}
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
		if (levelUpRest > maxLevelUpRestDiscard(st.getHumanHand().size())) {
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
			if (st.getHumanNextElfOnlyBonus() > 0
					&& CardAttributes.hasAttributeForDeployPreview(mainDef, main, st.isSpec666NextHumanUndead(),
							st.getHumanNextMechanicStacks(), "ELF")) {
				deployBonus += st.getHumanNextElfOnlyBonus();
			}
			if (st.getHumanNextDeployCostBonusTimes() > 0) {
				deployBonus += deployCharacteristicCostForPowerBonuses(mainDef, main, defs, st.getHumanRest(), st)
						* st.getHumanNextDeployCostBonusTimes();
			}
			deployBonus += 3 * st.getHumanNextMechanicStacks();
			deployBonus += st.getHumanNextCrystakulDeployBonus();
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
			if (levelUpStones > 0) b.append("ストーン").append(levelUpStones).append("つ");
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
			int cost = effectiveDeployCost(mainDef, main, defs, st.getHumanRest(), st.getHumanNextMechanicStacks(), st);
			List<BattleCard> paid = new ArrayList<>();
			for (int i = 0; i < cost; i++) {
				paid.add(st.getHumanHand().remove(st.getHumanHand().size() - 1));
			}
			ZoneFighter z = new ZoneFighter();
			assignBattleZoneMain(z, main, st);
			z.setCostUnder(paid);
			z.setCostPayCardCount(cost);
			int levelUpDeployPwr = levelUpRest * 2 + levelUpStones * 2;
			applyCrystakulBonusesToDeployedZone(st, z, deployBonus, levelUpDeployPwr, true);
			applyPendingZadkielBonusToNewlyDeployedZone(st, z, true);
			// 次回配置ボーナス消費
			int mechanicStacksConsumedForTribe = st.getHumanNextMechanicStacks();
			st.setHumanNextDeployBonus(0);
			st.setHumanNextElfOnlyBonus(0);
			st.setHumanNextDeployCostBonusTimes(0);
			st.setHumanNextMechanicStacks(0);
			retireOwnBattleZoneBeforeNewDeploy(st, true, true, defs);
			st.setHumanBattle(z);
			st.addLog("あなたは「" + mainDef.getName() + "」を配置した");
			applyFieldNebulaWhenCarbuncleFighterDeployed(st, mainDef, main, true);
			CardDefinition deployAbilityDef = mainDef;
			if (mainDef.getAbilityDeployCode() != null && "NINJA".equals(mainDef.getAbilityDeployCode())) {
				applyNinjaPhysicalSwap(st, defs, true);
				BattleCard zm = st.getHumanBattle() != null ? st.getHumanBattle().getMain() : null;
				deployAbilityDef = zm != null ? defs.get(zm.getCardId()) : mainDef;
			}
			applyDeployHuman(st, deployAbilityDef, defs, main);
			applyMechanicMachineTribeToDeployedFighterIfUsed(st, true, mechanicStacksConsumedForTribe, defs);
			applyChojuGigaTribeIfPending(st, true, defs);
			if (st.getPendingChoice() == null) {
				ensureCrystakulOptionalStonePromptAfterDeployEffect(st, defs, true);
			}
			if (st.getPendingChoice() != null) {
				st.setPhase(BattlePhase.HUMAN_CHOICE);
				st.setLastMessage("選択してください");
				return;
			}

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
		resetTurnBuffs(st, defs);
		st.setHumansTurn(false);
		// CPUのターン開始：ストーン付与（先攻1ターン目のみ獲得なし）
		beginTurnGainStone(st, false, defs);
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
		if (levelUpRest > maxLevelUpRestDiscard(st.getHumanHand().size())) {
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
			cost = effectiveDeployCost(mainDef, simMain, defs, st.getHumanRest(), st.getHumanNextMechanicStacks(), st);

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
				if (st.getHumanNextElfOnlyBonus() > 0
						&& CardAttributes.hasAttributeForDeployPreview(mainDef, simMain, st.isSpec666NextHumanUndead(),
								st.getHumanNextMechanicStacks(), "ELF")) {
					deployBonus += st.getHumanNextElfOnlyBonus();
				}
				if (st.getHumanNextDeployCostBonusTimes() > 0) {
					deployBonus += deployCharacteristicCostForPowerBonuses(mainDef, simMain, defs, st.getHumanRest(), st)
							* st.getHumanNextDeployCostBonusTimes();
				}
				deployBonus += 3 * st.getHumanNextMechanicStacks();
				deployBonus += st.getHumanNextCrystakulDeployBonus();
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
			if (levelUpStones > 0) b.append("ストーン").append(levelUpStones).append("つ");
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
				if (mainDef.getId() != null && mainDef.getId() == FLEET_HO_IVI_FIELD_ID) {
					applyFleetHoIviFieldDeployBothSides(st, defs, ThreadLocalRandom.current());
				}
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
			assignBattleZoneMain(z, main, st);
			z.setCostUnder(paid);
			z.setCostPayCardCount(payIds.size());
			int levelUpDeployPwr = levelUpRest * 2 + levelUpStones * 2;
			applyCrystakulBonusesToDeployedZone(st, z, deployBonus, levelUpDeployPwr, true);
			applyPendingZadkielBonusToNewlyDeployedZone(st, z, true);
			int mechanicStacksForPendingDeploy = st.getHumanNextMechanicStacks();
			st.setHumanNextDeployBonus(0);
			st.setHumanNextElfOnlyBonus(0);
			st.setHumanNextDeployCostBonusTimes(0);
			st.setHumanNextMechanicStacks(0);
			retireOwnBattleZoneBeforeNewDeploy(st, true, true, defs);
			st.setHumanBattle(z);
			st.addLog("あなたは「" + mainDef.getName() + "」を配置した");
			// 〈探鉱の洞窟〉: 配置確定直後にストーン+1（UI の「決定」直後に数字が増える）。resolve では二重付与防止でスキップ。
			applyFieldNebulaWhenCarbuncleFighterDeployed(st, mainDef, main, true);
			stageInteractiveDeployEffectWithCrystakulOptionalFirst(st, true, mainDef, z, defs, mechanicStacksForPendingDeploy);
			return;
		} else {
			st.setConfirmAcceptLossSnapshot(null);
			st.addLog("あなたは配置をスキップした");
			// 配置しない場合、レベルアップで使ったカードはレストへ
			st.getHumanRest().addAll(levelUpCards);
			resolveKnockAndDraw(st, true, defs);
			resetTurnBuffs(st, defs);
			st.setHumansTurn(false);
			st.setPhase(BattlePhase.CPU_THINKING);
			// CPUのターン開始：ストーン付与（先攻1ターン目のみ獲得なし）
			beginTurnGainStone(st, false, defs);
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
		if (levelUpRest > maxLevelUpRestDiscard(st.getCpuHand().size())) {
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
			cost = effectiveDeployCost(mainDef, simMain, defs, st.getCpuRest(), st.getCpuNextMechanicStacks(), st);

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
				if (st.getCpuNextElfOnlyBonus() > 0
						&& CardAttributes.hasAttributeForDeployPreview(mainDef, simMain, st.isSpec666NextCpuUndead(),
								st.getCpuNextMechanicStacks(), "ELF")) {
					deployBonus += st.getCpuNextElfOnlyBonus();
				}
				if (st.getCpuNextDeployCostBonusTimes() > 0) {
					deployBonus += deployCharacteristicCostForPowerBonuses(mainDef, simMain, defs, st.getCpuRest(), st)
							* st.getCpuNextDeployCostBonusTimes();
				}
				deployBonus += 3 * st.getCpuNextMechanicStacks();
				deployBonus += st.getCpuNextCrystakulDeployBonus();
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
			if (levelUpStones > 0) b.append("ストーン").append(levelUpStones).append("つ");
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
				if (mainDef.getId() != null && mainDef.getId() == FLEET_HO_IVI_FIELD_ID) {
					applyFleetHoIviFieldDeployBothSides(st, defs, ThreadLocalRandom.current());
				}
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
			assignBattleZoneMain(z, main, st);
			z.setCostUnder(paid);
			z.setCostPayCardCount(payIds.size());
			int levelUpDeployPwrGuest = levelUpRest * 2 + levelUpStones * 2;
			applyCrystakulBonusesToDeployedZone(st, z, deployBonus, levelUpDeployPwrGuest, false);
			applyPendingZadkielBonusToNewlyDeployedZone(st, z, false);
			int mechanicStacksForPendingDeployGuest = st.getCpuNextMechanicStacks();
			st.setCpuNextDeployBonus(0);
			st.setCpuNextElfOnlyBonus(0);
			st.setCpuNextDeployCostBonusTimes(0);
			st.setCpuNextMechanicStacks(0);
			retireOwnBattleZoneBeforeNewDeploy(st, false, true, defs);
			st.setCpuBattle(z);
			st.addLog("相手は「" + mainDef.getName() + "」を配置した");
			applyFieldNebulaWhenCarbuncleFighterDeployed(st, mainDef, main, false);
			stageInteractiveDeployEffectWithCrystakulOptionalFirst(st, false, mainDef, z, defs, mechanicStacksForPendingDeployGuest);
			return;
		} else {
			st.setConfirmAcceptLossSnapshot(null);
			st.addLog("相手は配置をスキップした");
			st.getCpuRest().addAll(levelUpCards);
			resolveKnockAndDraw(st, false, defs);
			resetTurnBuffs(st, defs);
			st.setHumansTurn(true);
			st.setPhase(BattlePhase.HUMAN_INPUT);
			beginTurnGainStone(st, true, defs);
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
		resetTurnBuffs(st, defs);
		st.setHumansTurn(true);
		st.setPhase(BattlePhase.HUMAN_INPUT);
		beginTurnGainStone(st, true, defs);
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

	private static BattleCard findByInstanceId(List<BattleCard> list, String instanceId) {
		if (list == null || instanceId == null) {
			return null;
		}
		for (BattleCard c : list) {
			if (c != null && instanceId.equals(c.getInstanceId())) {
				return c;
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
		int cost = effectiveDeployCost(d, main, defs, rest, mech, st);
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

	/**
	 * 忍者のコスト先頭（入れ替え対象）の選び方。
	 * {@link #legacy()} は従来どおり手札末尾から支払う。{@link #skip()} はこの忍者配置案を棄却する。
	 */
	private record NinjaFirstCostPick(boolean skipped, String firstPaidInstanceIdForCharacteristic) {
		static NinjaFirstCostPick legacy() {
			return new NinjaFirstCostPick(false, null);
		}

		static NinjaFirstCostPick skip() {
			return new NinjaFirstCostPick(true, null);
		}

		static NinjaFirstCostPick choice(String firstInstanceId) {
			return new NinjaFirstCostPick(false, firstInstanceId);
		}
	}

	/** 忍者の〈配置〉シミュレーション: 入れ替え後メインで {@link #applyDeployCpu}（従来は忍者のまま計測されていた）。 */
	private void cpuSimApplyDeployAbilitiesAfterZonePlaced(CpuBattleState simSt, CardDefinition originalMainDef,
			Map<Short, CardDefinition> defs, Random rnd) {
		if (simSt.getCpuBattle() == null || originalMainDef == null) {
			return;
		}
		BattleCard zoneMain = simSt.getCpuBattle().getMain();
		if (originalMainDef.getAbilityDeployCode() != null && "NINJA".equals(originalMainDef.getAbilityDeployCode())
				&& simSt.getCpuBattle().getCostPayCardCount() > 0) {
			applyNinjaPhysicalSwap(simSt, defs, false);
			BattleCard dm = simSt.getCpuBattle() != null ? simSt.getCpuBattle().getMain() : null;
			CardDefinition dd = dm != null ? defs.get(dm.getCardId()) : null;
			if (dd != null) {
				applyDeployCpu(simSt, dd, defs, rnd, dm);
			}
			return;
		}
		applyDeployCpu(simSt, originalMainDef, defs, rnd, zoneMain);
	}

	private static int compareInstanceIdsNullSafe(String a, String b) {
		if (a == null && b == null) {
			return 0;
		}
		if (a == null) {
			return -1;
		}
		if (b == null) {
			return 1;
		}
		return a.compareTo(b);
	}

	private List<BattleCard> cpuTakeCharacteristicCostPaymentFromHand(List<BattleCard> hand, int payCards,
			NinjaFirstCostPick pick) {
		if (payCards <= 0) {
			return new ArrayList<>();
		}
		if (pick.skipped()) {
			return new ArrayList<>();
		}
		List<BattleCard> paid = new ArrayList<>();
		if (pick.firstPaidInstanceIdForCharacteristic() != null) {
			BattleCard first = removeByInstanceId(hand, pick.firstPaidInstanceIdForCharacteristic());
			if (first == null) {
				return null;
			}
			paid.add(first);
			for (int i = 1; i < payCards; i++) {
				if (hand.isEmpty()) {
					while (paid.size() > 1) {
						hand.add(paid.remove(paid.size() - 1));
					}
					hand.add(0, paid.remove(0));
					return null;
				}
				paid.add(hand.remove(hand.size() - 1));
			}
		} else {
			for (int i = 0; i < payCards; i++) {
				if (hand.isEmpty()) {
					break;
				}
				paid.add(hand.remove(hand.size() - 1));
			}
		}
		return paid;
	}

	private Optional<BattleCard> bestCpuNinjaSwapCostAmongTier(CpuBattleState simHandAfterMainRemovedTemplate,
			BattleCard ninjaMain, CardDefinition ninjaDef, int payCards, int deployBonus, int levelUpDeployPowerBonus,
			List<BattleCard> tier, Map<Short, CardDefinition> defs, long simSalt, boolean hasOpp) {
		BattleCard best = null;
		int bestCpuEff = hasOpp ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		for (BattleCard cand : tier) {
			CpuBattleState trial = copyStateForCpuSim(simHandAfterMainRemovedTemplate);
			List<BattleCard> hand = trial.getCpuHand();
			BattleCard first = removeByInstanceId(hand, cand.getInstanceId());
			if (first == null) {
				continue;
			}
			List<BattleCard> paid = new ArrayList<>();
			paid.add(first);
			boolean bad = false;
			for (int k = 1; k < payCards; k++) {
				if (hand.isEmpty()) {
					bad = true;
					break;
				}
				paid.add(hand.remove(hand.size() - 1));
			}
			if (bad || paid.size() != payCards) {
				continue;
			}
			ZoneFighter z = new ZoneFighter();
			assignBattleZoneMain(z, ninjaMain, trial);
			z.setCostUnder(paid);
			z.setCostPayCardCount(payCards);
			applyCrystakulBonusesToDeployedZone(trial, z, deployBonus, levelUpDeployPowerBonus, false);
			applyPendingZadkielBonusToNewlyDeployedZone(trial, z, false);
			retireOwnBattleZoneBeforeNewDeploy(trial, false, false, defs);
			trial.setCpuBattle(z);
			long salt = simSalt ^ (cand.getCardId() * 31L);
			String cid = cand.getInstanceId();
			if (cid != null) {
				salt ^= cid.hashCode();
			}
			Random trialRnd = new Random(31_337L ^ salt);
			cpuSimApplyDeployAbilitiesAfterZonePlaced(trial, ninjaDef, defs, trialRnd);
			int cpuEff = effectiveBattlePower(trial.getCpuBattle(), false, trial, defs);
			int oppEff = effectiveBattlePower(trial.getHumanBattle(), true, trial, defs);
			if (hasOpp && cpuEff < oppEff) {
				continue;
			}
			if (hasOpp) {
				if (best == null || cpuEff < bestCpuEff
						|| (cpuEff == bestCpuEff && compareInstanceIdsNullSafe(cand.getInstanceId(), best.getInstanceId()) < 0)) {
					best = cand;
					bestCpuEff = cpuEff;
				}
			} else {
				if (best == null || cpuEff > bestCpuEff
						|| (cpuEff == bestCpuEff && compareInstanceIdsNullSafe(cand.getInstanceId(), best.getInstanceId()) < 0)) {
					best = cand;
					bestCpuEff = cpuEff;
				}
			}
		}
		return Optional.ofNullable(best);
	}

	/**
	 * 忍者: 手札ファイターを印字コストの高い順に試し、入れ替え後に相手以上ならその先頭コストを採用。
	 * どのコスト帯でも足りなければ {@link NinjaFirstCostPick#skip()}（レベルアップ等の別案へ）。
	 */
	private NinjaFirstCostPick pickCpuNinjaCharacteristicFirstCost(CpuBattleState simTemplateAfterMainRemoved,
			BattleCard ninjaMain, CardDefinition ninjaDef, int payCards, int deployBonus, int levelUpDeployPowerBonus,
			CpuBattleState stCostContext, Map<Short, CardDefinition> defs, long simSalt) {
		if (ninjaDef == null || ninjaDef.getAbilityDeployCode() == null
				|| !"NINJA".equals(ninjaDef.getAbilityDeployCode()) || payCards <= 0) {
			return NinjaFirstCostPick.legacy();
		}
		List<BattleCard> fighters = new ArrayList<>();
		for (BattleCard c : simTemplateAfterMainRemoved.getCpuHand()) {
			if (c == null) {
				continue;
			}
			CardDefinition cd = defs.get(c.getCardId());
			if (!isNonFieldFighterCardDef(cd)) {
				continue;
			}
			fighters.add(c);
		}
		if (fighters.isEmpty()) {
			return NinjaFirstCostPick.skip();
		}
		fighters.sort((a, b) -> {
			int ca = effectiveDeployCost(defs.get(a.getCardId()), a, defs,
					stCostContext.getCpuRest(), stCostContext.getCpuNextMechanicStacks(), stCostContext);
			int cb = effectiveDeployCost(defs.get(b.getCardId()), b, defs,
					stCostContext.getCpuRest(), stCostContext.getCpuNextMechanicStacks(), stCostContext);
			int cmp = Integer.compare(cb, ca);
			if (cmp != 0) {
				return cmp;
			}
			return compareInstanceIdsNullSafe(a.getInstanceId(), b.getInstanceId());
		});
		boolean hasOpp = simTemplateAfterMainRemoved.getHumanBattle() != null;
		int i = 0;
		while (i < fighters.size()) {
			int tierCost = effectiveDeployCost(defs.get(fighters.get(i).getCardId()), fighters.get(i), defs,
					stCostContext.getCpuRest(), stCostContext.getCpuNextMechanicStacks(), stCostContext);
			int j = i + 1;
			while (j < fighters.size()) {
				int cj = effectiveDeployCost(defs.get(fighters.get(j).getCardId()), fighters.get(j), defs,
						stCostContext.getCpuRest(), stCostContext.getCpuNextMechanicStacks(), stCostContext);
				if (cj != tierCost) {
					break;
				}
				j++;
			}
			Optional<BattleCard> best = bestCpuNinjaSwapCostAmongTier(simTemplateAfterMainRemoved, ninjaMain, ninjaDef,
					payCards, deployBonus, levelUpDeployPowerBonus, fighters.subList(i, j), defs, simSalt, hasOpp);
			if (best.isPresent()) {
				return NinjaFirstCostPick.choice(best.get().getInstanceId());
			}
			i = j;
		}
		return NinjaFirstCostPick.skip();
	}

	/**
	 * 「漫画家」〈配置〉: このファイターの現在の強さ（バトル列の表示と同じ計算＝レベルアップ等の加算を含む）に等しい
	 * 「もとの強さ」のランダムなファイター1枚を手札に加える。種族は問わない。
	 */
	private void applyMangakaDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs, Random rnd) {
		if (st == null || defs == null) {
			return;
		}
		String prefix = cpuAiDeploy ? "CPU漫画家" : "漫画家";
		ZoneFighter zf = deployerIsHuman ? st.getHumanBattle() : st.getCpuBattle();
		if (zf == null || zf.getMain() == null) {
			return;
		}
		// 前列の強さが相手に及ばないときは〈配置〉を発動させない（敗北確認の裏で手札が増えるのを防ぐ）
		if (deployerIsHuman) {
			if (st.getHumanBattle() != null && st.getCpuBattle() != null) {
				int me = effectiveBattlePower(st.getHumanBattle(), true, st, defs);
				int opp = effectiveBattlePower(st.getCpuBattle(), false, st, defs);
				if (me < opp) {
					st.addLog(prefix + ": 強さが及ばないため〈配置〉は発動しない");
					return;
				}
			}
		} else {
			if (st.getHumanBattle() != null && st.getCpuBattle() != null) {
				int me = effectiveBattlePower(st.getCpuBattle(), false, st, defs);
				int opp = effectiveBattlePower(st.getHumanBattle(), true, st, defs);
				if (me < opp) {
					st.addLog(prefix + ": 強さが及ばないため〈配置〉は発動しない");
					return;
				}
			}
		}
		int p = effectiveBattlePower(zf, deployerIsHuman, st, defs);
		List<Short> ids = new ArrayList<>();
		for (CardDefinition cd : defs.values()) {
			if (cd == null || cd.getId() == null) {
				continue;
			}
			if (GameConstants.excludedFromPackOpenAndLibraryListing(cd.getId())) {
				continue;
			}
			if (!isNonFieldFighterCardDef(cd)) {
				continue;
			}
			int bp = cd.getBasePower() != null ? cd.getBasePower() : 0;
			if (bp == p) {
				ids.add(cd.getId());
			}
		}
		if (ids.isEmpty()) {
			st.addLog(prefix + ": 強さ" + p + "に合うファイターが定義にない");
			return;
		}
		Random r = rnd != null ? rnd : ThreadLocalRandom.current();
		short pick = ids.get(r.nextInt(ids.size()));
		List<BattleCard> hand = deployerIsHuman ? st.getHumanHand() : st.getCpuHand();
		addCopiesOfCardIdToHand(hand, pick, 1, defs);
		CardDefinition pickedDef = defs.get(pick);
		String pickedName = pickedDef != null && pickedDef.getName() != null ? pickedDef.getName() : "？";
		st.addLog(prefix + ": 現在の強さ" + p + "に合うファイターとして「" + pickedName + "」を手札に加えた");
	}

	/**
	 * マーメイド〈配置〉: ストーン+1。手札に残っている各「ソードフィッシュ」にバトル終了まで強さ+2（配置したマーメイドは手札にいない）。
	 */
	private void applyMermaidDeployEffect(CpuBattleState st, List<BattleCard> deployerHand, boolean stonesToHuman,
			boolean cpuAiDeploy, Map<Short, CardDefinition> defs) {
		if (st == null) {
			return;
		}
		if (stonesToHuman) {
			st.setHumanStones(st.getHumanStones() + 1);
		} else {
			st.setCpuStones(st.getCpuStones() + 1);
		}
		String logName = cpuAiDeploy ? "CPUマーメイド" : "マーメイド";
		st.addLog(logName + ": ストーン+1");
		if (defs == null || defs.get(GameConstants.SWORDFISH_TOKEN_CARD_ID) == null || deployerHand == null) {
			return;
		}
		int count = 0;
		for (BattleCard c : deployerHand) {
			if (c != null && c.getCardId() == GameConstants.SWORDFISH_TOKEN_CARD_ID) {
				c.setBattleEndPowerBonus(c.getBattleEndPowerBonus() + MERMAID_SWORDFISH_POWER_BONUS_EACH);
				count++;
			}
		}
		if (count > 0) {
			st.addLog(logName + ": 手札の「ソードフィッシュ」" + count + "枚をバトル終了まで強さ+"
					+ MERMAID_SWORDFISH_POWER_BONUS_EACH);
		} else {
			st.addLog(logName + ": 手札に「ソードフィッシュ」がなかった");
		}
	}

	private static boolean restContainsSwordfishToken(List<BattleCard> rest) {
		if (rest == null) {
			return false;
		}
		for (BattleCard c : rest) {
			if (c != null && c.getCardId() == GameConstants.SWORDFISH_TOKEN_CARD_ID) {
				return true;
			}
		}
		return false;
	}

	private static String sirenDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "セイレーン";
		}
		if (cpuAiDeploy) {
			return "CPUセイレーン";
		}
		return cpuSlotActorLogLabel(st) + "のセイレーン";
	}

	private static void transformStolenFighterToSwordfishTokenInPlace(BattleCard c) {
		if (c == null) {
			return;
		}
		c.setCardId(GameConstants.SWORDFISH_TOKEN_CARD_ID);
		c.setBlankEffects(false);
		c.setHandDeployCostModifier(0);
		c.setDeathbounceHandCostStacks(0);
		c.setBattleTribeOverride(null);
		c.setBattleEndPowerBonus(0);
	}

	/**
	 * セイレーン〈配置〉: 自分のレストに「ソードフィッシュ」があるとき、相手前列のファイターを手札に加え「ソードフィッシュ」に変化。
	 */
	private void applySirenDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null || defs.get(GameConstants.SWORDFISH_TOKEN_CARD_ID) == null) {
			return;
		}
		String logP = sirenDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		List<BattleCard> deployerRest = deployerIsHuman ? st.getHumanRest() : st.getCpuRest();
		if (!restContainsSwordfishToken(deployerRest)) {
			st.addLog(logP + ": レストに「ソードフィッシュ」がないため効果はなかった");
			return;
		}
		ZoneFighter oppZone = deployerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
		if (oppZone == null || oppZone.getMain() == null) {
			st.addLog(logP + ": 相手バトルゾーンにファイターがいなかった");
			return;
		}
		CardDefinition oppDef = defs.get(oppZone.getMain().getCardId());
		if (oppDef == null || isFieldCard(oppDef)) {
			st.addLog(logP + ": 相手バトルゾーンにファイターがいなかった");
			return;
		}
		List<BattleCard> oppRest = deployerIsHuman ? st.getCpuRest() : st.getHumanRest();
		List<BattleCard> deployerHand = deployerIsHuman ? st.getHumanHand() : st.getCpuHand();
		for (BattleCard under : new ArrayList<>(oppZone.getCostUnder())) {
			oppRest.add(under);
		}
		oppZone.getCostUnder().clear();
		BattleCard stolen = oppZone.getMain();
		if (deployerIsHuman) {
			st.setCpuBattle(null);
		} else {
			st.setHumanBattle(null);
		}
		transformStolenFighterToSwordfishTokenInPlace(stolen);
		deployerHand.add(0, stolen);
		st.addLog(logP + ": 相手のファイターを手札に加え「ソードフィッシュ」に変化させた");
	}

	private static String poseidonDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "ポセイドン";
		}
		if (cpuAiDeploy) {
			return "CPUポセイドン";
		}
		return cpuSlotActorLogLabel(st) + "のポセイドン";
	}

	/**
	 * ポセイドン〈配置〉: 自分のターンの終わりまでこの前列の強さ+3。レストの「ソードフィッシュ」を最大3枚手札に加える。
	 */
	private void applyPoseidonDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null || defs.get(GameConstants.SWORDFISH_TOKEN_CARD_ID) == null) {
			return;
		}
		String logP = poseidonDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		ZoneFighter z = deployerIsHuman ? st.getHumanBattle() : st.getCpuBattle();
		if (z != null) {
			z.setTemporaryPowerBonus(z.getTemporaryPowerBonus() + POSEIDON_DEPLOY_TEMPORARY_POWER);
		}
		st.addLog(logP + ": 自分のターンの終わりまで強さ+" + POSEIDON_DEPLOY_TEMPORARY_POWER);
		List<BattleCard> rest = deployerIsHuman ? st.getHumanRest() : st.getCpuRest();
		List<BattleCard> hand = deployerIsHuman ? st.getHumanHand() : st.getCpuHand();
		int moved = 0;
		if (rest != null && hand != null) {
			for (int i = rest.size() - 1; i >= 0; i--) {
				BattleCard c = rest.get(i);
				if (c == null) {
					continue;
				}
				if (isTuckedUnderOwnFighter(z, c)) {
					continue;
				}
				if (c.getCardId() == GameConstants.SWORDFISH_TOKEN_CARD_ID) {
					rest.remove(i);
					hand.add(0, c);
					moved++;
					if (moved >= POSEIDON_DEPLOY_MAX_SWORDFISH_FROM_REST) {
						break;
					}
				}
			}
		}
		if (moved > 0) {
			st.addLog(logP + ": レストの「ソードフィッシュ」を" + moved + "枚手札に加えた");
		} else {
			st.addLog(logP + ": レストに「ソードフィッシュ」がなかった");
		}
	}

	private static int countInkKnightsInHand(List<BattleCard> hand) {
		if (hand == null) {
			return 0;
		}
		int n = 0;
		for (BattleCard c : hand) {
			if (c != null && c.getCardId() == INK_KNIGHT_ID) {
				n++;
			}
		}
		return n;
	}

	private static String kingMakerDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "キングメーカー";
		}
		if (cpuAiDeploy) {
			return "CPUキングメーカー";
		}
		return cpuSlotActorLogLabel(st) + "のキングメーカー";
	}

	private static String inkKingDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "インクキング";
		}
		if (cpuAiDeploy) {
			return "CPUインクキング";
		}
		return cpuSlotActorLogLabel(st) + "のインクキング";
	}

	/**
	 * キングメーカー〈配置〉: 手札に「インクナイト」が2枚以上あるなら「インクキング」を1枚手札に加える（配置済みメインは手札に含まない）。
	 */
	private void applyKingMakerDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null || defs.get(INK_KING_ID) == null) {
			return;
		}
		List<BattleCard> hand = deployerIsHuman ? st.getHumanHand() : st.getCpuHand();
		int ink = countInkKnightsInHand(hand);
		String logP = kingMakerDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		if (ink >= 2) {
			addCopiesOfCardIdToHand(hand, INK_KING_ID, 1, defs);
			st.addLog(logP + ": 「インクキング」を1枚手札に加えた");
		} else {
			st.addLog(logP + ": 手札の「インクナイト」が2枚未満のため効果はなかった");
		}
	}

	/**
	 * インクキング〈配置〉: 自分のターンの終わりまでこの前列の強さ+4。自分のレストの「インクナイト」をすべて手札に加える。
	 */
	private void applyInkKingDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		String logP = inkKingDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		ZoneFighter z = deployerIsHuman ? st.getHumanBattle() : st.getCpuBattle();
		if (z != null) {
			z.setTemporaryPowerBonus(z.getTemporaryPowerBonus() + INK_KING_DEPLOY_TEMPORARY_POWER);
		}
		st.addLog(logP + ": 自分のターンの終わりまで強さ+" + INK_KING_DEPLOY_TEMPORARY_POWER);
		List<BattleCard> rest = deployerIsHuman ? st.getHumanRest() : st.getCpuRest();
		List<BattleCard> hand = deployerIsHuman ? st.getHumanHand() : st.getCpuHand();
		int moved = 0;
		if (rest != null && hand != null && z != null) {
			for (int i = rest.size() - 1; i >= 0; i--) {
				BattleCard c = rest.get(i);
				if (c == null) {
					continue;
				}
				if (isTuckedUnderOwnFighter(z, c)) {
					continue;
				}
				if (c.getCardId() == INK_KNIGHT_ID) {
					rest.remove(i);
					hand.add(0, c);
					moved++;
				}
			}
		}
		if (moved > 0) {
			st.addLog(logP + ": レストの「インクナイト」を" + moved + "枚手札に加えた");
		} else {
			st.addLog(logP + ": レストに「インクナイト」がなかった");
		}
	}

	private static int countMinionSoldiersInHand(List<BattleCard> hand) {
		if (hand == null) {
			return 0;
		}
		int n = 0;
		for (BattleCard c : hand) {
			if (c != null && c.getCardId() == MINION_SOLDIER_ID) {
				n++;
			}
		}
		return n;
	}

	private static String dominionDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "ドミニオン";
		}
		if (cpuAiDeploy) {
			return "CPUドミニオン";
		}
		return cpuSlotActorLogLabel(st) + "のドミニオン";
	}

	/**
	 * ドミニオン〈配置〉: 手札に「ミニオンソルジャー」があれば「ミニオンチャンピオン」を1枚加える。そうでなければ手札をすべて「ミニオンソルジャー」に変化。
	 */
	private void applyDominionDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs) {
		if (st == null) {
			return;
		}
		List<BattleCard> hand = deployerIsHuman ? st.getHumanHand() : st.getCpuHand();
		String logP = dominionDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		if (hand == null) {
			return;
		}
		if (countMinionSoldiersInHand(hand) >= 1) {
			if (defs == null || defs.get(MINION_CHAMPION_ID) == null) {
				st.addLog(logP + ": 「ミニオンチャンピオン」の定義がない");
				return;
			}
			addCopiesOfCardIdToHand(hand, MINION_CHAMPION_ID, 1, defs);
			st.addLog(logP + ": 「ミニオンチャンピオン」を1枚手札に加えた");
			return;
		}
		if (defs == null || defs.get(MINION_SOLDIER_ID) == null) {
			st.addLog(logP + ": 「ミニオンソルジャー」の定義がない");
			return;
		}
		int n = 0;
		for (BattleCard c : hand) {
			if (c == null) {
				continue;
			}
			c.setCardId(MINION_SOLDIER_ID);
			n++;
		}
		st.addLog(logP + ": 手札のカードを「ミニオンソルジャー」に" + n + "枚変化させた");
	}

	private static String minionSoldierDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "ミニオンソルジャー";
		}
		if (cpuAiDeploy) {
			return "CPUミニオンソルジャー";
		}
		return cpuSlotActorLogLabel(st) + "のミニオンソルジャー";
	}

	/** ミニオンソルジャー〈配置〉: 自分ターン終了までこの前列の強さ+3。 */
	private void applyMinionSoldierDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null || defs.get(MINION_SOLDIER_ID) == null) {
			return;
		}
		String logP = minionSoldierDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		ZoneFighter z = deployerIsHuman ? st.getHumanBattle() : st.getCpuBattle();
		if (z != null) {
			z.setTemporaryPowerBonus(z.getTemporaryPowerBonus() + MINION_SOLDIER_DEPLOY_TEMPORARY_POWER);
		}
		st.addLog(logP + ": ターンの終わりまで強さ+" + MINION_SOLDIER_DEPLOY_TEMPORARY_POWER);
	}

	private static String ramielDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "ラミエル";
		}
		if (cpuAiDeploy) {
			return "CPUラミエル";
		}
		return cpuSlotActorLogLabel(st) + "のラミエル";
	}

	/** ラミエル〈配置〉（ストーン1使用後）: 次の自分のターン開始時に「奇跡」を1枚手札に加える（重ねがけ可）。 */
	private static String mikaelStrategyDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "ミカエルの戦略";
		}
		if (cpuAiDeploy) {
			return "CPUミカエルの戦略";
		}
		return cpuSlotActorLogLabel(st) + "のミカエルの戦略";
	}

	/** ミカエルの戦略〈配置〉: 「奇跡」を1枚手札に加える。 */
	private void applyMikaelStrategyDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		String logP = mikaelStrategyDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		List<BattleCard> hand = deployerIsHuman ? st.getHumanHand() : st.getCpuHand();
		if (!canGrantMiracleSlotCard(st, deployerIsHuman, defs)) {
			st.addLog(logP + ": 「奇跡」の定義がない");
			return;
		}
		addMiracleCopiesToHandForPlayer(hand, 1, st, deployerIsHuman, defs);
		short gid = miracleGrantCardId(st, deployerIsHuman, defs);
		CardDefinition gd = defs.get(gid);
		String nm = gd != null && gd.getName() != null ? gd.getName() : "奇跡";
		st.addLog(logP + ": 「" + nm + "」を1枚手札に加えた");
	}

	private void applyRamielDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (st == null) {
			return;
		}
		String logP = ramielDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		if (deployerIsHuman) {
			st.setHumanRamielNextTurnMiracleAdds(st.getHumanRamielNextTurnMiracleAdds() + 1);
		} else {
			st.setCpuRamielNextTurnMiracleAdds(st.getCpuRamielNextTurnMiracleAdds() + 1);
		}
		st.addLog(logP + ": 次の自分のターンの開始時に「奇跡」を1枚手札に加える");
	}

	private String luciferDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "ルシファー";
		}
		if (cpuAiDeploy) {
			return "CPUルシファー";
		}
		return cpuSlotActorLogLabel(st) + "のルシファー";
	}

	private String fallenAngelLuciferDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "堕天使ルシファー";
		}
		if (cpuAiDeploy) {
			return "CPU堕天使ルシファー";
		}
		return cpuSlotActorLogLabel(st) + "の堕天使ルシファー";
	}

	/** ルシファー〈配置〉: 「奇跡」1枚を手札に加え、以降の「奇跡」付与は堕天使となり、既存の奇跡も変化する。 */
	private void applyLuciferDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		String logP = luciferDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		if (defs.get(GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID) == null) {
			st.addLog(logP + ": 「堕天使ルシファー」の定義がない");
			return;
		}
		List<BattleCard> hand = deployerIsHuman ? st.getHumanHand() : st.getCpuHand();
		boolean addedMiracle = defs.get(GameConstants.MIRACLE_TOKEN_CARD_ID) != null;
		if (addedMiracle) {
			addCopiesOfCardIdToHand(hand, GameConstants.MIRACLE_TOKEN_CARD_ID, 1, defs);
		}
		if (deployerIsHuman) {
			st.setHumanMiraclesBecomeFallenLucifer(true);
		} else {
			st.setCpuMiraclesBecomeFallenLucifer(true);
		}
		replaceMiraclesWithFallenLuciferInPlayerZones(st, deployerIsHuman, defs);
		if (addedMiracle) {
			st.addLog(logP + ": 「奇跡」を1枚手札に加えた。バトル終了まで、自分の「奇跡」はすべて「堕天使ルシファー」になった");
		} else {
			st.addLog(logP + ": 「奇跡」の定義がない。バトル終了まで、自分の「奇跡」はすべて「堕天使ルシファー」になった");
		}
	}

	/** 堕天使ルシファー〈配置〉: 手札の「種族：アンデッド」ファイターにバトル終了まで強さ+1。 */
	private void applyFallenAngelLuciferDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		List<BattleCard> hand = deployerIsHuman ? st.getHumanHand() : st.getCpuHand();
		String logP = fallenAngelLuciferDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		int n = 0;
		if (hand != null) {
			for (BattleCard c : hand) {
				if (c == null) {
					continue;
				}
				CardDefinition cd = defs.get(c.getCardId());
				if (!isNonFieldFighterCardDef(cd)) {
					continue;
				}
				if (!restCardHasTribe(st, cd, c, "UNDEAD")) {
					continue;
				}
				c.setBattleEndPowerBonus(c.getBattleEndPowerBonus() + 1);
				n++;
			}
		}
		st.addLog(logP + ": 手札のアンデッド・ファイター" + n + "枚にバトル終了まで強さ+1");
	}

	private String sketcherDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "スケッチャー";
		}
		if (cpuAiDeploy) {
			return "CPUスケッチャー";
		}
		return cpuSlotActorLogLabel(st) + "のスケッチャー";
	}

	/**
	 * スケッチャー〈配置〉: 「インクナイト」を1枚自分レストへ。手札1枚を選びそのコピーを手札に加える（CPU戦は自動選択）。
	 */
	private void applySketcherDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs, Random rnd) {
		if (st == null || defs == null || defs.get(INK_KNIGHT_ID) == null) {
			return;
		}
		List<BattleCard> rest = deployerIsHuman ? st.getHumanRest() : st.getCpuRest();
		List<BattleCard> hand = deployerIsHuman ? st.getHumanHand() : st.getCpuHand();
		String logP = sketcherDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		if (rest != null) {
			rest.add(new BattleCard(UUID.randomUUID().toString(), INK_KNIGHT_ID));
		}
		st.addLog(logP + ": 「インクナイト」を1枚レストに加えた");
		if (hand == null) {
			return;
		}
		if (cpuAiDeploy) {
			if (hand.isEmpty()) {
				st.addLog(logP + ": 手札がないためコピーはなかった");
				return;
			}
			Random r = rnd != null ? rnd : ThreadLocalRandom.current();
			int ri = r.nextInt(hand.size());
			BattleCard src = hand.get(ri);
			BattleCard cp = copyCard(src);
			cp.setInstanceId(UUID.randomUUID().toString());
			hand.add(0, cp);
			st.addLog(logP + ": 手札のカードのコピーを1枚手札に加えた");
			return;
		}
		List<String> opts = new ArrayList<>();
		for (BattleCard hc : hand) {
			if (hc != null && hc.getInstanceId() != null) {
				opts.add(hc.getInstanceId());
			}
		}
		if (!opts.isEmpty()) {
			st.setPendingChoice(new PendingChoice(
					ChoiceKind.SELECT_ONE_FROM_HAND_FOR_SKETCHER_COPY,
					logP + "（コピーする手札を1枚選ぶ）",
					deployerIsHuman,
					SKETCHER_DEPLOY_CODE,
					0,
					opts,
					!deployerIsHuman));
		} else {
			st.addLog(logP + ": 手札がないためコピーはなかった");
		}
	}

	/**
	 * クラーケン: 〈配置〉で付いた「次の自分ターン開始時」予約を、ストーン獲得の直後に処理する。
	 */
	private void applyKrakenPendingAtTurnStart(CpuBattleState st, boolean forHuman, Map<Short, CardDefinition> defs) {
		if (st == null || defs == null || defs.get(GameConstants.SWORDFISH_TOKEN_CARD_ID) == null) {
			return;
		}
		int pending = forHuman ? st.getHumanKrakenNextTurnSwordfishAdds() : st.getCpuKrakenNextTurnSwordfishAdds();
		if (pending <= 0) {
			return;
		}
		List<BattleCard> rest = forHuman ? st.getHumanRest() : st.getCpuRest();
		List<BattleCard> hand = forHuman ? st.getHumanHand() : st.getCpuHand();
		if (!restContainsSwordfishToken(rest)) {
			if (forHuman) {
				st.setHumanKrakenNextTurnSwordfishAdds(0);
				st.addLog("クラーケン: レストに「ソードフィッシュ」がないため、手札への追加はなかった");
			} else {
				st.setCpuKrakenNextTurnSwordfishAdds(0);
				st.addLog(cpuSlotActorLogLabel(st) + "のクラーケン: レストに「ソードフィッシュ」がないため、手札への追加はなかった");
			}
			return;
		}
		String okLogPrefix = forHuman ? "クラーケン" : cpuSlotActorLogLabel(st) + "のクラーケン";
		while (pending > 0 && restContainsSwordfishToken(rest)) {
			addCopiesOfCardIdToHand(hand, GameConstants.SWORDFISH_TOKEN_CARD_ID, 1, defs);
			pending--;
			st.addLog(okLogPrefix + ": 「ソードフィッシュ」を1枚手札に加えた");
		}
		if (forHuman) {
			st.setHumanKrakenNextTurnSwordfishAdds(pending);
		} else {
			st.setCpuKrakenNextTurnSwordfishAdds(pending);
		}
	}

	/**
	 * ラミエル: 〈配置〉で付いた「次の自分ターン開始時に奇跡」を、ストーン獲得の直後に処理する。
	 */
	private void applyRamielPendingAtTurnStart(CpuBattleState st, boolean forHuman, Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		short mid = GameConstants.MIRACLE_TOKEN_CARD_ID;
		if (defs.get(mid) == null && defs.get(GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID) == null) {
			if (forHuman) {
				st.setHumanRamielNextTurnMiracleAdds(0);
			} else {
				st.setCpuRamielNextTurnMiracleAdds(0);
			}
			return;
		}
		int pending = forHuman ? st.getHumanRamielNextTurnMiracleAdds() : st.getCpuRamielNextTurnMiracleAdds();
		if (pending <= 0) {
			return;
		}
		List<BattleCard> hand = forHuman ? st.getHumanHand() : st.getCpuHand();
		String logP = forHuman ? "ラミエル" : cpuSlotActorLogLabel(st) + "のラミエル";
		addMiracleCopiesToHandForPlayer(hand, pending, st, forHuman, defs);
		if (forHuman) {
			st.setHumanRamielNextTurnMiracleAdds(0);
		} else {
			st.setCpuRamielNextTurnMiracleAdds(0);
		}
		short gid = miracleGrantCardId(st, forHuman, defs);
		String cardName = defs.get(gid) != null && defs.get(gid).getName() != null ? defs.get(gid).getName() : "？";
		st.addLog(logP + ": 「" + cardName + "」を" + pending + "枚手札に加えた");
	}

	private BattleCard copyCard(BattleCard c) {
		if (c == null) return null;
		BattleCard n = new BattleCard(c.getInstanceId(), c.getCardId(), c.isBlankEffects());
		n.setHandDeployCostModifier(c.getHandDeployCostModifier());
		n.setDeathbounceHandCostStacks(c.getDeathbounceHandCostStacks());
		n.setBattleTribeOverride(c.getBattleTribeOverride());
		n.setBattleEndPowerBonus(c.getBattleEndPowerBonus());
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
		nz.setLevelUpDeployPowerBonus(z.getLevelUpDeployPowerBonus());
		nz.setNinjaSwapPowerPenalty(z.getNinjaSwapPowerPenalty());
		nz.setCostPayCardCount(z.getCostPayCardCount());
		nz.setReturnToHandOnKnock(z.isReturnToHandOnKnock());
		nz.setFieldNebulaStoneGrantedForThisDeploy(z.isFieldNebulaStoneGrantedForThisDeploy());
		nz.setSpec777RolledPower(z.getSpec777RolledPower());
		nz.setBotBikeMechanicPowerBonus(z.getBotBikeMechanicPowerBonus());
		nz.setZadkielOpponentTurnPowerBonus(z.getZadkielOpponentTurnPowerBonus());
		nz.setBattleMainLineSeq(z.getBattleMainLineSeq());
		nz.setKusuriOpponentDebuffFromDeployStones(z.getKusuriOpponentDebuffFromDeployStones());
		return nz;
	}

	private void assignBattleZoneMain(ZoneFighter z, BattleCard main, CpuBattleState st) {
		z.setMain(main);
		z.setBattleMainLineSeq(st.takeNextBattleMainLineSeq());
		if (main == null || main.getCardId() != KUSURI_ID) {
			z.setKusuriOpponentDebuffFromDeployStones(0);
		}
	}

	/** ザドキエル: 「奇跡」を置いた直後の予約を、今バトルゾーンに置いたファイターへ適用する */
	private void applyPendingZadkielBonusToNewlyDeployedZone(CpuBattleState st, ZoneFighter z, boolean deployerIsHumanSlot) {
		if (st == null || z == null || z.getMain() == null) {
			return;
		}
		if (deployerIsHumanSlot) {
			if (!st.isHumanPendingZadkielNextDeployOppTurnPower3()) {
				return;
			}
			z.setZadkielOpponentTurnPowerBonus(ZADKIEL_OPPONENT_TURN_POWER_BONUS);
			st.setHumanPendingZadkielNextDeployOppTurnPower3(false);
			st.addLog("ザドキエル: 配置したファイターは相手ターン中強さ+" + ZADKIEL_OPPONENT_TURN_POWER_BONUS);
		} else {
			if (!st.isCpuPendingZadkielNextDeployOppTurnPower3()) {
				return;
			}
			z.setZadkielOpponentTurnPowerBonus(ZADKIEL_OPPONENT_TURN_POWER_BONUS);
			st.setCpuPendingZadkielNextDeployOppTurnPower3(false);
			st.addLog((st.isPvp() ? "ゲスト" : "CPU") + "のザドキエル: 配置したファイターは相手ターン中強さ+"
					+ ZADKIEL_OPPONENT_TURN_POWER_BONUS);
		}
	}

	private CpuBattleState copyStateForCpuSim(CpuBattleState st) {
		CpuBattleState ns = new CpuBattleState();
		ns.setPvp(st.isPvp());
		ns.setCpuBattleMode(st.getCpuBattleMode() != null ? st.getCpuBattleMode() : CpuBattleMode.ORIGIN);
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
		ns.setHumanKoryuBonus(st.getHumanKoryuBonus());
		ns.setCpuKoryuBonus(st.getCpuKoryuBonus());
		ns.setHumanNextCrystakulDeployBonus(st.getHumanNextCrystakulDeployBonus());
		ns.setCpuNextCrystakulDeployBonus(st.getCpuNextCrystakulDeployBonus());
		ns.setHumanCrystakulCombatBonus(st.getHumanCrystakulCombatBonus());
		ns.setCpuCrystakulCombatBonus(st.getCpuCrystakulCombatBonus());
		ns.setSpec666NextHumanUndead(st.isSpec666NextHumanUndead());
		ns.setSpec666NextCpuUndead(st.isSpec666NextCpuUndead());
		ns.setHumanKrakenNextTurnSwordfishAdds(st.getHumanKrakenNextTurnSwordfishAdds());
		ns.setCpuKrakenNextTurnSwordfishAdds(st.getCpuKrakenNextTurnSwordfishAdds());
		ns.setHumanRamielNextTurnMiracleAdds(st.getHumanRamielNextTurnMiracleAdds());
		ns.setCpuRamielNextTurnMiracleAdds(st.getCpuRamielNextTurnMiracleAdds());
		ns.setHumanPendingZadkielNextDeployOppTurnPower3(st.isHumanPendingZadkielNextDeployOppTurnPower3());
		ns.setCpuPendingZadkielNextDeployOppTurnPower3(st.isCpuPendingZadkielNextDeployOppTurnPower3());
		ns.setHumanMiraclesBecomeFallenLucifer(st.isHumanMiraclesBecomeFallenLucifer());
		ns.setCpuMiraclesBecomeFallenLucifer(st.isCpuMiraclesBecomeFallenLucifer());
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
		ns.setScrapyardFieldTurnsRemaining(st.getScrapyardFieldTurnsRemaining());
		ns.setDeathbounceFieldTurnsRemaining(st.getDeathbounceFieldTurnsRemaining());
		ns.setAtlantisFieldCounterDisplay(st.getAtlantisFieldCounterDisplay());
		ns.setAtlantisAwaitingCount0(st.isAtlantisAwaitingCount0());
		ns.setWeeklyShonenCampFieldCounterDisplay(st.getWeeklyShonenCampFieldCounterDisplay());
		ns.setWeeklyShonenCampCount2ComicBonus(st.isWeeklyShonenCampCount2ComicBonus());
		ns.setWeeklyShonenCampGlobalDeployCostPlusOneThisTurn(st.isWeeklyShonenCampGlobalDeployCostPlusOneThisTurn());
		ns.setWorldRebuildFieldCounterDisplay(st.getWorldRebuildFieldCounterDisplay());
		ns.setPaperCityFieldCounterDisplay(st.getPaperCityFieldCounterDisplay());
		ns.setChojuGigaPendingHumanSlotNextDeployDragon(st.isChojuGigaPendingHumanSlotNextDeployDragon());
		ns.setChojuGigaPendingCpuSlotNextDeployHuman(st.isChojuGigaPendingCpuSlotNextDeployHuman());
		ns.setWorldRebuildOpenHumanHand(copyCards(st.getWorldRebuildOpenHumanHand()));
		ns.setWorldRebuildOpenHumanDeck(copyCards(st.getWorldRebuildOpenHumanDeck()));
		ns.setWorldRebuildOpenCpuHand(copyCards(st.getWorldRebuildOpenCpuHand()));
		ns.setWorldRebuildOpenCpuDeck(copyCards(st.getWorldRebuildOpenCpuDeck()));
		ns.setWorldRebuildOpenHumanStones(st.getWorldRebuildOpenHumanStones());
		ns.setWorldRebuildOpenCpuStones(st.getWorldRebuildOpenCpuStones());
		ns.setHumanSlotDeckId(st.getHumanSlotDeckId());
		ns.setCpuSlotDeckId(st.getCpuSlotDeckId());
		ns.setBattleMainLineSeqCounter(st.getBattleMainLineSeqCounter());
		return ns;
	}

	/** CPU がファイター配置を確定するときの候補（シミュレーションと実適用で共用） */
	private record CpuFighterPick(
			String mainInstanceId,
			int levelUpRest,
			int levelUpStones,
			List<String> levelUpDiscardIds,
			int deployBonus,
			int cpuEff,
			/** 忍者の印字コスト先頭。null のとき従来どおり手札末尾から支払う。 */
			String cpuNinjaCharacteristicFirstInstanceId) {
	}

	private int countFieldCardsAmongDiscards(List<String> discIds, List<BattleCard> hand,
			Map<Short, CardDefinition> defs) {
		if (discIds == null || discIds.isEmpty() || hand == null) {
			return 0;
		}
		int n = 0;
		for (BattleCard c : hand) {
			if (c == null || !discIds.contains(c.getInstanceId())) {
				continue;
			}
			CardDefinition d = defs.get(c.getCardId());
			if (isFieldCard(d)) {
				n++;
			}
		}
		return n;
	}

	/**
	 * アドバンスド CPU: ホスト（ユーザー）側が〈フィールド〉を配置しているとき、ストーンで出せる手札の〈フィールド〉があれば
	 * 必ず1枚選んで上書きする（強さ条件は後続のファイター探索で満たす前提）。CPU／ゲストが置いた〈フィールド〉は
	 * {@link CpuBattleState#getActiveFieldOwnerHuman()} が false のためここでは対象にしない。
	 */
	private Optional<String> pickAdvancedCpuFieldOverwriteHumanIfPossible(CpuBattleState st,
			Map<Short, CardDefinition> defs, Random rnd) {
		if (st == null || defs == null || rnd == null) {
			return Optional.empty();
		}
		if (st.getCpuBattleMode() != CpuBattleMode.ADVANCED) {
			return Optional.empty();
		}
		if (!Boolean.TRUE.equals(st.getActiveFieldOwnerHuman()) || st.getActiveField() == null) {
			return Optional.empty();
		}
		int minCost = Integer.MAX_VALUE;
		List<String> bestIds = new ArrayList<>();
		for (BattleCard fc : st.getCpuHand()) {
			if (fc == null) {
				continue;
			}
			CardDefinition fd = defs.get(fc.getCardId());
			if (!isFieldCard(fd)) {
				continue;
			}
			int fcost = effectiveDeployCost(fd, fc, defs, st.getCpuRest(), st.getCpuNextMechanicStacks(), st);
			if (fcost > st.getCpuStones()) {
				continue;
			}
			if (fcost < minCost) {
				minCost = fcost;
				bestIds.clear();
				bestIds.add(fc.getInstanceId());
			} else if (fcost == minCost) {
				bestIds.add(fc.getInstanceId());
			}
		}
		if (bestIds.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(bestIds.get(rnd.nextInt(bestIds.size())));
	}

	private Optional<CpuFighterPick> findBestWinningNoLevelUpCpuFighterPick(CpuBattleState st,
			Map<Short, CardDefinition> defs, Random rnd) {
		return findBestCpuFighterPick(st, defs, rnd, true);
	}

	/**
	 * @param noLevelUpOnly true のときレベルアップ 0 のみ探索
	 */
	private Optional<CpuFighterPick> findBestCpuFighterPick(CpuBattleState st, Map<Short, CardDefinition> defs,
			Random rnd, boolean noLevelUpOnly) {
		final boolean cpuIsFirstPlayer = !st.isHumanGoesFirst();
		final boolean cpuIsFirstTurnAsFirstPlayer = cpuIsFirstPlayer && st.getCpuTurnStarts() == 1;
		boolean hasOpp = st.getHumanBattle() != null;

		String bestInstanceId = null;
		int bestLevelUpRest = 0;
		int bestLevelUpStones = 0;
		int bestDeployBonus = 0;
		List<String> bestLevelUpDiscardIds = List.of();
		String bestCpuNinjaCharacteristicFirstInstanceId = null;
		int bestScore = Integer.MIN_VALUE;
		int bestCpuEff = hasOpp ? Integer.MAX_VALUE : -1;
		int bestResource = Integer.MAX_VALUE;
		int bestFieldDiscards = -1;

		int maxRest = maxLevelUpRestDiscard(st.getCpuHand().size());
		int maxStones = Math.max(0, st.getCpuStones());
		if (cpuIsFirstTurnAsFirstPlayer) {
			maxRest = 0;
			maxStones = 0;
		}

		int restEnd = maxRest;
		if (noLevelUpOnly) {
			restEnd = 0;
		}

		for (int levelUpRest = 0; levelUpRest <= restEnd; levelUpRest++) {
			List<List<String>> discardPlans = cpuDiscardPlans(st.getCpuHand(), levelUpRest);
			for (int levelUpStones = 0; levelUpStones <= maxStones; levelUpStones++) {
				for (BattleCard main : st.getCpuHand()) {
					CardDefinition mainDef = defs.get(main.getCardId());
					if (mainDef == null) {
						continue;
					}
					if (isFieldCard(mainDef)) {
						continue;
					}

					for (List<String> discIds : discardPlans) {
						if (discIds.contains(main.getInstanceId())) {
							continue;
						}

						int deployBonus = levelUpRest * 2 + levelUpStones * 2;
						deployBonus += st.getCpuNextDeployBonus();
						if (st.getCpuNextElfOnlyBonus() > 0
								&& CardAttributes.hasAttributeForDeployPreview(mainDef, main, st.isSpec666NextCpuUndead(),
										st.getCpuNextMechanicStacks(), "ELF")) {
							deployBonus += st.getCpuNextElfOnlyBonus();
						}
						if (st.getCpuNextDeployCostBonusTimes() > 0) {
							deployBonus += deployCharacteristicCostForPowerBonuses(mainDef, main, defs, st.getCpuRest(), st)
									* st.getCpuNextDeployCostBonusTimes();
						}
						deployBonus += 3 * st.getCpuNextMechanicStacks();
						deployBonus += st.getCpuNextCrystakulDeployBonus();

						CpuBattleState simSt = copyStateForCpuSim(st);
						simSt.setHumansTurn(false);
						simSt.setGameOver(false);

						if (levelUpStones > simSt.getCpuStones()) {
							continue;
						}
						simSt.setCpuStones(simSt.getCpuStones() - levelUpStones);

						for (String did : discIds) {
							BattleCard dc = removeByInstanceId(simSt.getCpuHand(), did);
							if (dc != null) {
								simSt.getCpuRest().add(dc);
							}
						}

						int cost = effectiveDeployCost(mainDef, main, defs, st.getCpuRest(), st.getCpuNextMechanicStacks(), st);
						int payCards = cpuDeployPayCardCount(cost, simSt.getCpuStones(), simSt.getCpuHand().size());
						if (payCards < 0) {
							continue;
						}
						int payStones = cost - payCards;
						simSt.setCpuStones(simSt.getCpuStones() - payStones);

						BattleCard simMain = removeByInstanceId(simSt.getCpuHand(), main.getInstanceId());
						if (simMain == null) {
							continue;
						}
						long simSalt = main.getCardId() ^ (levelUpRest * 31L) ^ (levelUpStones * 131L);
						int levelUpDeployPwrSimAdv = levelUpRest * 2 + levelUpStones * 2;
						NinjaFirstCostPick nPick = pickCpuNinjaCharacteristicFirstCost(
								simSt, simMain, mainDef, payCards, deployBonus, levelUpDeployPwrSimAdv, st, defs, simSalt);
						if (nPick.skipped()) {
							continue;
						}
						List<BattleCard> paid = cpuTakeCharacteristicCostPaymentFromHand(simSt.getCpuHand(), payCards, nPick);
						if (paid == null || paid.size() != payCards) {
							continue;
						}
						ZoneFighter z = new ZoneFighter();
						assignBattleZoneMain(z, simMain, simSt);
						z.setCostUnder(paid);
						z.setCostPayCardCount(payCards);
						applyCrystakulBonusesToDeployedZone(simSt, z, deployBonus, levelUpDeployPwrSimAdv, false);
						applyPendingZadkielBonusToNewlyDeployedZone(simSt, z, false);
						retireOwnBattleZoneBeforeNewDeploy(simSt, false, false, defs);
						simSt.setCpuBattle(z);

						Random simRnd = new Random(31_337L ^ simSalt);
						cpuSimApplyDeployAbilitiesAfterZonePlaced(simSt, mainDef, defs, simRnd);

						int cpuEff = effectiveBattlePower(simSt.getCpuBattle(), false, simSt, defs);
						int oppEff = effectiveBattlePower(simSt.getHumanBattle(), true, simSt, defs);
						if (simSt.getHumanBattle() != null && cpuEff < oppEff) {
							continue;
						}

						int fieldDisc = countFieldCardsAmongDiscards(discIds, st.getCpuHand(), defs);
						int resource = levelUpRest + levelUpStones;

						if (hasOpp) {
							boolean better;
							if (st.getCpuBattleMode() == CpuBattleMode.ADVANCED && !noLevelUpOnly) {
								better = isBetterAdvancedCpuFighterPick(resource, levelUpStones, levelUpRest, fieldDisc, cpuEff,
										bestResource, bestLevelUpStones, bestLevelUpRest, bestFieldDiscards, bestCpuEff);
							} else {
								better = resource < bestResource
										|| (resource == bestResource && (
												(levelUpStones > bestLevelUpStones)
												|| (levelUpStones == bestLevelUpStones && levelUpRest < bestLevelUpRest)
												|| (levelUpStones == bestLevelUpStones && levelUpRest == bestLevelUpRest && cpuEff < bestCpuEff)
										));
							}
							if (better) {
								bestCpuEff = cpuEff;
								bestResource = resource;
								bestFieldDiscards = fieldDisc;
								bestInstanceId = main.getInstanceId();
								bestLevelUpRest = levelUpRest;
								bestLevelUpStones = levelUpStones;
								bestDeployBonus = deployBonus;
								bestLevelUpDiscardIds = discIds;
								bestCpuNinjaCharacteristicFirstInstanceId = nPick.firstPaidInstanceIdForCharacteristic();
							}
						} else {
							int score = cpuEff - oppEff;
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
								bestFieldDiscards = fieldDisc;
								bestInstanceId = main.getInstanceId();
								bestLevelUpRest = levelUpRest;
								bestLevelUpStones = levelUpStones;
								bestDeployBonus = deployBonus;
								bestLevelUpDiscardIds = discIds;
								bestCpuNinjaCharacteristicFirstInstanceId = nPick.firstPaidInstanceIdForCharacteristic();
							}
						}
					}
				}
			}
		}

		if (bestInstanceId == null) {
			return Optional.empty();
		}
		return Optional.of(new CpuFighterPick(
				bestInstanceId, bestLevelUpRest, bestLevelUpStones, bestLevelUpDiscardIds, bestDeployBonus, bestCpuEff,
				bestCpuNinjaCharacteristicFirstInstanceId));
	}

	/**
	 * アドバンスド CPU: レベルアップの比較は ①総コスト ②ストーン優先 ③捨て札枚数 ④〈フィールド〉カードを捨てる枚数（多いほど良い＝ストーン・フィールド・ファイター順の支払い）⑤最小の場の強さ
	 */
	private static boolean isBetterAdvancedCpuFighterPick(int resource, int levelUpStones, int levelUpRest, int fieldDiscards,
			int cpuEff, int bestResource, int bestLevelUpStones, int bestLevelUpRest, int bestFieldDiscards, int bestCpuEff) {
		if (resource < bestResource) {
			return true;
		}
		if (resource > bestResource) {
			return false;
		}
		if (levelUpStones > bestLevelUpStones) {
			return true;
		}
		if (levelUpStones < bestLevelUpStones) {
			return false;
		}
		if (levelUpRest < bestLevelUpRest) {
			return true;
		}
		if (levelUpRest > bestLevelUpRest) {
			return false;
		}
		if (fieldDiscards > bestFieldDiscards) {
			return true;
		}
		if (fieldDiscards < bestFieldDiscards) {
			return false;
		}
		return cpuEff < bestCpuEff;
	}

	private boolean tryAdvancedCpuFieldOnly(CpuBattleState st, Map<Short, CardDefinition> defs, Random rnd) {
		Optional<String> fid = pickAdvancedCpuFieldOverwriteHumanIfPossible(st, defs, rnd);
		if (fid.isEmpty()) {
			return false;
		}
		BattleCard main = removeByInstanceId(st.getCpuHand(), fid.get());
		if (main == null) {
			return false;
		}
		CardDefinition mainDef = defs.get(main.getCardId());
		if (mainDef == null || !isFieldCard(mainDef)) {
			return false;
		}
		int cost = effectiveDeployCost(mainDef, main, defs, st.getCpuRest(), st.getCpuNextMechanicStacks(), st);
		if (cost > st.getCpuStones()) {
			return false;
		}
		st.setCpuStones(st.getCpuStones() - cost);
		replaceActiveField(st, main, false, defs);
		st.addLog("相手は「" + mainDef.getName() + "」を〈場〉に置いた");
		if (mainDef.getId() != null && mainDef.getId() == FLEET_HO_IVI_FIELD_ID) {
			applyFleetHoIviFieldDeployBothSides(st, defs, rnd);
		}
		st.setLastMessage("《フィールド》を配置しました（ターンは続きます）");
		return true;
	}

	private boolean commitCpuFighterPick(CpuBattleState st, CpuFighterPick pick, Map<Short, CardDefinition> defs) {
		if (pick == null || pick.mainInstanceId == null) {
			return false;
		}
		if (pick.levelUpStones <= st.getCpuStones()) {
			st.setCpuStones(st.getCpuStones() - pick.levelUpStones);
		}
		List<BattleCard> levelUpCards = new ArrayList<>();
		for (String did : pick.levelUpDiscardIds) {
			BattleCard dc = removeByInstanceId(st.getCpuHand(), did);
			if (dc != null) {
				levelUpCards.add(dc);
			}
		}
		if (!levelUpCards.isEmpty() || pick.levelUpStones > 0) {
			StringBuilder b = new StringBuilder("CPUレベルアップ: ");
			if (!levelUpCards.isEmpty()) {
				b.append("カード").append(levelUpCards.size()).append("枚");
			}
			if (!levelUpCards.isEmpty() && pick.levelUpStones > 0) {
				b.append(" + ");
			}
			if (pick.levelUpStones > 0) {
				b.append("ストーン").append(pick.levelUpStones).append("つ");
			}
			st.addLog(b.toString());
		}

		BattleCard deployCard = findByInstanceId(st.getCpuHand(), pick.mainInstanceId);
		CardDefinition bestDef = deployCard != null ? defs.get(deployCard.getCardId()) : null;
		if (bestDef == null) {
			st.getCpuRest().addAll(levelUpCards);
			return false;
		}
		int mechanicStacksForPendingCpuDeploy = st.getCpuNextMechanicStacks();
		int cost = effectiveDeployCost(bestDef, deployCard, defs, st.getCpuRest(), mechanicStacksForPendingCpuDeploy, st);
		int payCards = cpuDeployPayCardCount(cost, st.getCpuStones(), st.getCpuHand().size());
		if (payCards < 0) {
			st.getCpuRest().addAll(levelUpCards);
			return false;
		}
		int payCostStones = cost - payCards;
		BattleCard main = removeByInstanceId(st.getCpuHand(), pick.mainInstanceId);
		if (main == null) {
			st.getCpuRest().addAll(levelUpCards);
			return false;
		}
		st.setCpuStones(st.getCpuStones() - payCostStones);
		NinjaFirstCostPick costPick = pick.cpuNinjaCharacteristicFirstInstanceId() != null
				? NinjaFirstCostPick.choice(pick.cpuNinjaCharacteristicFirstInstanceId())
				: NinjaFirstCostPick.legacy();
		List<BattleCard> paid = cpuTakeCharacteristicCostPaymentFromHand(st.getCpuHand(), payCards, costPick);
		if (paid == null || paid.size() != payCards) {
			st.getCpuHand().add(0, main);
			st.setCpuStones(st.getCpuStones() + payCostStones + pick.levelUpStones);
			st.getCpuRest().addAll(levelUpCards);
			return false;
		}
		paid.addAll(levelUpCards);
		ZoneFighter z = new ZoneFighter();
		assignBattleZoneMain(z, main, st);
		z.setCostUnder(paid);
		z.setCostPayCardCount(payCards);
		int levelUpDeployPick = pick.levelUpRest * 2 + pick.levelUpStones * 2;
		applyCrystakulBonusesToDeployedZone(st, z, pick.deployBonus, levelUpDeployPick, false);
		applyPendingZadkielBonusToNewlyDeployedZone(st, z, false);
		st.setCpuNextDeployBonus(0);
		st.setCpuNextElfOnlyBonus(0);
		st.setCpuNextDeployCostBonusTimes(0);
		st.setCpuNextMechanicStacks(0);
		retireOwnBattleZoneBeforeNewDeploy(st, false, true, defs);
		st.setCpuBattle(z);
		if (payCostStones > 0 && payCards > 0) {
			st.addLog("CPUは「" + bestDef.getName() + "」を配置（コスト: ストーン" + payCostStones + "＋カード" + payCards + "）");
		} else if (payCostStones > 0) {
			st.addLog("CPUは「" + bestDef.getName() + "」を配置（コスト: ストーン" + payCostStones + "）");
		} else {
			st.addLog("CPUは「" + bestDef.getName() + "」を配置した");
		}
		stagePendingDeployEffect(st, false, bestDef, z, mechanicStacksForPendingCpuDeploy);
		return true;
	}

	private void cpuTurnGameOverCpuCannotDeploy(CpuBattleState st) {
		st.setGameOver(true);
		st.setHumanWon(true);
		st.setLastMessage("勝利（CPUが相手以上のファイターを出せません）");
		st.addLog("勝利: CPUが相手以上のファイターを出せない");
	}

	private void cpuTurnGameOverCpuSkipped(CpuBattleState st) {
		st.addLog("CPUは配置しなかった");
		st.setGameOver(true);
		st.setHumanWon(true);
		st.setPhase(BattlePhase.GAME_OVER);
		st.setLastMessage("勝利（CPUが配置しませんでした）");
	}

	/** アドバンスド: フィールド→（同一ターン続行）ファイター、または従来探索 */
	private void cpuTurnAdvanced(CpuBattleState st, Map<Short, CardDefinition> defs, Random rnd) {
		if (tryAdvancedCpuFieldOnly(st, defs, rnd)) {
			return;
		}
		Optional<CpuFighterPick> noLu = findBestWinningNoLevelUpCpuFighterPick(st, defs, rnd);
		if (noLu.isPresent()) {
			if (commitCpuFighterPick(st, noLu.get(), defs)) {
				return;
			}
		}
		Optional<CpuFighterPick> any = findBestCpuFighterPick(st, defs, rnd, false);
		if (any.isPresent() && commitCpuFighterPick(st, any.get(), defs)) {
			return;
		}
		cpuTurnGameOverCpuSkipped(st);
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
			cpuTurnGameOverCpuCannotDeploy(st);
			return;
		}

		if (st.getCpuBattleMode() == CpuBattleMode.ADVANCED) {
			cpuTurnAdvanced(st, defs, rnd);
			return;
		}

		// CPU は手札を吟味し、配置効果・常時効果・複数回レベルアップを考慮して
		// 相手バトルゾーン以上になれるなら必ず配置する。
		String bestInstanceId = null;
		int bestLevelUpRest = 0;
		int bestLevelUpStones = 0;
		int bestDeployBonus = 0;
		List<String> bestLevelUpDiscardIds = List.of();
		String bestCpuNinjaCharacteristicFirstInstanceId = null;
		boolean hasOpp = st.getHumanBattle() != null;
		int bestScore = Integer.MIN_VALUE; // 相手がいないとき用
		int bestCpuEff = hasOpp ? Integer.MAX_VALUE : -1;
		int bestResource = Integer.MAX_VALUE;

		int maxRest = maxLevelUpRestDiscard(st.getCpuHand().size());
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
						if (st.getCpuNextElfOnlyBonus() > 0
								&& CardAttributes.hasAttributeForDeployPreview(mainDef, main, st.isSpec666NextCpuUndead(),
										st.getCpuNextMechanicStacks(), "ELF")) {
							deployBonus += st.getCpuNextElfOnlyBonus();
						}
						if (st.getCpuNextDeployCostBonusTimes() > 0) {
							deployBonus += deployCharacteristicCostForPowerBonuses(mainDef, main, defs, st.getCpuRest(), st)
									* st.getCpuNextDeployCostBonusTimes();
						}
						deployBonus += 3 * st.getCpuNextMechanicStacks();
						deployBonus += st.getCpuNextCrystakulDeployBonus();

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

						int cost = effectiveDeployCost(mainDef, main, defs, st.getCpuRest(), st.getCpuNextMechanicStacks(), st);
						int payCards = cpuDeployPayCardCount(cost, simSt.getCpuStones(), simSt.getCpuHand().size());
						if (payCards < 0) {
							continue;
						}
						int payStones = cost - payCards;
						simSt.setCpuStones(simSt.getCpuStones() - payStones);

						// 配置カードを取り出す
						BattleCard simMain = removeByInstanceId(simSt.getCpuHand(), main.getInstanceId());
						if (simMain == null) continue;
						long simSalt = main.getCardId() ^ (levelUpRest * 31L) ^ (levelUpStones * 131L);
						int levelUpDeployPwrSimOrig = levelUpRest * 2 + levelUpStones * 2;
						NinjaFirstCostPick nPick = pickCpuNinjaCharacteristicFirstCost(
								simSt, simMain, mainDef, payCards, deployBonus, levelUpDeployPwrSimOrig, st, defs, simSalt);
						if (nPick.skipped()) {
							continue;
						}
						List<BattleCard> paid = cpuTakeCharacteristicCostPaymentFromHand(simSt.getCpuHand(), payCards, nPick);
						if (paid == null || paid.size() != payCards) {
							continue;
						}
						ZoneFighter z = new ZoneFighter();
						assignBattleZoneMain(z, simMain, simSt);
						z.setCostUnder(paid);
						z.setCostPayCardCount(payCards);
						applyCrystakulBonusesToDeployedZone(simSt, z, deployBonus, levelUpDeployPwrSimOrig, false);
						applyPendingZadkielBonusToNewlyDeployedZone(simSt, z, false);
						retireOwnBattleZoneBeforeNewDeploy(simSt, false, false, defs);
						simSt.setCpuBattle(z);

						Random simRnd = new Random(31_337L ^ simSalt);
						cpuSimApplyDeployAbilitiesAfterZonePlaced(simSt, mainDef, defs, simRnd);

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
								bestCpuNinjaCharacteristicFirstInstanceId = nPick.firstPaidInstanceIdForCharacteristic();
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
								bestCpuNinjaCharacteristicFirstInstanceId = nPick.firstPaidInstanceIdForCharacteristic();
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
				if (bestLevelUpStones > 0) b.append("ストーン").append(bestLevelUpStones).append("つ");
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
				int mechanicStacksForPendingCpuTurnDeploy = st.getCpuNextMechanicStacks();
				int cost = effectiveDeployCost(bestDef, deployCard, defs, st.getCpuRest(), mechanicStacksForPendingCpuTurnDeploy, st);
				int payCards = cpuDeployPayCardCount(cost, st.getCpuStones(), st.getCpuHand().size());
				if (payCards >= 0) {
					int payCostStones = cost - payCards;
					BattleCard main = removeByInstanceId(st.getCpuHand(), bestInstanceId);
					if (main != null) {
						st.setCpuStones(st.getCpuStones() - payCostStones);
						NinjaFirstCostPick costPick = bestCpuNinjaCharacteristicFirstInstanceId != null
								? NinjaFirstCostPick.choice(bestCpuNinjaCharacteristicFirstInstanceId)
								: NinjaFirstCostPick.legacy();
						List<BattleCard> paid = cpuTakeCharacteristicCostPaymentFromHand(st.getCpuHand(), payCards, costPick);
						if (paid == null || paid.size() != payCards) {
							st.getCpuHand().add(0, main);
							st.setCpuStones(st.getCpuStones() + payCostStones + bestLevelUpStones);
						} else {
							paid.addAll(levelUpCards);
							ZoneFighter z = new ZoneFighter();
							assignBattleZoneMain(z, main, st);
							z.setCostUnder(paid);
							z.setCostPayCardCount(payCards);
							int levelUpDeployBest = bestLevelUpRest * 2 + bestLevelUpStones * 2;
							applyCrystakulBonusesToDeployedZone(st, z, bestDeployBonus, levelUpDeployBest, false);
							applyPendingZadkielBonusToNewlyDeployedZone(st, z, false);
							st.setCpuNextDeployBonus(0);
							st.setCpuNextElfOnlyBonus(0);
							st.setCpuNextDeployCostBonusTimes(0);
							st.setCpuNextMechanicStacks(0);
							retireOwnBattleZoneBeforeNewDeploy(st, false, true, defs);
							st.setCpuBattle(z);
							// 〈探鉱の洞窟〉は resolve 内で〈配置〉より先に適用する
							if (payCostStones > 0 && payCards > 0) {
								st.addLog("CPUは「" + bestDef.getName() + "」を配置（コスト: ストーン" + payCostStones + "＋カード" + payCards + "）");
							} else if (payCostStones > 0) {
								st.addLog("CPUは「" + bestDef.getName() + "」を配置（コスト: ストーン" + payCostStones + "）");
							} else {
								st.addLog("CPUは「" + bestDef.getName() + "」を配置した");
							}
							deployed = true;
							stagePendingDeployEffect(st, false, bestDef, z, mechanicStacksForPendingCpuTurnDeploy);
						}
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
				boolean returned = moveZoneToRestOrReturnToHand(st, st.getCpuBattle(), st.getCpuRest(), st.getCpuHand(), defs);
				st.setCpuBattle(null);
				st.addLog(returned
						? cpuSlotActorLogLabel(st) + "のファイターは手札へ戻った"
						: cpuSlotActorLogLabel(st) + "のファイターをレストへ");
			}
			int handBefore = st.getHumanHand().size();
			while (st.getHumanHand().size() < 4 && !st.getHumanDeck().isEmpty()) {
				drawOne(st.getHumanDeck(), st.getHumanHand());
			}
			int drawn = st.getHumanHand().size() - handBefore;
			if (drawn > 0) {
				st.addLog(humanSlotActorLogLabel(st) + "はデッキからカードを" + drawn + "枚引いた");
			}
		} else {
			if (st.getCpuBattle() != null && st.getHumanBattle() != null) {
				boolean returned = moveZoneToRestOrReturnToHand(st, st.getHumanBattle(), st.getHumanRest(), st.getHumanHand(), defs);
				st.setHumanBattle(null);
				st.addLog(returned
						? humanSlotActorLogLabel(st) + "のファイターは手札へ戻った"
						: humanSlotActorLogLabel(st) + "のファイターがレストへ");
			}
			int handBefore = st.getCpuHand().size();
			while (st.getCpuHand().size() < 4 && !st.getCpuDeck().isEmpty()) {
				drawOne(st.getCpuDeck(), st.getCpuHand());
			}
			int drawn = st.getCpuHand().size() - handBefore;
			if (drawn > 0) {
				st.addLog(cpuSlotActorLogLabel(st) + "はデッキからカードを" + drawn + "枚引いた");
			}
		}
		maybeExpireScrapyardFieldAfterKnock(st, humanWasActing, defs);
		maybeExpireDeathbounceFieldAfterKnock(st, humanWasActing, defs);
		maybeExpireWeeklyShonenCampFieldAfterKnock(st, humanWasActing, defs);
	}

	/** 〈フィールド〉廃棄工場: 効果残存中のみ。名前に「ガラクタ」を含むメインはノック時に手札へ（コスト下はレストのまま） */
	private static boolean scrapyardFieldSendsGarakutaMainToHand(CpuBattleState st, ZoneFighter z,
			Map<Short, CardDefinition> defs) {
		if (st == null || z == null || z.getMain() == null || defs == null) {
			return false;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != SCRAPYARD_FIELD_ID) {
			return false;
		}
		if (st.getScrapyardFieldTurnsRemaining() <= 0) {
			return false;
		}
		CardDefinition md = defs.get(z.getMain().getCardId());
		return md != null && md.getName() != null && md.getName().contains("ガラクタ");
	}

	/**
	 * カウントが 0 なのに場に残っている〈フィールド〉を所有者レストへ（各 tick／解決の取りこぼし防止）。
	 */
	private void expireActiveCountedFieldAtCountZero(CpuBattleState st, Map<Short, CardDefinition> defs) {
		if (st == null || st.isGameOver() || defs == null || st.getActiveField() == null) {
			return;
		}
		BattleCard f = st.getActiveField();
		short id = f.getCardId();
		if (id == SCRAPYARD_FIELD_ID && st.getScrapyardFieldTurnsRemaining() <= 0) {
			removeActiveScrapyardFieldToOwnerRestNow(st, defs);
		} else if (id == DEATHBOUNCE_FIELD_ID && st.getDeathbounceFieldTurnsRemaining() <= 0) {
			removeActiveDeathbounceFieldToOwnerRestNow(st, defs);
		} else if (id == GameConstants.WEEKLY_SHONEN_CAMP_FIELD_CARD_ID
				&& st.getWeeklyShonenCampFieldCounterDisplay() <= 0) {
			removeActiveWeeklyShonenCampToOwnerRestNow(st, defs);
		} else if (id == GameConstants.PAPER_CITY_FIELD_CARD_ID && st.getPaperCityFieldCounterDisplay() <= 0) {
			removeActivePaperCityFieldToOwnerRestNow(st, defs);
		} else if (id == GameConstants.WORLD_REBUILD_FIELD_CARD_ID && st.getWorldRebuildFieldCounterDisplay() <= 0) {
			maybeExecuteWorldRebuildFieldCount0(st, defs);
		} else if (id == GameConstants.ATLANTIS_FIELD_CARD_ID && st.isAtlantisAwaitingCount0()
				&& st.getAtlantisFieldCounterDisplay() <= 0) {
			Boolean owner = st.getActiveFieldOwnerHuman();
			if (owner != null) {
				executeAtlantisFieldCount0Resolution(st, owner.booleanValue(), defs);
			}
		}
	}

	/** 廃棄工場: ターン開始時に 4→3→2→1（1 の間は減らさない） */
	private static void tickScrapyardFieldAtTurnStart(CpuBattleState st, Map<Short, CardDefinition> defs) {
		if (st == null) {
			return;
		}
		BattleCard f = st.getActiveField();
		if (f == null || f.getCardId() != SCRAPYARD_FIELD_ID) {
			return;
		}
		int n = st.getScrapyardFieldTurnsRemaining();
		if (n <= 0) {
			return;
		}
		if (n > 1) {
			st.setScrapyardFieldTurnsRemaining(n - 1);
		}
	}

	/**
	 * 廃棄工場: 残り「1」の相手ターンの終了時（ノック・ドロー処理の直後）に場から使用者のレストへ。
	 *
	 * @param humanWasActing いま終わった手番がホスト（human スロット）か
	 */
	private void maybeExpireScrapyardFieldAfterKnock(CpuBattleState st, boolean humanWasActing,
			Map<Short, CardDefinition> defs) {
		if (st == null) {
			return;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != SCRAPYARD_FIELD_ID) {
			return;
		}
		if (st.getScrapyardFieldTurnsRemaining() != 1) {
			return;
		}
		Boolean ownerHuman = st.getActiveFieldOwnerHuman();
		if (ownerHuman == null) {
			return;
		}
		if (humanWasActing == ownerHuman.booleanValue()) {
			return;
		}
		CardDefinition fd = defs != null ? defs.get(field.getCardId()) : null;
		String nm = fd != null && fd.getName() != null ? fd.getName() : "廃棄工場 5C-R4P";
		if (ownerHuman) {
			st.getHumanRest().add(field);
			st.addLog("〈フィールド〉「" + nm + "」の効果が切れ、あなたのレストに置かれた");
		} else {
			st.getCpuRest().add(field);
			st.addLog(st.isPvp()
					? "〈フィールド〉「" + nm + "」の効果が切れ、ゲストのレストに置かれた"
					: "〈フィールド〉「" + nm + "」の効果が切れ、相手のレストに置かれた");
		}
		st.setActiveField(null);
		st.setActiveFieldOwnerHuman(null);
		st.setScrapyardFieldTurnsRemaining(0);
	}

	/** 霊園教会 デスバウンス: ターン開始時に 6→5→…→1（1 の間は減らさない） */
	private static void tickDeathbounceFieldAtTurnStart(CpuBattleState st, Map<Short, CardDefinition> defs) {
		if (st == null) {
			return;
		}
		BattleCard f = st.getActiveField();
		if (f == null || f.getCardId() != DEATHBOUNCE_FIELD_ID) {
			return;
		}
		int n = st.getDeathbounceFieldTurnsRemaining();
		if (n <= 0) {
			return;
		}
		if (n > 1) {
			st.setDeathbounceFieldTurnsRemaining(n - 1);
		}
	}

	private static void clearWeeklyShonenCampFieldTracking(CpuBattleState st) {
		if (st == null) {
			return;
		}
		st.setWeeklyShonenCampFieldCounterDisplay(0);
		st.setWeeklyShonenCampCount2ComicBonus(false);
		st.setWeeklyShonenCampGlobalDeployCostPlusOneThisTurn(false);
	}

	/** 週刊少年 CAMP: ターン開始時に 6→5→…→1（1 の間は減らさない） */
	private static void tickWeeklyShonenCampFieldAtTurnStart(CpuBattleState st, Map<Short, CardDefinition> defs) {
		if (st == null) {
			return;
		}
		BattleCard f = st.getActiveField();
		if (f == null || f.getCardId() != GameConstants.WEEKLY_SHONEN_CAMP_FIELD_CARD_ID) {
			return;
		}
		int n = st.getWeeklyShonenCampFieldCounterDisplay();
		if (n <= 0) {
			return;
		}
		if (n > 1) {
			int next = n - 1;
			st.setWeeklyShonenCampFieldCounterDisplay(next);
			applyWeeklyShonenCampCounterMilestones(st, next);
		}
	}

	private static void applyWeeklyShonenCampCounterMilestones(CpuBattleState st, int counterAfterTick) {
		if (st == null) {
			return;
		}
		if (counterAfterTick == 3) {
			st.setWeeklyShonenCampGlobalDeployCostPlusOneThisTurn(true);
			st.addLog("週刊少年 CAMP: カウント3 — ターンの終わりまですべてのカードのコスト+1");
		}
		if (counterAfterTick == 2) {
			st.setWeeklyShonenCampCount2ComicBonus(true);
			st.addLog("週刊少年 CAMP: カウント2 — 種族：コミックの強さ+4");
		}
	}

	/**
	 * 週刊少年 CAMP: 残り「1」の相手ターンの終了時（ノック・ドロー処理の直後）に場から使用者のレストへ。
	 */
	private void maybeExpireWeeklyShonenCampFieldAfterKnock(CpuBattleState st, boolean humanWasActing,
			Map<Short, CardDefinition> defs) {
		if (st == null) {
			return;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != GameConstants.WEEKLY_SHONEN_CAMP_FIELD_CARD_ID) {
			return;
		}
		if (st.getWeeklyShonenCampFieldCounterDisplay() != 1) {
			return;
		}
		Boolean ownerHuman = st.getActiveFieldOwnerHuman();
		if (ownerHuman == null) {
			return;
		}
		if (humanWasActing == ownerHuman.booleanValue()) {
			return;
		}
		removeActiveWeeklyShonenCampToOwnerRestNow(st, defs);
	}

	private void removeActiveWeeklyShonenCampToOwnerRestNow(CpuBattleState st, Map<Short, CardDefinition> defs) {
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != GameConstants.WEEKLY_SHONEN_CAMP_FIELD_CARD_ID) {
			return;
		}
		Boolean ownerHuman = st.getActiveFieldOwnerHuman();
		if (ownerHuman == null) {
			return;
		}
		clearWeeklyShonenCampFieldTracking(st);
		CardDefinition fd = defs != null ? defs.get(field.getCardId()) : null;
		String nm = fd != null && fd.getName() != null ? fd.getName() : "週刊少年 CAMP";
		if (ownerHuman) {
			st.getHumanRest().add(field);
			st.addLog("〈フィールド〉「" + nm + "」の効果が切れ、あなたのレストに置かれた");
		} else {
			st.getCpuRest().add(field);
			st.addLog(st.isPvp()
					? "〈フィールド〉「" + nm + "」の効果が切れ、ゲストのレストに置かれた"
					: "〈フィールド〉「" + nm + "」の効果が切れ、相手のレストに置かれた");
		}
		st.setActiveField(null);
		st.setActiveFieldOwnerHuman(null);
	}

	private void applyPaperCityInkKnightToFieldOwner(CpuBattleState st, boolean fieldOwnerIsHuman, Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		short ink = GameConstants.INK_KNIGHT_FIGHTER_CARD_ID;
		if (defs.get(ink) == null) {
			return;
		}
		List<BattleCard> hand = fieldOwnerIsHuman ? st.getHumanHand() : st.getCpuHand();
		if (hand == null) {
			return;
		}
		addCopiesOfCardIdToHand(hand, ink, 1, defs);
	}

	/**
	 * ペーパーシティ: ターン開始ごとにカウントを1減らし、4・2で効果、0で場から所有者レストへ。
	 */
	private void tickPaperCityFieldAtTurnStart(CpuBattleState st, Map<Short, CardDefinition> defs) {
		advancePaperCityFieldCountSteps(st, 1, defs);
	}

	private void advancePaperCityFieldCountSteps(CpuBattleState st, int steps, Map<Short, CardDefinition> defs) {
		if (st == null || defs == null || steps <= 0) {
			return;
		}
		BattleCard f = st.getActiveField();
		if (f == null || f.getCardId() != GameConstants.PAPER_CITY_FIELD_CARD_ID) {
			return;
		}
		Boolean owner = st.getActiveFieldOwnerHuman();
		if (owner == null) {
			return;
		}
		int n = st.getPaperCityFieldCounterDisplay();
		if (n <= 0) {
			removeActivePaperCityFieldToOwnerRestNow(st, defs);
			return;
		}
		for (int i = 0; i < steps && n > 0; i++) {
			n--;
			if (n == 4) {
				applyPaperCityInkKnightToFieldOwner(st, owner.booleanValue(), defs);
				st.addLog("ペーパーシティ: カウント4 — 「インクナイト」を1枚手札に加えた");
			} else if (n == 2) {
				if (owner.booleanValue()) {
					st.setHumanStones(st.getHumanStones() + 2);
				} else {
					st.setCpuStones(st.getCpuStones() + 2);
				}
				st.addLog("ペーパーシティ: カウント2 — ストーン+2");
			}
		}
		st.setPaperCityFieldCounterDisplay(Math.max(0, n));
		if (n <= 0) {
			removeActivePaperCityFieldToOwnerRestNow(st, defs);
		}
	}

	private void removeActivePaperCityFieldToOwnerRestNow(CpuBattleState st, Map<Short, CardDefinition> defs) {
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != GameConstants.PAPER_CITY_FIELD_CARD_ID) {
			return;
		}
		Boolean ownerHuman = st.getActiveFieldOwnerHuman();
		if (ownerHuman == null) {
			return;
		}
		st.setPaperCityFieldCounterDisplay(0);
		CardDefinition fd = defs != null ? defs.get(field.getCardId()) : null;
		String nm = fd != null && fd.getName() != null ? fd.getName() : "ペーパーシティ";
		if (ownerHuman) {
			st.getHumanRest().add(field);
			st.addLog("〈フィールド〉「" + nm + "」の効果が切れ、あなたのレストに置かれた");
		} else {
			st.getCpuRest().add(field);
			st.addLog(st.isPvp()
					? "〈フィールド〉「" + nm + "」の効果が切れ、ゲストのレストに置かれた"
					: "〈フィールド〉「" + nm + "」の効果が切れ、相手のレストに置かれた");
		}
		st.setActiveField(null);
		st.setActiveFieldOwnerHuman(null);
	}

	/**
	 * 霊園教会 デスバウンス: 残り「1」の相手ターンの終了時（ノック・ドロー処理の直後）に場から使用者のレストへ。
	 */
	private void maybeExpireDeathbounceFieldAfterKnock(CpuBattleState st, boolean humanWasActing,
			Map<Short, CardDefinition> defs) {
		if (st == null) {
			return;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != DEATHBOUNCE_FIELD_ID) {
			return;
		}
		if (st.getDeathbounceFieldTurnsRemaining() != 1) {
			return;
		}
		Boolean ownerHuman = st.getActiveFieldOwnerHuman();
		if (ownerHuman == null) {
			return;
		}
		if (humanWasActing == ownerHuman.booleanValue()) {
			return;
		}
		CardDefinition fd = defs != null ? defs.get(field.getCardId()) : null;
		String nm = fd != null && fd.getName() != null ? fd.getName() : "霊園教会 デスバウンス";
		if (ownerHuman) {
			st.getHumanRest().add(field);
			st.addLog("〈フィールド〉「" + nm + "」の効果が切れ、あなたのレストに置かれた");
		} else {
			st.getCpuRest().add(field);
			st.addLog(st.isPvp()
					? "〈フィールド〉「" + nm + "」の効果が切れ、ゲストのレストに置かれた"
					: "〈フィールド〉「" + nm + "」の効果が切れ、相手のレストに置かれた");
		}
		st.setActiveField(null);
		st.setActiveFieldOwnerHuman(null);
		st.setDeathbounceFieldTurnsRemaining(0);
	}

	private void removeActiveScrapyardFieldToOwnerRestNow(CpuBattleState st, Map<Short, CardDefinition> defs) {
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != SCRAPYARD_FIELD_ID) {
			return;
		}
		Boolean ownerHuman = st.getActiveFieldOwnerHuman();
		if (ownerHuman == null) {
			return;
		}
		CardDefinition fd = defs != null ? defs.get(field.getCardId()) : null;
		String nm = fd != null && fd.getName() != null ? fd.getName() : "廃棄工場 5C-R4P";
		if (ownerHuman) {
			st.getHumanRest().add(field);
			st.addLog("〈フィールド〉「" + nm + "」の効果が切れ、あなたのレストに置かれた");
		} else {
			st.getCpuRest().add(field);
			st.addLog(st.isPvp()
					? "〈フィールド〉「" + nm + "」の効果が切れ、ゲストのレストに置かれた"
					: "〈フィールド〉「" + nm + "」の効果が切れ、相手のレストに置かれた");
		}
		st.setActiveField(null);
		st.setActiveFieldOwnerHuman(null);
		st.setScrapyardFieldTurnsRemaining(0);
	}

	private void removeActiveDeathbounceFieldToOwnerRestNow(CpuBattleState st, Map<Short, CardDefinition> defs) {
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != DEATHBOUNCE_FIELD_ID) {
			return;
		}
		Boolean ownerHuman = st.getActiveFieldOwnerHuman();
		if (ownerHuman == null) {
			return;
		}
		stripDeathbouncePersistedHandPenalties(st);
		CardDefinition fd = defs != null ? defs.get(field.getCardId()) : null;
		String nm = fd != null && fd.getName() != null ? fd.getName() : "霊園教会 デスバウンス";
		if (ownerHuman) {
			st.getHumanRest().add(field);
			st.addLog("〈フィールド〉「" + nm + "」の効果が切れ、あなたのレストに置かれた");
		} else {
			st.getCpuRest().add(field);
			st.addLog(st.isPvp()
					? "〈フィールド〉「" + nm + "」の効果が切れ、ゲストのレストに置かれた"
					: "〈フィールド〉「" + nm + "」の効果が切れ、相手のレストに置かれた");
		}
		st.setActiveField(null);
		st.setActiveFieldOwnerHuman(null);
		st.setDeathbounceFieldTurnsRemaining(0);
	}

	/**
	 * リヴァイアサン等: カウント付き〈フィールド〉の数字を進める（減らす）。ゼロ以下なら場を所有者レストへ。
	 */
	private void advanceActiveFieldCountForLeviathan(CpuBattleState st, int steps, Map<Short, CardDefinition> defs) {
		if (st == null || defs == null || steps <= 0) {
			return;
		}
		BattleCard f = st.getActiveField();
		if (f == null) {
			st.addLog("リヴァイアサン: 〈フィールド〉がないためカウントは進まなかった");
			return;
		}
		short id = f.getCardId();
		if (id == SCRAPYARD_FIELD_ID) {
			int n = st.getScrapyardFieldTurnsRemaining();
			if (n <= 0) {
				return;
			}
			int next = n - steps;
			if (next <= 0) {
				st.addLog("リヴァイアサン: 〈フィールド〉のカウントを進めた（廃棄工場がレストへ）");
				removeActiveScrapyardFieldToOwnerRestNow(st, defs);
			} else {
				st.setScrapyardFieldTurnsRemaining(next);
				st.addLog("リヴァイアサン: 〈フィールド〉のカウントが " + n + " から " + next + " になった（廃棄工場）");
			}
			return;
		}
		if (id == DEATHBOUNCE_FIELD_ID) {
			int n = st.getDeathbounceFieldTurnsRemaining();
			if (n <= 0) {
				return;
			}
			int next = n - steps;
			if (next <= 0) {
				st.addLog("リヴァイアサン: 〈フィールド〉のカウントを進めた（デスバウンスがレストへ）");
				removeActiveDeathbounceFieldToOwnerRestNow(st, defs);
			} else {
				st.setDeathbounceFieldTurnsRemaining(next);
				st.addLog("リヴァイアサン: 〈フィールド〉のカウントが " + n + " から " + next + " になった（デスバウンス）");
			}
			return;
		}
		if (id == GameConstants.ATLANTIS_FIELD_CARD_ID) {
			if (!st.isAtlantisAwaitingCount0()) {
				return;
			}
			int d = st.getAtlantisFieldCounterDisplay();
			if (d <= 0) {
				return;
			}
			Boolean owner = st.getActiveFieldOwnerHuman();
			if (owner == null) {
				return;
			}
			int next = Math.max(0, d - steps);
			st.setAtlantisFieldCounterDisplay(next);
			if (next <= 0 && st.isAtlantisAwaitingCount0()) {
				st.addLog("リヴァイアサン: 〈フィールド〉のカウントを進めた（アトランティスを解決）");
				executeAtlantisFieldCount0Resolution(st, owner.booleanValue(), defs);
			} else {
				st.addLog("リヴァイアサン: 〈フィールド〉のカウントが " + d + " から " + next + " になった（アトランティス）");
			}
			return;
		}
		if (id == GameConstants.WEEKLY_SHONEN_CAMP_FIELD_CARD_ID) {
			int n = st.getWeeklyShonenCampFieldCounterDisplay();
			if (n <= 0) {
				return;
			}
			int target = Math.max(0, n - steps);
			while (n > target) {
				if (n > 1) {
					n--;
					st.setWeeklyShonenCampFieldCounterDisplay(n);
					applyWeeklyShonenCampCounterMilestones(st, n);
				} else {
					break;
				}
			}
			if (n <= 0) {
				st.addLog("リヴァイアサン: 〈フィールド〉のカウントを進めた（週刊少年 CAMPがレストへ）");
				removeActiveWeeklyShonenCampToOwnerRestNow(st, defs);
			} else {
				st.addLog("リヴァイアサン: 〈フィールド〉のカウントが進んだ（週刊少年 CAMP）");
			}
			return;
		}
		if (id == GameConstants.WORLD_REBUILD_FIELD_CARD_ID) {
			int n = st.getWorldRebuildFieldCounterDisplay();
			if (n <= 0) {
				return;
			}
			int next = Math.max(0, n - steps);
			st.setWorldRebuildFieldCounterDisplay(next);
			if (next <= 0) {
				st.addLog("リヴァイアサン: 〈フィールド〉のカウントを進めた（世界の再構築を解決）");
				maybeExecuteWorldRebuildFieldCount0(st, defs);
				return;
			} else {
				st.addLog("リヴァイアサン: 〈フィールド〉のカウントが " + n + " から " + next + " になった（世界の再構築）");
			}
			return;
		}
		if (id == GameConstants.PAPER_CITY_FIELD_CARD_ID) {
			int n = st.getPaperCityFieldCounterDisplay();
			if (n <= 0) {
				return;
			}
			st.addLog("リヴァイアサン: 〈フィールド〉のカウントを進めた（ペーパーシティ）");
			advancePaperCityFieldCountSteps(st, steps, defs);
			return;
		}
		st.addLog("リヴァイアサン: 現在の〈フィールド〉にカウントがないためカウントは進まなかった");
	}

	/**
	 * デスバウンスが〈場〉から上書き等で失われたとき、同フィールドで付与した手札コスト+1だけを戻す。
	 */
	private static void stripDeathbouncePersistedHandPenalties(CpuBattleState st) {
		if (st == null) {
			return;
		}
		stripDeathbounceOnCardList(st.getHumanHand());
		stripDeathbounceOnCardList(st.getCpuHand());
		stripDeathbounceOnCardList(st.getHumanRest());
		stripDeathbounceOnCardList(st.getCpuRest());
		stripDeathbounceOnCardList(st.getHumanDeck());
		stripDeathbounceOnCardList(st.getCpuDeck());
		stripDeathbounceOnZone(st.getHumanBattle());
		stripDeathbounceOnZone(st.getCpuBattle());
	}

	private static void stripDeathbounceOnZone(ZoneFighter z) {
		if (z == null) {
			return;
		}
		stripDeathbounceOnCardList(z.getCostUnder());
		stripDeathbounceOnOneCard(z.getMain());
	}

	private static void stripDeathbounceOnCardList(List<BattleCard> list) {
		if (list == null) {
			return;
		}
		for (BattleCard c : list) {
			stripDeathbounceOnOneCard(c);
		}
	}

	private static void stripDeathbounceOnOneCard(BattleCard c) {
		if (c == null) {
			return;
		}
		if (c.getDeathbounceHandCostStacks() <= 0) {
			return;
		}
		c.setDeathbounceHandCostStacks(0);
	}

	/**
	 * 〈フィールド〉霊園教会 デスバウンス: バトルゾーンのアンデッド・ファイターはレストでなく手札へ戻り、
	 * 手札コスト+1 は {@link BattleCard#getDeathbounceHandCostStacks()} にのみ積む（墓守神父の -2 等の
	 * {@link BattleCard#getHandDeployCostModifier()} とは分離。〈場〉喪失時はスタックのみクリア）。
	 */
	private static boolean deathbounceFieldSendsUndeadMainToHand(CpuBattleState st, ZoneFighter z,
			Map<Short, CardDefinition> defs) {
		if (st == null || z == null || z.getMain() == null || defs == null) {
			return false;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != DEATHBOUNCE_FIELD_ID) {
			return false;
		}
		if (st.getDeathbounceFieldTurnsRemaining() <= 0) {
			return false;
		}
		CardDefinition md = defs.get(z.getMain().getCardId());
		if (!isNonFieldFighterCardDef(md)) {
			return false;
		}
		return CardAttributes.hasAttribute(md, z.getMain(), "UNDEAD");
	}

	private static void applyDeathbounceHandCostIncrease(BattleCard main) {
		if (main == null) {
			return;
		}
		main.setDeathbounceHandCostStacks(main.getDeathbounceHandCostStacks() + 1);
	}

	private boolean moveZoneToRestOrReturnToHand(CpuBattleState st, ZoneFighter z, List<BattleCard> rest, List<BattleCard> hand,
			Map<Short, CardDefinition> defs) {
		if (z == null || z.getMain() == null) return false;
		// コストカードは常にレストへ
		for (BattleCard c : z.getCostUnder()) {
			rest.add(c);
		}
		if (z.isReturnToHandOnKnock()) {
			hand.add(z.getMain());
			return true;
		}
		if (scrapyardFieldSendsGarakutaMainToHand(st, z, defs)) {
			hand.add(z.getMain());
			return true;
		}
		if (deathbounceFieldSendsUndeadMainToHand(st, z, defs)) {
			applyDeathbounceHandCostIncrease(z.getMain());
			hand.add(z.getMain());
			return true;
		}
		if (tryTransformFossilFighterToField(st, z, rest, defs)) {
			return false;
		}
		rest.add(z.getMain());
		return false;
	}

	private boolean tryTransformFossilFighterToField(CpuBattleState st, ZoneFighter z, List<BattleCard> rest,
			Map<Short, CardDefinition> defs) {
		if (st == null || z == null || z.getMain() == null || defs == null) {
			return false;
		}
		if (z.getMain().getCardId() != FOSSIL_FIGHTER_ID) {
			return false;
		}
		CardDefinition fd = defs.get(GameConstants.FOSSIL_FIELD_TRANSFORMS_TOKEN_CARD_ID);
		if (fd == null || !isFieldCard(fd)) {
			return false;
		}
		BattleCard main = z.getMain();
		main.setCardId(GameConstants.FOSSIL_FIELD_TRANSFORMS_TOKEN_CARD_ID);
		boolean hostSide = rest == st.getHumanRest();
		replaceActiveField(st, main, hostSide, defs);
		st.addLog("「化石」が「化石（フィールド）」に変化して〈場〉に置かれた");
		return true;
	}

	private void moveZoneToRest(ZoneFighter z, List<BattleCard> rest, CpuBattleState st, List<BattleCard> mainHand,
			Map<Short, CardDefinition> defs) {
		if (z == null || z.getMain() == null) {
			return;
		}
		for (BattleCard c : z.getCostUnder()) {
			rest.add(c);
		}
		if (scrapyardFieldSendsGarakutaMainToHand(st, z, defs)) {
			mainHand.add(z.getMain());
			return;
		}
		if (deathbounceFieldSendsUndeadMainToHand(st, z, defs)) {
			applyDeathbounceHandCostIncrease(z.getMain());
			mainHand.add(z.getMain());
			return;
		}
		if (tryTransformFossilFighterToField(st, z, rest, defs)) {
			return;
		}
		rest.add(z.getMain());
	}

	/**
	 * 手札から新しいファイターを配置する直前に呼ぶ。既に自バトルゾーンにファイターがいる場合は、
	 * 旧ファイター（とコスト下）をレストへ送るか、手札へ戻す設定なら手札へ戻す。
	 * 置き換えで前のカードが状態から消えると、「レストにカーバンクルがある」系の〈配置〉が誤って不発になる。
	 */
	private void retireOwnBattleZoneBeforeNewDeploy(CpuBattleState st, boolean humanHost, boolean addLog,
			Map<Short, CardDefinition> defs) {
		if (st == null) {
			return;
		}
		ZoneFighter old = humanHost ? st.getHumanBattle() : st.getCpuBattle();
		if (old == null) {
			return;
		}
		if (humanHost) {
			boolean toHand = moveZoneToRestOrReturnToHand(st, old, st.getHumanRest(), st.getHumanHand(), defs);
			st.setHumanBattle(null);
			if (addLog) {
				st.addLog(toHand ? "あなたのファイターは手札へ戻った" : "あなたのファイターがレストへ");
			}
		} else {
			boolean toHand = moveZoneToRestOrReturnToHand(st, old, st.getCpuRest(), st.getCpuHand(), defs);
			st.setCpuBattle(null);
			if (addLog) {
				st.addLog(toHand
						? (opponentActorLogLabel(st) + "のファイターは手札へ戻った")
						: (opponentActorLogLabel(st) + "のファイターがレストへ"));
			}
		}
	}

	/**
	 * ターン終了時にバトルゾーンの一時加算を消す。
	 * {@link ZoneFighter#getLevelUpDeployPowerBonus()} は毎ターン境界で必ずリセット（スカイアの対象外）。
	 * {@link ZoneFighter#getTemporaryPowerBonus()} のうち、〈神秘の大樹 スカイア〉が〈場〉にある間の
	 * 種族：エルフの分だけ相手ターンにも持続するため消さない（カード効果由来のみが本フィールドに入る）。
	 * 忍者の入れ替えによる強さ−2（{@link ZoneFighter#getNinjaSwapPowerPenalty()}）は相手ターン中も持続させるため、ここでは消さない。
	 */
	private void resetTurnBuffs(CpuBattleState st, Map<Short, CardDefinition> defs) {
		boolean skyArbor = defs != null && skyArborFieldPersistsElfTurnBuffs(st);
		if (st.getHumanBattle() != null) {
			st.getHumanBattle().setLevelUpDeployPowerBonus(0);
			if (!skyArbor || !isElfFighterInZone(st.getHumanBattle(), defs)) {
				st.getHumanBattle().setTemporaryPowerBonus(0);
			}
		}
		if (st.getCpuBattle() != null) {
			st.getCpuBattle().setLevelUpDeployPowerBonus(0);
			if (!skyArbor || !isElfFighterInZone(st.getCpuBattle(), defs)) {
				st.getCpuBattle().setTemporaryPowerBonus(0);
			}
		}
		st.setPowerSwapActive(false);
		st.setWeeklyShonenCampGlobalDeployCostPlusOneThisTurn(false);
	}

	/**
	 * 〈神秘の大樹 スカイア〉が〈場〉から去った直後。スカイアによって相手ターンまで伸びていた
	 * エルフの「ターン終了まで」のカード由来加算を、手番でない側から即座に取り除く。
	 */
	private void stripSkyaPersistedElfDeployBonusesOnFieldLoss(CpuBattleState st, Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		boolean humansTurn = st.isHumansTurn();
		clearSkyaOffTurnElfTemporaryDeployBonus(st.getHumanBattle(), true, humansTurn, defs);
		clearSkyaOffTurnElfTemporaryDeployBonus(st.getCpuBattle(), false, humansTurn, defs);
	}

	private static void clearSkyaOffTurnElfTemporaryDeployBonus(ZoneFighter z, boolean zoneOwnerIsHuman,
			boolean humansInteractiveTurn, Map<Short, CardDefinition> defs) {
		if (z == null || z.getMain() == null || !isElfFighterInZone(z, defs)) {
			return;
		}
		if (zoneOwnerIsHuman == humansInteractiveTurn) {
			return;
		}
		z.setTemporaryPowerBonus(0);
	}

	private static boolean skyArborFieldPersistsElfTurnBuffs(CpuBattleState st) {
		if (st == null || st.getActiveField() == null) {
			return false;
		}
		Short fid = st.getActiveField().getCardId();
		return fid != null && fid.shortValue() == MYSTERIOUS_TREE_SKYAR_FIELD_ID;
	}

	private static boolean isElfFighterInZone(ZoneFighter zf, Map<Short, CardDefinition> defs) {
		if (zf == null || zf.getMain() == null || defs == null) {
			return false;
		}
		CardDefinition d = defs.get(zf.getMain().getCardId());
		if (!isNonFieldFighterCardDef(d)) {
			return false;
		}
		return CardAttributes.hasAttribute(d, zf.getMain(), "ELF");
	}

	/**
	 * クリスタクル「次の配置 +N」は {@link ZoneFighter#getTemporaryPowerBonus()} / {@link ZoneFighter#getLevelUpDeployPowerBonus()} に含めない。
	 * ターン終了時の {@link #resetTurnBuffs} で消えないよう {@link CpuBattleState} 側の戦闘加算として持つ。
	 *
	 * @param levelUpDeployPowerBonus レベルアップ（レスト捨て・ストーン）のみの強さ加算。〈神秘の大樹 スカイア〉の相手ターン持続対象外。
	 */
	private void applyCrystakulBonusesToDeployedZone(CpuBattleState st, ZoneFighter z, int deployBonus,
			int levelUpDeployPowerBonus, boolean forHuman) {
		int cry = forHuman ? st.getHumanNextCrystakulDeployBonus() : st.getCpuNextCrystakulDeployBonus();
		int nonCry = deployBonus - cry;
		int lu = Math.max(0, levelUpDeployPowerBonus);
		if (lu > nonCry) {
			lu = nonCry;
		}
		z.setLevelUpDeployPowerBonus(lu);
		z.setTemporaryPowerBonus(nonCry - lu);
		if (forHuman) {
			st.setHumanCrystakulCombatBonus(st.getHumanCrystakulCombatBonus() + cry);
			st.setHumanNextCrystakulDeployBonus(0);
		} else {
			st.setCpuCrystakulCombatBonus(st.getCpuCrystakulCombatBonus() + cry);
			st.setCpuNextCrystakulDeployBonus(0);
		}
	}

	/**
	 * SPEC-777: 〈配置〉で出目が確定した直後、その時点の相手バトルゾーンの強さ未満なら即敗北（確認なし）。
	 * 以降は出目が固定のため、相手の強さが後から変わっても再判定しない。
	 *
	 * @param spec777OnHumanZone 配置した SPEC-777 が人間側（ホスト）バトルゾーンにあるか
	 * @return ゲーム終了をセットしたら true
	 */
	private boolean applySpec777DeployLossIfRollBelowOpponentAtDeploy(CpuBattleState st, Map<Short, CardDefinition> defs,
			boolean spec777OnHumanZone) {
		if (st == null || st.isGameOver() || defs == null) {
			return false;
		}
		ZoneFighter specZ = spec777OnHumanZone ? st.getHumanBattle() : st.getCpuBattle();
		ZoneFighter oppZ = spec777OnHumanZone ? st.getCpuBattle() : st.getHumanBattle();
		if (specZ == null || specZ.getMain() == null || specZ.getMain().getCardId() != SPEC_777_ID
				|| specZ.getSpec777RolledPower() <= 0 || specZ.getMain().isBlankEffects()) {
			return false;
		}
		int roll = specZ.getSpec777RolledPower();
		int oppPow = oppZ != null && oppZ.getMain() != null
				? effectiveBattlePower(oppZ, !spec777OnHumanZone, st, defs)
				: 0;
		if (oppZ != null && oppZ.getMain() != null && roll < oppPow) {
			st.setPendingChoice(null);
			st.setPendingEffect(null);
			st.setGameOver(true);
			st.setHumanWon(!spec777OnHumanZone);
			st.setPhase(BattlePhase.GAME_OVER);
			st.setLastMessage(spec777OnHumanZone ? "敗北（SPEC-777）"
					: (st.isPvp() ? "勝利（相手のSPEC-777）" : "勝利（CPUのSPEC-777）"));
			st.addLog(spec777OnHumanZone
					? "SPEC-777: 出目がその時点の相手未満のため敗北"
					: "SPEC-777: 相手の出目がその時点のあなた未満のため相手敗北");
			return true;
		}
		return false;
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
		int basePower = d != null && d.getBasePower() != null ? d.getBasePower() : 0;
		if (id == SPEC_777_ID && zf.getSpec777RolledPower() > 0) {
			basePower = zf.getSpec777RolledPower();
		}
		int p = basePower + zf.getMain().getBattleEndPowerBonus() + zf.getTemporaryPowerBonus() + zf.getLevelUpDeployPowerBonus();
		if (zf.getNinjaSwapPowerPenalty() > 0) {
			p -= zf.getNinjaSwapPowerPenalty();
		}
		p += fieldGloriaCarbunclePowerBonus(st, d, zf.getMain(), defs);
		p += fieldRagnarokKoukaNashiPowerBonus(st, d, zf.getMain(), defs);
		p += weeklyShonenCampComicPowerForFighter(st, d, zf.getMain(), defs);

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

		// ガラクタレッグ系（相手前列／デンジリオン＋レスト継承）: このファイターは〈常時〉による強さ加減を適用しない（先出し解決あり）
		boolean passivesSuppressedByOpponentGarakutaLeg = opponentGarakutaSuppressesFighterPassives(ownerIsHuman, st,
				defs);
		boolean passivesSuppressedByOpponentMikaelsWrath = (!ownerIsHuman && hasMikaelsWrathOnZone(st.getHumanBattle()))
				|| (ownerIsHuman && hasMikaelsWrathOnZone(st.getCpuBattle()));

		// 一時強化（古竜・クリスタクル・ボットバイク）
		if (st != null) {
			p += ownerIsHuman ? st.getHumanKoryuBonus() : st.getCpuKoryuBonus();
			p += ownerIsHuman ? st.getHumanCrystakulCombatBonus() : st.getCpuCrystakulCombatBonus();
		}
		p += zf.getBotBikeMechanicPowerBonus();
		if (st != null) {
			boolean oppTurnForZadkielOwner = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			if (oppTurnForZadkielOwner && zf.getZadkielOpponentTurnPowerBonus() > 0) {
				p += zf.getZadkielOpponentTurnPowerBonus();
			}
		}

		// 薬売り〈配置〉: 配置時点の所持ストーン数ぶん、相手ファイター強さ-1（スナップショット）
		if (!suppressOpponentEffects) {
			ZoneFighter oppZone = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (oppZone != null && oppZone.getMain() != null && oppZone.getMain().getCardId() == KUSURI_ID
					&& !fighterIgnoresKusuriDebuffDueToGarakutaLeg(zf, st, ownerIsHuman, defs)) {
				p -= oppZone.getKusuriOpponentDebuffFromDeployStones();
			}
		}

		if (zf.getMain().isBlankEffects()) {
			return Math.max(0, p);
		}

		if (passivesSuppressedByOpponentGarakutaLeg || passivesSuppressedByOpponentMikaelsWrath) {
			return Math.max(0, p);
		}

		if (id == DENZIRION_ID && st != null) {
			List<BattleCard> dRest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			if (dRest != null) {
				for (BattleCard rc : dRest) {
					if (isTuckedUnderOwnFighter(zf, rc)) {
						continue;
					}
					if (!isMachineFighterInRest(st, rc, defs)) {
						continue;
					}
					if (rc.getCardId() == DENZIRION_ID) {
						continue;
					}
					p += denzirionPassivePowerFromRestCard(rc.getCardId(), zf, ownerIsHuman, st, defs);
				}
			}
		}

		if (id == ARCHER_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && opp.getMain() != null) {
				BattleCard om = opp.getMain();
				CardDefinition od = defs.get(om.getCardId());
				if (!CardAttributes.hasAttribute(od, om, "DRAGON")) {
					p += 1;
				}
			}
		}

		if (id == DRAGON_RIDER_ID && ownerIsHuman) {
			if (restContainsAttribute(st, st.getHumanRest(), defs, "DRAGON")) {
				p += 4;
			}
		}
		if (id == DRAGON_RIDER_ID && !ownerIsHuman) {
			if (restContainsAttribute(st, st.getCpuRest(), defs, "DRAGON")) {
				p += 4;
			}
		}

		if (id == GAIKOTSU_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && opp.getMain() != null
					&& CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), opp.getMain(), "ELF")) {
				p += 2;
			}
		}

		if (id == RED_EYE_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && opp.getMain() != null
					&& CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), opp.getMain(), "HUMAN")) {
				p += 1;
			}
		}

		if (id == SHIREI_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && opp.getMain() != null
					&& !CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), opp.getMain(), "HUMAN")) {
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
				if (restCardHasTribe(st, defs.get(c.getCardId()), c, "UNDEAD")) {
					undead++;
				}
			}
			p += undead;
		}

		if (id == AQUA_GUARDIAN_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			int merfolk = 0;
			for (BattleCard c : rest) {
				if (isTuckedUnderOwnFighter(zf, c)) {
					continue;
				}
				if (restCardHasTribe(st, defs.get(c.getCardId()), c, "MERFOLK")) {
					merfolk++;
				}
			}
			p += merfolk;
		}

		if (id == SHINY_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			int kinds = countDistinctCarbuncleTypesInRest(st, zf, rest, defs);
			p += kinds * 2;
		}

		if (id == INK_KNIGHT_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			int ink = 0;
			for (BattleCard c : rest) {
				if (isTuckedUnderOwnFighter(zf, c)) {
					continue;
				}
				if (c.getCardId() == INK_KNIGHT_ID) {
					ink++;
				}
			}
			p += ink;
		}

		if (id == COMIC_HERO_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			p += countDistinctTribeSegmentsFromRestFighters(st, zf, rest, defs);
		}

		if (id == ANGEL_MAGE_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			if (rest != null) {
				for (BattleCard c : rest) {
					if (c == null) {
						continue;
					}
					if (isTuckedUnderOwnFighter(zf, c)) {
						continue;
					}
					if (c.getCardId() == ANGEL_MAGE_ID) {
						p += 2;
						break;
					}
				}
			}
		}

		if (id == GABRIEL_ID && gabrielCharacteristicCostContainsMiracle(zf)) {
			p += 1;
		}

		if (id == FROSTKRUL_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			if (oppTurn && restContainsAttribute(st, rest, defs, "CARBUNCLE")) {
				p += 3;
			}
		}

		if (id == NEMURY_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			if (oppTurn) {
				p += NEMURY_OPPONENT_TURN_POWER_BONUS;
			}
		}

		if (id == MINION_CHAMPION_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			if (oppTurn) {
				p += MINION_CHAMPION_OPPONENT_TURN_POWER_BONUS;
			}
		}

		if (id == GARAKUTA_ARM_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			if (oppTurn) {
				p += 1;
			}
		}

		if (id == ARTHUR_ID) {
			BattleCard field = st != null ? st.getActiveField() : null;
			if (field != null && field.getCardId() == FIELD_KAMUI_ID) {
				p += 3;
			}
		}

		if (id == STONIA_ID && st != null) {
			boolean ownTurn = ownerIsHuman ? st.isHumansTurn() : !st.isHumansTurn();
			if (ownTurn) {
				p += ownerIsHuman ? st.getHumanStones() : st.getCpuStones();
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
			if (id == SPEC_777_ID && zf.getSpec777RolledPower() > 0) {
				out.add(new BattlePowerModifierDto(SPEC_777_ID, "（出目" + zf.getSpec777RolledPower() + "）"));
			}
			if (zf.getLevelUpDeployPowerBonus() != 0) {
				out.add(new BattlePowerModifierDto(null, "レベルアップ（配置）"));
			}
			if (zf.getTemporaryPowerBonus() != 0) {
				out.add(new BattlePowerModifierDto(null, "配置時の一時加算（カード効果等）"));
			}
			if (zf.getNinjaSwapPowerPenalty() > 0) {
				out.add(new BattlePowerModifierDto(NINJA_ID, "（忍者・入れ替え−2）"));
			}
			if (fieldGloriaCarbunclePowerBonus(st, d, zf.getMain(), defs) > 0) {
				out.add(new BattlePowerModifierDto(FIELD_GLORIA_ID, "〈宝石の地〉"));
			}
			if (fieldRagnarokKoukaNashiPowerBonus(st, d, zf.getMain(), defs) > 0) {
				out.add(new BattlePowerModifierDto(FIELD_RAGNAROK_STRAIT_ID, "〈龍鱗海峡 ラグナロク〉"));
			}
			int shonenComicSup = weeklyShonenCampComicPowerForFighter(st, d, zf.getMain(), defs);
			if (shonenComicSup > 0) {
				out.add(new BattlePowerModifierDto(GameConstants.WEEKLY_SHONEN_CAMP_FIELD_CARD_ID,
						"（週刊少年 CAMP・コミック+" + shonenComicSup + "）"));
			}
			return out;
		}

		boolean suppressOpponentEffects = ownerIsHuman
				? hasRyuoh(st.getHumanBattle())
				: hasRyuoh(st.getCpuBattle());

		boolean passivesSuppressedByOpponentGarakutaLeg = opponentGarakutaSuppressesFighterPassives(ownerIsHuman, st,
				defs);
		boolean passivesSuppressedByOpponentMikaelsWrath = (!ownerIsHuman && hasMikaelsWrathOnZone(st.getHumanBattle()))
				|| (ownerIsHuman && hasMikaelsWrathOnZone(st.getCpuBattle()));

		if (zf.getLevelUpDeployPowerBonus() != 0) {
			out.add(new BattlePowerModifierDto(null, "レベルアップ（配置）"));
		}
		if (zf.getTemporaryPowerBonus() != 0) {
			out.add(new BattlePowerModifierDto(null, "配置時の一時加算（カード効果等）"));
		}

		if (zf.getNinjaSwapPowerPenalty() > 0) {
			out.add(new BattlePowerModifierDto(NINJA_ID, "（忍者・入れ替え−2）"));
		}

		if (fieldGloriaCarbunclePowerBonus(st, d, zf.getMain(), defs) > 0) {
			out.add(new BattlePowerModifierDto(FIELD_GLORIA_ID, "〈宝石の地〉"));
		}
		if (fieldRagnarokKoukaNashiPowerBonus(st, d, zf.getMain(), defs) > 0) {
			out.add(new BattlePowerModifierDto(FIELD_RAGNAROK_STRAIT_ID, "〈龍鱗海峡 ラグナロク〉"));
		}

		int shonenComic = weeklyShonenCampComicPowerForFighter(st, d, zf.getMain(), defs);
		if (shonenComic > 0) {
			out.add(new BattlePowerModifierDto(GameConstants.WEEKLY_SHONEN_CAMP_FIELD_CARD_ID,
					"（週刊少年 CAMP・コミック+" + shonenComic + "）"));
		}

		if (id == SPEC_777_ID && zf.getSpec777RolledPower() > 0) {
			out.add(new BattlePowerModifierDto(SPEC_777_ID, "（出目" + zf.getSpec777RolledPower() + "）"));
		}

		int koryu = ownerIsHuman ? st.getHumanKoryuBonus() : st.getCpuKoryuBonus();
		if (koryu > 0) {
			out.add(new BattlePowerModifierDto(KORYU_ID, null));
		}

		int cryst = ownerIsHuman ? st.getHumanCrystakulCombatBonus() : st.getCpuCrystakulCombatBonus();
		if (cryst > 0) {
			out.add(new BattlePowerModifierDto(CRYSTAKUL_ID, "（クリスタクル+" + cryst + "）"));
		}

		if (zf.getBotBikeMechanicPowerBonus() > 0) {
			out.add(new BattlePowerModifierDto(BOT_BIKE_ID, "（メカニック・次の相手ターン終了まで+3）"));
		}
		if (st != null) {
			boolean oppTurnZ = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			if (oppTurnZ && zf.getZadkielOpponentTurnPowerBonus() > 0) {
				out.add(new BattlePowerModifierDto(GameConstants.ZADKIEL_FIGHTER_CARD_ID,
						"（ザドキエル・相手ターン中+" + zf.getZadkielOpponentTurnPowerBonus() + "）"));
			}
		}

		if (!suppressOpponentEffects) {
			ZoneFighter oppZone = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (oppZone != null && oppZone.getMain() != null && oppZone.getMain().getCardId() == KUSURI_ID
					&& !fighterIgnoresKusuriDebuffDueToGarakutaLeg(zf, st, ownerIsHuman, defs)) {
				int debuff = oppZone.getKusuriOpponentDebuffFromDeployStones();
				if (debuff > 0) {
					out.add(new BattlePowerModifierDto(KUSURI_ID, "（相手の薬売り・配置時ストーン" + debuff + "）"));
				}
			}
		}

		if (zf.getMain().isBlankEffects()) {
			return out;
		}

		if (passivesSuppressedByOpponentGarakutaLeg || passivesSuppressedByOpponentMikaelsWrath) {
			return out;
		}

		if (id == DENZIRION_ID && st != null) {
			appendDenzirionPassiveExplain(zf, ownerIsHuman, st, defs, out);
		}

		if (id == ARCHER_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && opp.getMain() != null) {
				BattleCard om = opp.getMain();
				CardDefinition od = defs.get(om.getCardId());
				if (!CardAttributes.hasAttribute(od, om, "DRAGON")) {
					out.add(new BattlePowerModifierDto(ARCHER_ID, null));
				}
			}
		}

		if (id == DRAGON_RIDER_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			if (restContainsAttribute(st, rest, defs, "DRAGON")) {
				out.add(new BattlePowerModifierDto(DRAGON_RIDER_ID, "（レストのドラゴン）"));
			}
		}

		if (id == GAIKOTSU_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && opp.getMain() != null
					&& CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), opp.getMain(), "ELF")) {
				out.add(new BattlePowerModifierDto(opp.getMain().getCardId(), null));
			}
		}

		if (id == RED_EYE_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && opp.getMain() != null
					&& CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), opp.getMain(), "HUMAN")) {
				out.add(new BattlePowerModifierDto(RED_EYE_ID, null));
			}
		}

		if (id == SHIREI_ID) {
			ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
			if (opp != null && opp.getMain() != null
					&& !CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), opp.getMain(), "HUMAN")) {
				out.add(new BattlePowerModifierDto(opp.getMain().getCardId(), null));
			}
		}

		if (id == HONE_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			for (BattleCard c : rest) {
				if (isTuckedUnderOwnFighter(zf, c)) {
					continue;
				}
				if (restCardHasTribe(st, defs.get(c.getCardId()), c, "UNDEAD")) {
					out.add(new BattlePowerModifierDto(c.getCardId(), null));
				}
			}
		}

		if (id == AQUA_GUARDIAN_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			for (BattleCard c : rest) {
				if (isTuckedUnderOwnFighter(zf, c)) {
					continue;
				}
				if (restCardHasTribe(st, defs.get(c.getCardId()), c, "MERFOLK")) {
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
				if (c.getCardId() == SHINY_ID) {
					continue;
				}
				if (restCardHasTribe(st, defs.get(c.getCardId()), c, "CARBUNCLE") && seen.add(c.getCardId())) {
					out.add(new BattlePowerModifierDto(c.getCardId(), "（種類+2）"));
				}
			}
		}

		if (id == INK_KNIGHT_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			for (BattleCard c : rest) {
				if (isTuckedUnderOwnFighter(zf, c)) {
					continue;
				}
				if (c.getCardId() == INK_KNIGHT_ID) {
					out.add(new BattlePowerModifierDto(INK_KNIGHT_ID, null));
				}
			}
		}

		if (id == COMIC_HERO_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			int comicKinds = countDistinctTribeSegmentsFromRestFighters(st, zf, rest, defs);
			if (comicKinds > 0) {
				out.add(new BattlePowerModifierDto(COMIC_HERO_ID, "（レストの種族" + comicKinds + "種）"));
			}
		}

		if (id == ANGEL_MAGE_ID) {
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			boolean angelMageInRest = false;
			if (rest != null) {
				for (BattleCard c : rest) {
					if (c == null) {
						continue;
					}
					if (isTuckedUnderOwnFighter(zf, c)) {
						continue;
					}
					if (c.getCardId() == ANGEL_MAGE_ID) {
						angelMageInRest = true;
						break;
					}
				}
			}
			if (angelMageInRest) {
				out.add(new BattlePowerModifierDto(ANGEL_MAGE_ID, "（レストに「エンジェルメイジ」+2）"));
			}
		}

		if (id == GABRIEL_ID && gabrielCharacteristicCostContainsMiracle(zf)) {
			out.add(new BattlePowerModifierDto(GABRIEL_ID, "（コストに「奇跡」+1）"));
		}

		if (id == FROSTKRUL_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
			if (oppTurn && restContainsAttribute(st, rest, defs, "CARBUNCLE")) {
				out.add(new BattlePowerModifierDto(FROSTKRUL_ID, "（相手ターン・レスト条件）"));
			}
		}

		if (id == NEMURY_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			if (oppTurn) {
				out.add(new BattlePowerModifierDto(NEMURY_ID, "（相手ターン）"));
			}
		}

		if (id == MINION_CHAMPION_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			if (oppTurn) {
				out.add(new BattlePowerModifierDto(MINION_CHAMPION_ID, "（相手ターン）"));
			}
		}

		if (id == GARAKUTA_ARM_ID) {
			boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
			if (oppTurn) {
				out.add(new BattlePowerModifierDto(GARAKUTA_ARM_ID, "（相手ターン+1）"));
			}
		}

		if (id == ARTHUR_ID) {
			BattleCard field = st != null ? st.getActiveField() : null;
			if (field != null && field.getCardId() == FIELD_KAMUI_ID) {
				out.add(new BattlePowerModifierDto(FIELD_KAMUI_ID, "〈決戦の地 カムイ〉"));
			}
		}

		if (id == STONIA_ID && st != null) {
			boolean ownTurn = ownerIsHuman ? st.isHumansTurn() : !st.isHumansTurn();
			int stones = ownerIsHuman ? st.getHumanStones() : st.getCpuStones();
			if (ownTurn && stones > 0) {
				out.add(new BattlePowerModifierDto(STONIA_ID, "（自分のターン・所持ストーン" + stones + "）"));
			}
		}

		return out;
	}

	private boolean hasRyuoh(ZoneFighter z) {
		return z != null && z.getMain() != null && z.getMain().getCardId() == RYUOH_ID;
	}

	/** ガラクタレッグ（61）: 相手のファイターは〈常時〉が使えない */
	private static boolean hasGarakutaLeg(ZoneFighter z) {
		return z != null && z.getMain() != null && z.getMain().getCardId() == GARAKUTA_LEG_ID;
	}

	/**
	 * 磁力合体デンジリオンが、レストのガラクタレッグの〈常時〉（相手の〈常時〉を封じる）を継承しているか。
	 */
	private static boolean battleLineInheritsGarakutaFromRestDenzirion(ZoneFighter zf, CpuBattleState st,
			boolean zoneOwnerIsHuman, Map<Short, CardDefinition> defs) {
		if (zf == null || zf.getMain() == null || zf.getMain().getCardId() != DENZIRION_ID || defs == null || st == null) {
			return false;
		}
		List<BattleCard> rest = zoneOwnerIsHuman ? st.getHumanRest() : st.getCpuRest();
		if (rest == null) {
			return false;
		}
		for (BattleCard rc : rest) {
			if (isTuckedUnderOwnFighter(zf, rc)) {
				continue;
			}
			if (!isMachineFighterInRest(st, rc, defs)) {
				continue;
			}
			if (rc.getCardId() == DENZIRION_ID) {
				continue;
			}
			if (rc.getCardId() == GARAKUTA_LEG_ID) {
				return true;
			}
		}
		return false;
	}

	/** 前列がガラクタレッグ、またはデンジリオンがレストのガラクタレッグの〈常時〉を持つとき */
	private static boolean battleLineHasGarakutaLegStylePassive(ZoneFighter zf, CpuBattleState st,
			boolean zoneOwnerIsHuman, Map<Short, CardDefinition> defs) {
		return hasGarakutaLeg(zf)
				|| battleLineInheritsGarakutaFromRestDenzirion(zf, st, zoneOwnerIsHuman, defs);
	}

	/**
	 * 相手前列の「相手のファイターは〈常時〉効果が使えない」が、こちらの〈常時〉強さ加減を封じるか。
	 * 双方に同系があるときは {@link ZoneFighter#getBattleMainLineSeq()} が小さい（先に前列に置かれた）側のみ有効。
	 */
	private boolean opponentGarakutaSuppressesFighterPassives(boolean ownerIsHuman, CpuBattleState st,
			Map<Short, CardDefinition> defs) {
		ZoneFighter me = ownerIsHuman ? st.getHumanBattle() : st.getCpuBattle();
		ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
		if (!battleLineHasGarakutaLegStylePassive(opp, st, !ownerIsHuman, defs)) {
			return false;
		}
		if (!battleLineHasGarakutaLegStylePassive(me, st, ownerIsHuman, defs)) {
			return true;
		}
		int oSeq = opp.getBattleMainLineSeq();
		int mSeq = me.getBattleMainLineSeq();
		if (oSeq <= 0 || mSeq <= 0) {
			return true;
		}
		if (oSeq == mSeq) {
			return true;
		}
		return oSeq < mSeq;
	}

	/**
	 * 薬売り〈配置〉の「相手ファイター強さ−1／配置時ストーン」は、ガラクタレッグの〈常時〉対象外。
	 * 前列がガラクタレッグのほか、磁力合体デンジリオンがレストのガラクタレッグの〈常時〉を継承している場合も同様。
	 */
	private static boolean fighterIgnoresKusuriDebuffDueToGarakutaLeg(ZoneFighter zf, CpuBattleState st,
			boolean ownerIsHuman, Map<Short, CardDefinition> defs) {
		if (hasGarakutaLeg(zf)) {
			return true;
		}
		if (zf == null || zf.getMain() == null || st == null || defs == null) {
			return false;
		}
		if (zf.getMain().getCardId() != DENZIRION_ID) {
			return false;
		}
		List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
		if (rest == null) {
			return false;
		}
		for (BattleCard rc : rest) {
			if (isTuckedUnderOwnFighter(zf, rc)) {
				continue;
			}
			if (!isMachineFighterInRest(st, rc, defs)) {
				continue;
			}
			if (rc.getCardId() == DENZIRION_ID) {
				continue;
			}
			if (rc.getCardId() == GARAKUTA_LEG_ID) {
				return true;
			}
		}
		return false;
	}

	/** ミスティンクル（33）・ワイバーン（80）: 相手の〈配置〉のみ封じる（〈常時〉は対象外） */
	private static boolean opponentLineSuppressesDeployOnly(ZoneFighter z) {
		if (z == null || z.getMain() == null) {
			return false;
		}
		short id = z.getMain().getCardId();
		return id == MISTYINKUL_ID || id == WYVERN_ID;
	}

	/**
	 * 紅蓮峡谷 フレイムガルド: 〈フィールド〉が場にある間、その〈フィールド〉を置いたプレイヤーの相手の〈配置〉を封じる（〈常時〉は対象外）。
	 * 〈クリスタクル〉の任意ストーン〈配置〉のみ例外（ミスティンクル／ワイバーンと同様）。
	 */
	private boolean activeFieldFlameguardSuppressesOpponentDeploy(CpuBattleState st, boolean deployerIsHuman,
			CardDefinition abilityDef) {
		if (st == null) {
			return false;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != GameConstants.FLAMEGUARD_FIELD_CARD_ID) {
			return false;
		}
		Boolean ownerHuman = st.getActiveFieldOwnerHuman();
		if (ownerHuman == null) {
			return false;
		}
		if (deployerIsHuman == ownerHuman.booleanValue()) {
			return false;
		}
		return abilityDef == null || !isCrystakulCardDefinition(abilityDef);
	}

	/**
	 * 〈配置〉能力1枚分が、相手前列（竜王／ミスティンクル／ワイバーン）または相手側のフレイムガルド〈フィールド〉に封じられるか。
	 * 〈クリスタクル〉の〈配置〉（任意ストーン）のみミスティンクル／ワイバーン／フレイムガルドでは封じない。ネビュラ坑道で先にストーンが増えたあとに確認を出せるようにする。竜王は従来どおりすべて無効。
	 */
	private boolean deployAbilitySuppressedByOpponentLine(CpuBattleState st, boolean deployerIsHuman,
			CardDefinition abilityDef) {
		if (st == null) {
			return false;
		}
		ZoneFighter opp = deployerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
		if (hasRyuoh(opp)) {
			return true;
		}
		if (opponentLineSuppressesDeployOnly(opp)) {
			return abilityDef == null || !isCrystakulCardDefinition(abilityDef);
		}
		if (activeFieldFlameguardSuppressesOpponentDeploy(st, deployerIsHuman, abilityDef)) {
			return true;
		}
		return false;
	}

	/** ミカエルの怒り（116）が前列にいるか。 */
	private static boolean hasMikaelsWrathOnZone(ZoneFighter z) {
		return z != null && z.getMain() != null && z.getMain().getCardId() == GameConstants.MIKAEL_WRATH_CARD_ID;
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
	 * ミラージュクル: 相手の〈配置〉をコピーする際、ストーン支払いが必要な効果で不足なら確認ダイアログを出さない。
	 * {@link #applyDeployHuman} / {@link #applyDeployHumanAsCpuSide} の任意ストーン系分岐と整合させる。
	 */
	private static boolean mirajukulMirrorHasStonesForOpponentDeploy(CpuBattleState st, String abilityDeployCode,
			boolean mirageOwnerIsHuman) {
		if (st == null || abilityDeployCode == null) {
			return true;
		}
		String code = abilityDeployCode.trim();
		int stones = mirageOwnerIsHuman ? st.getHumanStones() : st.getCpuStones();
		return switch (code) {
			case "SAMURAI" -> stones >= 3;
			case "YOSEI", "NOROWARETA", "FUWAFUWA", "NIDONEBI", "KORYU", "SEASERPENT", "CELESTIA", RAMIEL_DEPLOY_CODE, "RESEARCHER_FLORA", "COMIC_WITCH" -> stones >= 1;
			case "CRYSTAKUL" -> stones >= CRYSTAKUL_OPTIONAL_STONE_COST;
			case "FEZARIA" -> stones >= FEZARIA_OPTIONAL_STONE_COST;
			default -> true;
		};
	}

	/**
	 * ミラージュクル: コピー先に成立しない〈配置〉は確認を出さない（例: コミックダイナソーは手札が1枚以上必要）。
	 */
	private static boolean mirajukulMirrorCanCopyOpponentDeploy(CpuBattleState st, String abilityDeployCode,
			boolean mirageOwnerIsHuman) {
		if (st == null || abilityDeployCode == null) {
			return true;
		}
		if (SKETCHER_DEPLOY_CODE.equals(abilityDeployCode.trim())) {
			List<BattleCard> h = mirageOwnerIsHuman ? st.getHumanHand() : st.getCpuHand();
			return h != null && !h.isEmpty();
		}
		if (COMIC_WITCH_DEPLOY_CODE.equals(abilityDeployCode.trim())) {
			List<String> ids = comicWitchPickableRestInstanceIds(st, mirageOwnerIsHuman);
			return !ids.isEmpty();
		}
		return true;
	}

	/**
	 * ミラージュクル: 相手に〈配置〉があれば、コピーするかの確認をキューする。
	 *
	 * @return 確認待ちを設定したら true
	 */
	private boolean offerMirajukulMirrorConfirmation(CpuBattleState st, boolean mirageOwnerIsHuman, boolean cpuSlotChooses,
			Map<Short, CardDefinition> defs) {
		ZoneFighter opp = mirageOwnerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
		if (opp == null || opp.getMain() == null) {
			return false;
		}
		CardDefinition oppFighter = defs.get(opp.getMain().getCardId());
		if (oppFighter == null) {
			return false;
		}
		String code = oppFighter.getAbilityDeployCode();
		if (code == null || code.isBlank()) {
			return false;
		}
		if ("MIRAJUKUL".equals(code)) {
			st.addLog("ミラージュクル: 相手もミラージュクルのためコピーできない");
			return false;
		}
		if (!mirajukulMirrorHasStonesForOpponentDeploy(st, code, mirageOwnerIsHuman)) {
			st.addLog("ミラージュクル: 必要なストーンが足りないため、相手の〈配置〉確認を出さない");
			return false;
		}
		if (!mirajukulMirrorCanCopyOpponentDeploy(st, code, mirageOwnerIsHuman)) {
			st.addLog("ミラージュクル: コピーに必要な手札がないため、相手の〈配置〉確認を出さない");
			return false;
		}
		String oppName = oppFighter.getName() != null ? oppFighter.getName() : "？";
		st.setPendingChoice(new PendingChoice(
				ChoiceKind.CONFIRM_MIRAJUKUL_MIRROR,
				"ミラージュクル: 相手の「" + oppName + "」と同じ〈配置〉を使いますか？",
				!cpuSlotChooses,
				code,
				0,
				List.of(),
				cpuSlotChooses));
		return true;
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
		CardDefinition deployAbilityDef = ghost;
		// 忍者は「メインが忍者」のときだけ入れ替える実装のため、ミラージュ本体のままではコピー時に入れ替わらない。コピーとして入れ替え後メインの〈配置〉を解決する。
		if ("NINJA".equals(code)) {
			if (applyNinjaPhysicalSwap(st, defs, mirageOwnerIsHuman, true)) {
				ZoneFighter zf = mirageOwnerIsHuman ? st.getHumanBattle() : st.getCpuBattle();
				BattleCard zm = zf != null ? zf.getMain() : null;
				CardDefinition swappedMainDef = zm != null ? defs.get(zm.getCardId()) : null;
				if (swappedMainDef != null) {
					deployAbilityDef = swappedMainDef;
				}
			}
		}
		if (mirageOwnerIsHuman) {
			applyDeployHuman(st, deployAbilityDef, defs, null);
		} else if (st.isPvp()) {
			applyDeployHumanAsCpuSide(st, deployAbilityDef, defs, null);
		} else {
			applyDeployCpu(st, deployAbilityDef, defs, rnd != null ? rnd : new Random(), null);
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

	/**
	 * ガブリエル〈常時〉: {@link ZoneFighter#getCostUnder()} の先頭 {@link ZoneFighter#getCostPayCardCount()} 枚が
	 * 特性コストとして使用したカード（レベルアップで下に重ねた枚は含まない）のうちに「奇跡」があるか。
	 */
	private static boolean gabrielCharacteristicCostContainsMiracle(ZoneFighter zf) {
		return zoneCharacteristicCostContainsCardId(zf, GameConstants.MIRACLE_TOKEN_CARD_ID)
				|| zoneCharacteristicCostContainsCardId(zf, GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID);
	}

	private void applyBehemothDeployStoneGain(CpuBattleState st, ZoneFighter zone, boolean ownerHuman) {
		if (st == null || zone == null || zone.getMain() == null
				|| zone.getMain().getCardId() != GameConstants.BEHEMOTH_FIGHTER_CARD_ID) {
			return;
		}
		if (!zoneCharacteristicCostContainsCardId(zone, GameConstants.DRAGON_EGG_CARD_ID)) {
			return;
		}
		if (ownerHuman) {
			st.setHumanStones(st.getHumanStones() + 3);
			st.addLog("ベヒモス: コストの「ドラゴンの卵」でストーン+3");
		} else {
			st.setCpuStones(st.getCpuStones() + 3);
			st.addLog("CPUベヒモス: コストの「ドラゴンの卵」でストーン+3");
		}
	}

	private static String fafnirDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "ファフニール";
		}
		if (cpuAiDeploy) {
			return "CPUファフニール";
		}
		return cpuSlotActorLogLabel(st) + "のファフニール";
	}

	/**
	 * ファフニール〈配置〉: 相手前列のファイターをレストへ。退けたファイターのその時点の強さと同じ分を、
	 * バトル終了までこのファイターに加算する。
	 */
	private void applyFafnirDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		String logP = fafnirDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		ZoneFighter selfZone = deployerIsHuman ? st.getHumanBattle() : st.getCpuBattle();
		if (selfZone == null || selfZone.getMain() == null
				|| selfZone.getMain().getCardId() != GameConstants.FAFNIR_FIGHTER_CARD_ID) {
			return;
		}
		ZoneFighter oppZone = deployerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
		if (oppZone == null || oppZone.getMain() == null) {
			st.addLog(logP + ": 相手バトルゾーンにファイターがいなかった");
			return;
		}
		CardDefinition oppDef = defs.get(oppZone.getMain().getCardId());
		if (oppDef == null || isFieldCard(oppDef)) {
			st.addLog(logP + ": 相手バトルゾーンにファイターがいなかった");
			return;
		}
		boolean oppOwnerIsHuman = !deployerIsHuman;
		int powerTaken = effectiveBattlePower(oppZone, oppOwnerIsHuman, st, defs);
		List<BattleCard> oppRest = deployerIsHuman ? st.getCpuRest() : st.getHumanRest();
		List<BattleCard> oppHand = deployerIsHuman ? st.getCpuHand() : st.getHumanHand();
		moveZoneToRest(oppZone, oppRest, st, oppHand, defs);
		if (deployerIsHuman) {
			st.setCpuBattle(null);
		} else {
			st.setHumanBattle(null);
		}
		BattleCard selfMain = selfZone.getMain();
		selfMain.setBattleEndPowerBonus(selfMain.getBattleEndPowerBonus() + powerTaken);
		st.addLog(logP + ": 相手のファイターをレストへ、バトル終了まで強さ+" + powerTaken);
	}

	private static String bahamutDeployLogPrefix(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy) {
		if (deployerIsHuman) {
			return "バハムート";
		}
		if (cpuAiDeploy) {
			return "CPUバハムート";
		}
		return cpuSlotActorLogLabel(st) + "のバハムート";
	}

	/**
	 * 相手前列のファイターをレストへ（いなければ false）。
	 */
	private boolean knockOpponentFighterToRest(CpuBattleState st, boolean deployerIsHuman,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return false;
		}
		ZoneFighter oppZone = deployerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
		if (oppZone == null || oppZone.getMain() == null) {
			return false;
		}
		CardDefinition oppDef = defs.get(oppZone.getMain().getCardId());
		if (oppDef == null || isFieldCard(oppDef)) {
			return false;
		}
		List<BattleCard> oppRest = deployerIsHuman ? st.getCpuRest() : st.getHumanRest();
		List<BattleCard> oppHand = deployerIsHuman ? st.getCpuHand() : st.getHumanHand();
		moveZoneToRest(oppZone, oppRest, st, oppHand, defs);
		if (deployerIsHuman) {
			st.setCpuBattle(null);
		} else {
			st.setHumanBattle(null);
		}
		return true;
	}

	/**
	 * バハムート〈配置〉: ストーン+2、相手ファイターをレストへ、場の〈フィールド〉を配置者側のレストへ。
	 */
	private void applyBahamutDeployEffect(CpuBattleState st, boolean deployerIsHuman, boolean cpuAiDeploy,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		String logP = bahamutDeployLogPrefix(st, deployerIsHuman, cpuAiDeploy);
		ZoneFighter selfZone = deployerIsHuman ? st.getHumanBattle() : st.getCpuBattle();
		if (selfZone == null || selfZone.getMain() == null
				|| selfZone.getMain().getCardId() != GameConstants.BAHAMUT_FIGHTER_CARD_ID) {
			return;
		}
		if (deployerIsHuman) {
			st.setHumanStones(st.getHumanStones() + 2);
		} else {
			st.setCpuStones(st.getCpuStones() + 2);
		}
		st.addLog(logP + ": ストーン+2");
		if (knockOpponentFighterToRest(st, deployerIsHuman, defs)) {
			st.addLog(logP + ": 相手のファイターをレストへ");
		} else {
			st.addLog(logP + ": 相手バトルゾーンにファイターがいなかった");
		}
		if (st.getActiveField() != null) {
			replaceActiveField(st, null, deployerIsHuman, defs);
		} else {
			st.addLog(logP + ": 場に〈フィールド〉がなかった");
		}
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

	/** 研究者フローラ: 自分レストに選べる「種族：エルフ」のカードがあるか（コスト下に差した枚は除外） */
	private static boolean restContainsPickableElfForResearcherFlora(CpuBattleState st, List<BattleCard> rest,
			ZoneFighter ownBattle, Map<Short, CardDefinition> defs) {
		if (rest == null || defs == null) {
			return false;
		}
		for (BattleCard c : rest) {
			if (c == null || isTuckedUnderOwnFighter(ownBattle, c)) {
				continue;
			}
			if (restCardHasTribe(st, defs.get(c.getCardId()), c, "ELF")) {
				return true;
			}
		}
		return false;
	}

	/** コミックウィッチ: レストから選べるカード（コスト下に差した枚は除外）の instanceId 一覧 */
	private static List<String> comicWitchPickableRestInstanceIds(CpuBattleState st, boolean humanSlot) {
		List<BattleCard> rest = humanSlot ? st.getHumanRest() : st.getCpuRest();
		ZoneFighter zb = humanSlot ? st.getHumanBattle() : st.getCpuBattle();
		List<String> out = new ArrayList<>();
		if (rest == null) {
			return out;
		}
		for (BattleCard bc : rest) {
			if (bc == null || bc.getInstanceId() == null) {
				continue;
			}
			if (isTuckedUnderOwnFighter(zb, bc)) {
				continue;
			}
			out.add(bc.getInstanceId());
		}
		return out;
	}

	/** 助手〈配置〉の選択対象: カード名に「研究者」を含む（部分一致・全角そのまま） */
	private static boolean cardNameContainsKenkyusha(CardDefinition def) {
		if (def == null || def.getName() == null) {
			return false;
		}
		return def.getName().contains("研究者");
	}

	/**
	 * 忍者: 配置直後にメインとコスト支払い先頭カードを入れ替え、入れ替え後のメインの強さを -2。
	 *
	 * @return コストにカードがあり実際に入れ替えた場合 true（入れ替え先の〈配置〉を別途解決）
	 */
	private boolean applyNinjaPhysicalSwap(CpuBattleState st, Map<Short, CardDefinition> defs, boolean humanHost) {
		return applyNinjaPhysicalSwap(st, defs, humanHost, false);
	}

	/**
	 * 忍者の入れ替え。{@code mirageMirrorCopiedNinja} が true のときは、メインがミラージュクルでも相手忍者の〈配置〉コピーとして入れ替える。
	 *
	 * @return コストにカードがあり実際に入れ替えた場合 true（入れ替え先の〈配置〉を別途解決）
	 */
	private boolean applyNinjaPhysicalSwap(CpuBattleState st, Map<Short, CardDefinition> defs, boolean humanHost,
			boolean mirageMirrorCopiedNinja) {
		ZoneFighter z = humanHost ? st.getHumanBattle() : st.getCpuBattle();
		if (z == null || z.getMain() == null) {
			return false;
		}
		CardDefinition mainDef = defs.get(z.getMain().getCardId());
		boolean mainIsNinja = mainDef != null && mainDef.getAbilityDeployCode() != null
				&& "NINJA".equals(mainDef.getAbilityDeployCode());
		boolean mainIsMirageCopyingNinja = mirageMirrorCopiedNinja && mainDef != null
				&& mainDef.getAbilityDeployCode() != null && "MIRAJUKUL".equals(mainDef.getAbilityDeployCode());
		if (!mainIsNinja && !mainIsMirageCopyingNinja) {
			return false;
		}
		if (z.getCostPayCardCount() > 0 && !z.getCostUnder().isEmpty()) {
			BattleCard paidCard = z.getCostUnder().get(0);
			BattleCard oldMain = z.getMain();
			z.setMain(paidCard);
			z.setBattleMainLineSeq(st.takeNextBattleMainLineSeq());
			z.getCostUnder().set(0, oldMain);
			z.setNinjaSwapPowerPenalty(NINJA_SWAP_POWER_PENALTY);
			z.setSpec777RolledPower(0);
			CardDefinition paidDef = defs.get(paidCard.getCardId());
			String paidName = paidDef != null && paidDef.getName() != null ? paidDef.getName() : "？";
			st.addLog("忍者: 「" + paidName + "」と入れ替え（強さ−" + NINJA_SWAP_POWER_PENALTY + "）");
			return true;
		}
		st.addLog("忍者: コストにカードがないため入れ替えなし");
		return false;
	}

	private void syncPendingEffectMainAfterNinjaSwap(PendingEffect pe, CpuBattleState st, Map<Short, CardDefinition> defs,
			boolean humanHost) {
		if (pe == null) {
			return;
		}
		ZoneFighter z = humanHost ? st.getHumanBattle() : st.getCpuBattle();
		if (z == null || z.getMain() == null) {
			return;
		}
		pe.setMainInstanceId(z.getMain().getInstanceId());
		pe.setCardId(z.getMain().getCardId());
		CardDefinition dm = defs.get(z.getMain().getCardId());
		if (dm != null) {
			String ac = dm.getAbilityDeployCode();
			pe.setAbilityDeployCode(ac != null && !ac.isBlank() ? ac : "");
		}
	}

	private void resolveNinjaSwappedDeployEffects(CpuBattleState st, Map<Short, CardDefinition> defs, Random rnd,
			boolean pendingOnHumanSlot, boolean pendingOnCpuSlot) {
		ZoneFighter z;
		boolean onHumanSlot;
		if (pendingOnHumanSlot) {
			z = st.getHumanBattle();
			onHumanSlot = true;
		} else if (pendingOnCpuSlot) {
			z = st.getCpuBattle();
			onHumanSlot = false;
		} else {
			// pending とゾーンの instanceId が一瞬ずれる等で両方 false でも、入れ替え後メインはどちらかにいるはず
			PendingEffect pe = st.getPendingEffect();
			String mid = pe != null ? pe.getMainInstanceId() : null;
			z = null;
			onHumanSlot = false;
			if (mid != null) {
				ZoneFighter hz = st.getHumanBattle();
				if (hz != null && hz.getMain() != null && mid.equals(hz.getMain().getInstanceId())) {
					z = hz;
					onHumanSlot = true;
				} else {
					ZoneFighter cz = st.getCpuBattle();
					if (cz != null && cz.getMain() != null && mid.equals(cz.getMain().getInstanceId())) {
						z = cz;
						onHumanSlot = false;
					}
				}
			}
			if (z == null) {
				return;
			}
		}
		if (z == null || z.getMain() == null) {
			return;
		}
		BattleCard dm = z.getMain();
		CardDefinition d = defs.get(dm.getCardId());
		if (d == null) {
			return;
		}
		if (onHumanSlot) {
			applyDeployHuman(st, d, defs, dm);
		} else if (st.isPvp()) {
			applyDeployHumanAsCpuSide(st, d, defs, dm);
		} else {
			applyDeployCpu(st, d, defs, rnd != null ? rnd : new Random(), dm);
		}
	}

	private boolean restContainsAttribute(CpuBattleState st, List<BattleCard> rest, Map<Short, CardDefinition> defs, String attr) {
		for (BattleCard c : rest) {
			if (restCardHasTribe(st, defs.get(c.getCardId()), c, attr)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * フェザリアの回収対象: 〈フィールド〉を除く自分のファイターで「種族：カーバンクル」、かつ「フェザリア」カード自身は除く。
	 * バトルゾーンのコスト下に重なっているインスタンスはレスト一覧に残っていてもレスト扱いにしない（シャイニ等と同じ）。
	 * 種族は {@link CardAttributes#hasAttribute(CardDefinition, BattleCard, String)}（SPEC-666 等の上書きを反映。アンデッドのみのカードは回収不可）。
	 */
	private static boolean isFezariaPickableCarbuncleInRest(CpuBattleState st, BattleCard c, ZoneFighter ownBattle,
			Map<Short, CardDefinition> defs) {
		if (c == null || defs == null) {
			return false;
		}
		if (ownBattle != null && isTuckedUnderOwnFighter(ownBattle, c)) {
			return false;
		}
		if (c.getCardId() == FEATHERIA_ID) {
			return false;
		}
		CardDefinition d = defs.get(c.getCardId());
		if (!isNonFieldFighterCardDef(d)) {
			return false;
		}
		return restCardHasTribe(st, d, c, "CARBUNCLE");
	}

	private static boolean isLeviathanPickableDragonOrMerfolkInRest(CpuBattleState st, BattleCard c, ZoneFighter ownBattle,
			Map<Short, CardDefinition> defs) {
		if (c == null || defs == null) {
			return false;
		}
		if (ownBattle != null && isTuckedUnderOwnFighter(ownBattle, c)) {
			return false;
		}
		CardDefinition d = defs.get(c.getCardId());
		if (!isNonFieldFighterCardDef(d)) {
			return false;
		}
		return restCardHasTribe(st, d, c, "DRAGON") || restCardHasTribe(st, d, c, "MERFOLK");
	}

	private static List<String> leviathanDragonMerfolkRestOptionIds(CpuBattleState st, boolean humanSlot,
			Map<Short, CardDefinition> defs) {
		List<BattleCard> rest = humanSlot ? st.getHumanRest() : st.getCpuRest();
		ZoneFighter zb = humanSlot ? st.getHumanBattle() : st.getCpuBattle();
		List<String> out = new ArrayList<>();
		if (rest == null || defs == null) {
			return out;
		}
		for (BattleCard c : rest) {
			if (isLeviathanPickableDragonOrMerfolkInRest(st, c, zb, defs)) {
				out.add(c.getInstanceId());
			}
		}
		return out;
	}

	private static boolean isSeraphimPickableAngelInRest(CpuBattleState st, BattleCard c, ZoneFighter ownBattle,
			Map<Short, CardDefinition> defs) {
		if (c == null || defs == null) {
			return false;
		}
		if (ownBattle != null && isTuckedUnderOwnFighter(ownBattle, c)) {
			return false;
		}
		CardDefinition d = defs.get(c.getCardId());
		return restCardHasTribe(st, d, c, "ANGEL");
	}

	private static List<String> seraphimAngelRestOptionIds(CpuBattleState st, boolean humanSlot,
			Map<Short, CardDefinition> defs) {
		List<BattleCard> rest = humanSlot ? st.getHumanRest() : st.getCpuRest();
		ZoneFighter zb = humanSlot ? st.getHumanBattle() : st.getCpuBattle();
		List<String> out = new ArrayList<>();
		if (rest == null || defs == null) {
			return out;
		}
		for (BattleCard c : rest) {
			if (c != null && c.getInstanceId() != null && isSeraphimPickableAngelInRest(st, c, zb, defs)) {
				out.add(c.getInstanceId());
			}
		}
		return out;
	}

	private boolean restContainsFezariaPickableCarbuncle(CpuBattleState st, List<BattleCard> rest, ZoneFighter ownBattle,
			Map<Short, CardDefinition> defs) {
		if (rest == null) {
			return false;
		}
		for (BattleCard c : rest) {
			if (isFezariaPickableCarbuncleInRest(st, c, ownBattle, defs)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * シャイニ: レストの「シャイニ」以外の「種族：カーバンクル」カードの種類数（同じカードIDの重複は1種類）。
	 */
	private int countDistinctCarbuncleTypesInRest(CpuBattleState st, ZoneFighter ownBattle, List<BattleCard> rest,
			Map<Short, CardDefinition> defs) {
		if (rest == null || defs == null) {
			return 0;
		}
		Set<Short> kinds = new HashSet<>();
		for (BattleCard c : rest) {
			if (c == null || isTuckedUnderOwnFighter(ownBattle, c)) {
				continue;
			}
			if (c.getCardId() == SHINY_ID) {
				continue;
			}
			if (restCardHasTribe(st, defs.get(c.getCardId()), c, "CARBUNCLE")) {
				kinds.add(c.getCardId());
			}
		}
		return kinds.size();
	}

	/** デンジリオン継承元: 〈フィールド〉以外のマシン・ファイター */
	private static boolean isMachineFighterRestSourceForDenziron(CardDefinition d) {
		if (d == null) {
			return false;
		}
		return isNonFieldFighterCardDef(d) && CardAttributes.hasAttribute(d, "MACHINE");
	}

	/**
	 * レストにある sourceCardId の〈常時〉を、前列のデンジリオンが持つとしたときの強さ増減。
	 * レストに同種が複数ある場合は枚数分だけ加算し、効果が重複する。
	 */
	private int denzirionPassivePowerFromRestCard(short sourceCardId, ZoneFighter battleZf, boolean ownerIsHuman,
			CpuBattleState st, Map<Short, CardDefinition> defs) {
		if (defs == null || battleZf == null || st == null) {
			return 0;
		}
		return switch (sourceCardId) {
			case ARCHER_ID -> {
				ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
				if (opp != null && opp.getMain() != null) {
					BattleCard om = opp.getMain();
					CardDefinition od = defs.get(om.getCardId());
					yield !CardAttributes.hasAttribute(od, om, "DRAGON") ? 1 : 0;
				}
				yield 0;
			}
			case DRAGON_RIDER_ID -> {
				List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
				yield restContainsAttribute(st, rest, defs, "DRAGON") ? 4 : 0;
			}
			case GAIKOTSU_ID -> {
				ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
				if (opp != null && opp.getMain() != null
						&& CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), opp.getMain(), "ELF")) {
					yield 2;
				}
				yield 0;
			}
			case RED_EYE_ID -> {
				ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
				if (opp != null && opp.getMain() != null
						&& CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), opp.getMain(), "HUMAN")) {
					yield 1;
				}
				yield 0;
			}
			case SHIREI_ID -> {
				ZoneFighter opp = ownerIsHuman ? st.getCpuBattle() : st.getHumanBattle();
				if (opp != null && opp.getMain() != null
						&& !CardAttributes.hasAttribute(defs.get(opp.getMain().getCardId()), opp.getMain(), "HUMAN")) {
					yield 1;
				}
				yield 0;
			}
			case HONE_ID -> {
				List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
				int undead = 0;
				for (BattleCard c : rest) {
					if (isTuckedUnderOwnFighter(battleZf, c)) {
						continue;
					}
					if (restCardHasTribe(st, defs.get(c.getCardId()), c, "UNDEAD")) {
						undead++;
					}
				}
				yield undead;
			}
			case SHINY_ID -> {
				List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
				int kinds = countDistinctCarbuncleTypesInRest(st, battleZf, rest, defs);
				yield kinds * 2;
			}
			case FROSTKRUL_ID -> {
				boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
				List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
				yield oppTurn && restContainsAttribute(st, rest, defs, "CARBUNCLE") ? 3 : 0;
			}
			case NEMURY_ID -> {
				boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
				yield oppTurn ? NEMURY_OPPONENT_TURN_POWER_BONUS : 0;
			}
			case MINION_CHAMPION_ID -> {
				boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
				yield oppTurn ? MINION_CHAMPION_OPPONENT_TURN_POWER_BONUS : 0;
			}
			case ARTHUR_ID -> {
				BattleCard field = st.getActiveField();
				yield field != null && field.getCardId() == FIELD_KAMUI_ID ? 3 : 0;
			}
			case STONIA_ID -> {
				boolean ownTurn = ownerIsHuman ? st.isHumansTurn() : !st.isHumansTurn();
				yield ownTurn ? (ownerIsHuman ? st.getHumanStones() : st.getCpuStones()) : 0;
			}
			case GARAKUTA_ARM_ID -> {
				boolean oppTurn = ownerIsHuman ? !st.isHumansTurn() : st.isHumansTurn();
				yield oppTurn ? 1 : 0;
			}
			default -> 0;
		};
	}

	private void appendDenzirionPassiveExplain(ZoneFighter zf, boolean ownerIsHuman, CpuBattleState st,
			Map<Short, CardDefinition> defs, List<BattlePowerModifierDto> out) {
		if (zf == null || st == null || defs == null) {
			return;
		}
		List<BattleCard> rest = ownerIsHuman ? st.getHumanRest() : st.getCpuRest();
		if (rest == null) {
			return;
		}
		for (BattleCard rc : rest) {
			if (isTuckedUnderOwnFighter(zf, rc)) {
				continue;
			}
			if (!isMachineFighterInRest(st, rc, defs)) {
				continue;
			}
			if (rc.getCardId() == DENZIRION_ID) {
				continue;
			}
			int b = denzirionPassivePowerFromRestCard(rc.getCardId(), zf, ownerIsHuman, st, defs);
			if (b != 0) {
				String sign = b > 0 ? "+" : "";
				out.add(new BattlePowerModifierDto(rc.getCardId(), "（デンジリオン継承" + sign + b + "）"));
			}
		}
	}

	/**
	 * SPEC-666: 次にそのスロットに出すファイター予定だった場合、配置直後のメインに種族上書きを付与する。
	 * 〈フィールド〉は対象外（フラグも消費しない）。
	 */
	private void applySpec666UndeadToDeployedFighterIfPending(CpuBattleState st, boolean humanSlot,
			Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		ZoneFighter z = humanSlot ? st.getHumanBattle() : st.getCpuBattle();
		if (z == null || z.getMain() == null) {
			return;
		}
		BattleCard main = z.getMain();
		CardDefinition mainDef = defs.get(main.getCardId());
		if (mainDef == null || isFieldCard(mainDef)) {
			return;
		}
		boolean pending = humanSlot ? st.isSpec666NextHumanUndead() : st.isSpec666NextCpuUndead();
		if (!pending) {
			return;
		}
		appendBattleTribeSegmentIfMissing(main, "UNDEAD");
		if (humanSlot) {
			st.setSpec666NextHumanUndead(false);
		} else {
			st.setSpec666NextCpuUndead(false);
		}
		st.addLog("SPEC-666: 配置したファイターを種族・アンデッドとして扱う");
	}

	/** メカニック: 次の配置でスタックを消費したファイターに、バトル終了まで種族マシンを付与（既存上書きと合成）。 */
	private void applyMechanicMachineTribeToDeployedFighterIfUsed(CpuBattleState st, boolean humanSlot,
			int mechanicStacksConsumed, Map<Short, CardDefinition> defs) {
		if (st == null || defs == null || mechanicStacksConsumed <= 0) {
			return;
		}
		ZoneFighter z = humanSlot ? st.getHumanBattle() : st.getCpuBattle();
		if (z == null || z.getMain() == null) {
			return;
		}
		BattleCard main = z.getMain();
		CardDefinition mainDef = defs.get(main.getCardId());
		if (mainDef == null || isFieldCard(mainDef)) {
			return;
		}
		appendBattleTribeSegmentIfMissing(main, "MACHINE");
		st.addLog("メカニック: 配置したファイターを種族・マシンとして扱う（バトル終了まで）");
	}

	/** 鳥獣戯画〈フィールド〉: 各スロットの次のファイター配置にドラゴン／人間を付与（場に鳥獣戯画がある間のみ・各1回）。 */
	private void applyChojuGigaTribeIfPending(CpuBattleState st, boolean humanSlot, Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != GameConstants.CHOJU_GIGA_FIELD_CARD_ID) {
			return;
		}
		ZoneFighter z = humanSlot ? st.getHumanBattle() : st.getCpuBattle();
		if (z == null || z.getMain() == null) {
			return;
		}
		BattleCard main = z.getMain();
		CardDefinition mainDef = defs.get(main.getCardId());
		if (mainDef == null || isFieldCard(mainDef)) {
			return;
		}
		if (humanSlot && st.isChojuGigaPendingHumanSlotNextDeployDragon()) {
			st.setChojuGigaPendingHumanSlotNextDeployDragon(false);
			appendBattleTribeSegmentIfMissing(main, "DRAGON");
			st.addLog("鳥獣戯画: 配置したファイターを種族・ドラゴンとして扱う（バトル終了まで）");
		} else if (!humanSlot && st.isChojuGigaPendingCpuSlotNextDeployHuman()) {
			st.setChojuGigaPendingCpuSlotNextDeployHuman(false);
			appendBattleTribeSegmentIfMissing(main, "HUMAN");
			st.addLog("鳥獣戯画: 配置したファイターを種族・人間として扱う（バトル終了まで）");
		}
	}

	private static void appendBattleTribeSegmentIfMissing(BattleCard main, String segment) {
		if (main == null || segment == null || segment.isBlank()) {
			return;
		}
		String cur = main.getBattleTribeOverride();
		if (cur == null || cur.isBlank()) {
			main.setBattleTribeOverride(segment);
			return;
		}
		if (CardAttributes.hasAttribute(cur, segment)) {
			return;
		}
		main.setBattleTribeOverride(cur + "_" + segment);
	}

	private static boolean restContainsMikaelMinionExcludingTucked(ZoneFighter zf, List<BattleCard> rest, short cardId) {
		if (rest == null) {
			return false;
		}
		for (BattleCard c : rest) {
			if (c == null) {
				continue;
			}
			if (zf != null && isTuckedUnderOwnFighter(zf, c)) {
				continue;
			}
			if (c.getCardId() == cardId) {
				return true;
			}
		}
		return false;
	}

	/** 〈配置〉コストとして支払っている {@link ZoneFighter#getCostUnder()} 先頭 {@link ZoneFighter#getCostPayCardCount()} 枚の「奇跡」の instanceId を登録（ミカエル効果の対象外）。 */
	private static void addMiracleInstanceIdsInDeployCostPaymentSlice(ZoneFighter zf, Set<String> out) {
		if (zf == null || zf.getCostUnder() == null || out == null) {
			return;
		}
		List<BattleCard> under = zf.getCostUnder();
		int n = Math.min(Math.max(0, zf.getCostPayCardCount()), under.size());
		for (int i = 0; i < n; i++) {
			BattleCard c = under.get(i);
			if (c != null && c.getCardId() == GameConstants.MIRACLE_TOKEN_CARD_ID && c.getInstanceId() != null) {
				out.add(c.getInstanceId());
			}
		}
	}

	/** ゾーンから「奇跡」（{@link GameConstants#MIRACLE_TOKEN_CARD_ID}）を取り除き {@code pulled} に追加。{@code excludeInstanceIds} にある instanceId は除外。レストでは前列 costUnder に差した枚も除外。 */
	private static void pullMiracleTokensFromZoneExcluding(List<BattleCard> zone, List<BattleCard> pulled,
			Set<String> excludeInstanceIds, ZoneFighter ownBattle, boolean skipIfTuckedUnderBattle) {
		if (zone == null || pulled == null) {
			return;
		}
		Set<String> ex = excludeInstanceIds != null ? excludeInstanceIds : Collections.emptySet();
		for (int i = zone.size() - 1; i >= 0; i--) {
			BattleCard c = zone.get(i);
			if (c == null || c.getCardId() != GameConstants.MIRACLE_TOKEN_CARD_ID) {
				continue;
			}
			if (skipIfTuckedUnderBattle && ownBattle != null && isTuckedUnderOwnFighter(ownBattle, c)) {
				continue;
			}
			String iid = c.getInstanceId();
			if (iid != null && ex.contains(iid)) {
				continue;
			}
			zone.remove(i);
			pulled.add(c);
		}
	}

	private static int countMiracleTokensInHandExcluding(List<BattleCard> hand, Set<String> excludeInstanceIds) {
		if (hand == null) {
			return 0;
		}
		Set<String> ex = excludeInstanceIds != null ? excludeInstanceIds : Collections.emptySet();
		int n = 0;
		for (BattleCard c : hand) {
			if (c == null || c.getCardId() != GameConstants.MIRACLE_TOKEN_CARD_ID) {
				continue;
			}
			String iid = c.getInstanceId();
			if (iid != null && ex.contains(iid)) {
				continue;
			}
			n++;
		}
		return n;
	}

	/**
	 * ミカエル〈配置〉: 手札に「奇跡」が3枚以上あるとき、手札の「奇跡」をすべてレストへ置き、
	 * ミカエルデッキ6種からランダム2枚をデッキの上に置く（〈配置〉コスト支払い中の奇跡は対象外）。
	 */
	private void applyMikaelMiracleDeckTransformOnDeploy(CpuBattleState st, boolean ownerHuman, Map<Short, CardDefinition> defs) {
		if (st == null || defs == null) {
			return;
		}
		short[] mikaelPool = {
				GameConstants.MIKAEL_WRATH_CARD_ID,
				GameConstants.MIKAEL_PUNCH_CARD_ID,
				GameConstants.MIKAEL_STRATEGY_CARD_ID,
				GameConstants.MIKAEL_MINION_A_CARD_ID,
				GameConstants.MIKAEL_MINION_B_CARD_ID,
				GameConstants.MIKAEL_FLASH_CARD_ID
		};
		for (short id : mikaelPool) {
			if (defs.get(id) == null) {
				st.addLog("ミカエル: ミカエルデッキ用の定義がないため効果はなかった");
				return;
			}
		}
		if (defs.get(GameConstants.MIRACLE_TOKEN_CARD_ID) == null) {
			return;
		}
		Set<String> costMiracleIds = new HashSet<>();
		ZoneFighter zf = ownerHuman ? st.getHumanBattle() : st.getCpuBattle();
		addMiracleInstanceIdsInDeployCostPaymentSlice(zf, costMiracleIds);

		List<BattleCard> rest = ownerHuman ? st.getHumanRest() : st.getCpuRest();
		List<BattleCard> hand = ownerHuman ? st.getHumanHand() : st.getCpuHand();
		List<BattleCard> deck = ownerHuman ? st.getHumanDeck() : st.getCpuDeck();
		if (rest == null || hand == null || deck == null) {
			return;
		}
		if (countMiracleTokensInHandExcluding(hand, costMiracleIds) < MIKAEL_DEPLOY_HAND_MIRACLE_MIN) {
			st.addLog("ミカエル: 手札に「奇跡」が3枚未満のため効果はなかった");
			return;
		}
		List<BattleCard> toRest = new ArrayList<>();
		pullMiracleTokensFromZoneExcluding(hand, toRest, costMiracleIds, zf, false);
		for (BattleCard c : toRest) {
			rest.add(c);
		}
		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		for (int i = 0; i < MIKAEL_DEPLOY_DECK_TOP_COUNT; i++) {
			short pick = mikaelPool[rnd.nextInt(mikaelPool.length)];
			deck.add(0, new BattleCard(UUID.randomUUID().toString(), pick));
		}
		if (ownerHuman) {
			st.addLog("ミカエル: 手札の「奇跡」" + toRest.size() + "枚をレストへ置き、ミカエルデッキから"
					+ MIKAEL_DEPLOY_DECK_TOP_COUNT + "枚をデッキの上に置いた");
		} else {
			st.addLog("ミカエル: 手札の「奇跡」" + toRest.size() + "枚をレストへ置き、" + cpuSlotActorLogLabel(st)
					+ "のミカエルデッキから" + MIKAEL_DEPLOY_DECK_TOP_COUNT + "枚をデッキの上に置いた");
		}
	}

	/** ミカエルの一閃: 場の〈フィールド〉を配置者のレストへ（定義がない場合は何もしない）。 */
	private void moveActiveFieldToDeployersRestForMikaelFlash(CpuBattleState st, boolean deployerHuman,
			Map<Short, CardDefinition> defs) {
		if (st == null) {
			return;
		}
		BattleCard old = st.getActiveField();
		if (old == null) {
			st.addLog("ミカエルの一閃: 場に〈フィールド〉がない");
			return;
		}
		CardDefinition oldDef = defs != null ? defs.get(old.getCardId()) : null;
		if (oldDef == null || !isFieldCard(oldDef)) {
			st.addLog("ミカエルの一閃: 場に〈フィールド〉がない");
			return;
		}
		boolean oldWasSkya = old.getCardId() == MYSTERIOUS_TREE_SKYAR_FIELD_ID;
		boolean oldWasDeathbounce = old.getCardId() == DEATHBOUNCE_FIELD_ID;
		List<BattleCard> targetRest = deployerHuman ? st.getHumanRest() : st.getCpuRest();
		if (targetRest == null) {
			return;
		}
		String nm = oldDef.getName() != null ? oldDef.getName() : "？";
		st.setActiveField(null);
		st.setActiveFieldOwnerHuman(null);
		st.setScrapyardFieldTurnsRemaining(0);
		st.setDeathbounceFieldTurnsRemaining(0);
		st.setAtlantisFieldCounterDisplay(0);
		st.setAtlantisAwaitingCount0(false);
		st.setWorldRebuildFieldCounterDisplay(0);
		st.setPaperCityFieldCounterDisplay(0);
		clearWeeklyShonenCampFieldTracking(st);
		st.setChojuGigaPendingHumanSlotNextDeployDragon(false);
		st.setChojuGigaPendingCpuSlotNextDeployHuman(false);
		targetRest.add(old);
		st.addLog("ミカエルの一閃: 〈フィールド〉「" + nm + "」をレストに置いた");
		if (defs != null) {
			if (oldWasSkya) {
				stripSkyaPersistedElfDeployBonusesOnFieldLoss(st, defs);
			}
			if (oldWasDeathbounce) {
				stripDeathbouncePersistedHandPenalties(st);
			}
		}
	}

	private void applyDeployHuman(CpuBattleState st, CardDefinition d, Map<Short, CardDefinition> defs,
			BattleCard deployedMain) {
		deployedMain = st.getHumanBattle() != null ? st.getHumanBattle().getMain() : null;
		applySpec666UndeadToDeployedFighterIfPending(st, true, defs);
		if (st != null && deployAbilitySuppressedByOpponentLine(st, true, d)) {
			return;
		}
		if (st != null && hasMikaelsWrathOnZone(st.getCpuBattle())) {
			return;
		}
		deployedMain = st.getHumanBattle() != null ? st.getHumanBattle().getMain() : null;
		if (deployedMain != null && deployedMain.isBlankEffects()) {
			return;
		}
		CardDefinition fighterForKamui = deployedMain != null ? defs.get(deployedMain.getCardId()) : null;
		if (fieldKamuiSuppressesDeployEffects(st, fighterForKamui)) {
			return;
		}
		String code = d != null ? d.getAbilityDeployCode() : null;
		if (code == null || code.isBlank()) {
			if (d != null && isCrystakulCardDefinition(d)) {
				code = "CRYSTAKUL";
			} else if (d != null && isBotBikeCardDefinition(d)) {
				code = "BOT_BIKE";
			} else if (d != null && isSpec1CardDefinition(d)) {
				code = "SPEC1";
			} else if (d != null && isHarpPlayerCardDefinition(d)) {
				code = "HARP_PLAYER";
			} else if (d != null && isGravePriestCardDefinition(d)) {
				code = "GRAVE_PRIEST";
			} else if (d != null && isBelieverCardDefinition(d)) {
				code = "BELIEVER";
			} else if (d != null && isHalfElfCardDefinition(d)) {
				code = "HALF_ELF";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.KRAKEN_FIGHTER_CARD_ID) {
				code = "KRAKEN";
			} else if (d != null && d.getId() != null && d.getId() == RAMIEL_ID) {
				code = RAMIEL_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.SIREN_FIGHTER_CARD_ID) {
				code = "SIREN";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.POSEIDON_FIGHTER_CARD_ID) {
				code = "POSEIDON";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.MERMAID_FIGHTER_CARD_ID) {
				code = "MERMAID";
			} else if (d != null && d.getId() != null && d.getId() == KUSURI_ID) {
				code = "KUSURI";
			} else if (d != null && isResearcherFloraCardDefinition(d)) {
				code = "RESEARCHER_FLORA";
			} else if (d != null && isMangakaCardDefinition(d)) {
				code = "MANGAKA";
			} else if (d != null && isComicDinosaurCardDefinition(d)) {
				code = "COMIC_DINOSAUR";
			} else if (d != null && isBehemothCardDefinition(d)) {
				code = "BEHEMOTH";
			} else if (d != null && isFafnirCardDefinition(d)) {
				code = "FAFNIR";
			} else if (d != null && isBahamutCardDefinition(d)) {
				code = "BAHAMUT";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.PAGE_WALKER_FIGHTER_CARD_ID) {
				code = "PAGE_WALKER";
			} else if (d != null && d.getId() != null && d.getId() == KING_MAKER_ID) {
				code = "KING_MAKER";
			} else if (d != null && d.getId() != null && d.getId() == DOMINION_ID) {
				code = DOMINION_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == MINION_SOLDIER_ID) {
				code = MINION_SOLDIER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == INK_KING_ID) {
				code = "INK_KING";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.SKETCHER_FIGHTER_CARD_ID) {
				code = SKETCHER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.ZADKIEL_FIGHTER_CARD_ID) {
				code = ZADKIEL_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.LUCIFER_FIGHTER_CARD_ID) {
				code = LUCIFER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID) {
				code = FALLEN_ANGEL_LUCIFER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.CELESTIA_FIGHTER_CARD_ID) {
				code = CELESTIA_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.VIRTUAL_FIGHTER_CARD_ID) {
				code = VIRTUAL_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.SERAPHIM_FIGHTER_CARD_ID) {
				code = SERAPHIM_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.COMIC_WITCH_FIGHTER_CARD_ID) {
				code = COMIC_WITCH_DEPLOY_CODE;
			} else {
				return;
			}
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
			case "KUSURI" -> {
				ZoneFighter zb = st.getHumanBattle();
				if (zb != null && zb.getMain() != null && zb.getMain().getCardId() == KUSURI_ID) {
					int n = st.getHumanStones();
					zb.setKusuriOpponentDebuffFromDeployStones(n);
					st.addLog("薬売り: 配置時の所持ストーン" + n + "のぶん、相手ファイターの強さ−" + n);
				}
			}
			case "KAGAKUSHA" -> {
				if (st.getHumanBattle() != null && st.getCpuBattle() != null) {
					st.setPowerSwapActive(true);
					st.addLog("科学者: 強さを入れ替えた");
				}
			}
			case "OKAMI_OTOKO" -> {
				if (st.getHumanBattle() != null) {
					swapMainWithWolfIfPaid(st.getHumanBattle(), st);
				}
			}
			case "MIKO" -> {
				// エルフの巫女: ストーン消費なしで、次回配置+1（任意選択なし）
				st.setHumanNextDeployBonus(st.getHumanNextDeployBonus() + 1);
				st.addLog("エルフの巫女: 次の配置+1");
			}
			case "MIRACLE" -> {
				st.setHumanStones(st.getHumanStones() + 1);
				st.addLog("奇跡: ストーンを1つ得た");
			}
			case "MIKAEL" -> applyMikaelMiracleDeckTransformOnDeploy(st, true, defs);
			case "MIKAEL_WRATH" -> st.addLog("ミカエルの怒りを配置した");
			case "MIKAEL_PUNCH" -> {
				ZoneFighter zf = st.getHumanBattle();
				if (zf != null) {
					zf.setZadkielOpponentTurnPowerBonus(3);
					st.addLog("ミカエルパンチ: 相手ターンの間、強さ+3");
				}
			}
			case "MIKAEL_STRATEGY" -> applyMikaelStrategyDeployEffect(st, true, false, defs);
			case "MIKAEL_MINION_A" -> {
				ZoneFighter zf = st.getHumanBattle();
				if (zf != null && zf.getMain() != null
						&& restContainsMikaelMinionExcludingTucked(zf, st.getHumanRest(),
								GameConstants.MIKAEL_MINION_B_CARD_ID)) {
					zf.getMain().setBattleEndPowerBonus(zf.getMain().getBattleEndPowerBonus() + 2);
					st.addLog("ミカエルの使いA: 強さ+2");
				}
			}
			case "MIKAEL_MINION_B" -> {
				ZoneFighter zf = st.getHumanBattle();
				if (zf != null
						&& restContainsMikaelMinionExcludingTucked(zf, st.getHumanRest(),
								GameConstants.MIKAEL_MINION_A_CARD_ID)) {
					zf.setZadkielOpponentTurnPowerBonus(zf.getZadkielOpponentTurnPowerBonus() + 1);
					st.addLog("ミカエルの使いB: 相手ターンの間、強さ+1");
				}
			}
			case "MIKAEL_FLASH" -> moveActiveFieldToDeployersRestForMikaelFlash(st, true, defs);
			case "FALLEN_ANGEL_LUCIFER" -> applyFallenAngelLuciferDeployEffect(st, true, false, defs);
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
			case "SEASERPENT" -> {
				if (st.getHumanStones() >= 1 && defs.get(GameConstants.SWORDFISH_TOKEN_CARD_ID) != null) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"シーサーペント",
							true,
							"SEASERPENT",
							1,
							List.of()
					));
				}
			}
			case "CELESTIA" -> {
				if (st.getHumanStones() >= 1 && canGrantMiracleSlotCard(st, true, defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"セレスティア（ストーン1・「奇跡」を2枚手札に）",
							true,
							CELESTIA_DEPLOY_CODE,
							1,
							List.of()
					));
				} else if (st.getHumanStones() < 1) {
					st.addLog("セレスティア: ストーンがないため効果はなかった");
				} else {
					st.addLog("セレスティア: 「奇跡」の定義がない");
				}
			}
			case "VIRTUAL" -> {
				st.setHumanStones(st.getHumanStones() + 2);
				st.addLog("ヴァーチャー: ストーンを2つ得た");
				if (canGrantMiracleSlotCard(st, true, defs)) {
					addMiracleCopiesToHandForPlayer(st.getHumanHand(), 1, st, true, defs);
					String nm = defs.get(miracleGrantCardId(st, true, defs)).getName();
					st.addLog("ヴァーチャー: 「" + (nm != null ? nm : "？") + "」を1枚手札に加えた");
				} else {
					st.addLog("ヴァーチャー: 「奇跡」の定義がない");
				}
			}
			case "RESEARCHER_FLORA" -> {
				if (st.getHumanStones() >= 1
						&& restContainsPickableElfForResearcherFlora(st, st.getHumanRest(), st.getHumanBattle(), defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"研究者フローラ（ストーン1・レストの「種族：エルフ」を1枚手札へ）",
							true,
							"RESEARCHER_FLORA",
							1,
							List.of()));
				}
			}
			case "MANGAKA" -> applyMangakaDeployEffect(st, true, false, defs, null);
			case "COMIC_DINOSAUR" -> beginComicDinosaurDeployHandChoice(st, true, false, defs);
			case "ZADKIEL" -> {
				List<String> mirOpts = new ArrayList<>();
				for (BattleCard hc : st.getHumanHand()) {
					if (hc != null && hc.getInstanceId() != null
							&& hc.getCardId() == GameConstants.MIRACLE_TOKEN_CARD_ID) {
						mirOpts.add(hc.getInstanceId());
					}
				}
				if (!mirOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ザドキエル（「奇跡」を1枚レストへ置いてもよい。置いたなら次に配置するファイターは相手ターン中強さ+3）",
							true,
							ZADKIEL_DEPLOY_CODE,
							0,
							List.of()));
				} else {
					st.addLog("ザドキエル: 手札に「奇跡」がない");
				}
			}
			case "SERAPHIM" -> {
				List<String> mirSerOpts = new ArrayList<>();
				for (BattleCard hc : st.getHumanHand()) {
					if (hc != null && hc.getInstanceId() != null
							&& hc.getCardId() == GameConstants.MIRACLE_TOKEN_CARD_ID) {
						mirSerOpts.add(hc.getInstanceId());
					}
				}
				if (!mirSerOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"セラフィム（「奇跡」を1枚レストへ置いてもよい。置いたなら、自分のレストの「種族：エンジェル」を1枚手札に）",
							true,
							SERAPHIM_DEPLOY_CODE,
							0,
							List.of()));
				} else {
					st.addLog("セラフィム: 手札に「奇跡」がない");
				}
			}
			case "COMIC_WITCH" -> {
				if (defs.get(INK_KNIGHT_ID) == null) {
					break;
				}
				List<String> cwRest = comicWitchPickableRestInstanceIds(st, true);
				if (st.getHumanStones() >= 1 && !cwRest.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"コミックウィッチ（ストーン1・レストから最大2枚を「インクナイト」に）",
							true,
							COMIC_WITCH_DEPLOY_CODE,
							1,
							List.of()));
				} else if (st.getHumanStones() < 1) {
					st.addLog("コミックウィッチ: ストーンがないため効果はなかった");
				} else {
					st.addLog("コミックウィッチ: レストに対象のカードがないため効果はなかった");
				}
			}
			case "MERMAID" -> applyMermaidDeployEffect(st, st.getHumanHand(), true, false, defs);
			case "SIREN" -> applySirenDeployEffect(st, true, false, defs);
			case "POSEIDON" -> applyPoseidonDeployEffect(st, true, false, defs);
			case "KRAKEN" -> {
				st.setHumanKrakenNextTurnSwordfishAdds(st.getHumanKrakenNextTurnSwordfishAdds() + 1);
				st.addLog("クラーケン: 次の自分のターンの開始時に「ソードフィッシュ」を1枚加える");
			}
			case "RAMIEL" -> {
				if (st.getHumanStones() >= 1 && canGrantMiracleSlotCard(st, true, defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ラミエル（ストーン1・次の自分のターン開始時に「奇跡」1枚）",
							true,
							RAMIEL_DEPLOY_CODE,
							1,
							List.of()
					));
				} else if (st.getHumanStones() < 1) {
					st.addLog("ラミエル: ストーンがないため効果はなかった");
				} else {
					st.addLog("ラミエル: 「奇跡」の定義がない");
				}
			}
			case "LUCIFER" -> applyLuciferDeployEffect(st, true, false, defs);
			case "LEVIATHAN" -> {
				List<String> levOpts = leviathanDragonMerfolkRestOptionIds(st, true, defs);
				if (!levOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_UP_TO_TWO_FROM_REST_TO_HAND,
							"リヴァイアサン（レストのドラゴン／マーフォークを2枚まで）",
							true,
							"LEVIATHAN",
							0,
							levOpts));
				} else {
					st.addLog("リヴァイアサン: レストにドラゴン／マーフォークのファイターがいない");
					advanceActiveFieldCountForLeviathan(st, 2, defs);
				}
			}
			case "PAGE_WALKER" -> {
				st.setHumanStones(st.getHumanStones() + 1);
				st.addLog("ページウォーカー: ストーン+1");
				advanceActiveFieldCountForLeviathan(st, 2, defs);
			}
			case "KING_MAKER" -> applyKingMakerDeployEffect(st, true, false, defs);
			case "DOMINION" -> applyDominionDeployEffect(st, true, false, defs);
			case "MINION_SOLDIER" -> applyMinionSoldierDeployEffect(st, true, false, defs);
			case "INK_KING" -> applyInkKingDeployEffect(st, true, false, defs);
			case "SKETCHER" -> applySketcherDeployEffect(st, true, false, defs, null);
			case "HARP_PLAYER" -> {
				st.setHumanNextElfOnlyBonus(st.getHumanNextElfOnlyBonus() + HARP_NEXT_ELF_POWER_BONUS);
				st.addLog("森のハープ弾き: 次に配置するエルフはターン終了まで強さ+" + HARP_NEXT_ELF_POWER_BONUS);
			}
			case "GRAVE_PRIEST" -> {
				List<String> gpOpts = gravePriestUndeadHandOptionIds(st.getHumanHand(), defs, st, true);
				if (!gpOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_UNDEAD_FIGHTER_FROM_HAND_FOR_COST,
							"墓守神父（手札の「墓守神父」以外の「種族：アンデッド」のファイターを1枚）",
							true,
							"GRAVE_PRIEST",
							0,
							gpOpts));
				} else {
					st.addLog("墓守神父: 手札に対象のファイターがいない");
				}
			}
			case "BELIEVER" -> {
				int dbN = moveAllDeathbounceFromRestToHand(st.getHumanRest(), st.getHumanHand());
				if (dbN > 0) {
					st.addLog("信奉者: 霊園教会 デスバウンスを" + dbN + "枚手札へ");
				} else {
					st.addLog("信奉者: レストに「霊園教会 デスバウンス」がない");
				}
			}
			case "BEHEMOTH" -> applyBehemothDeployStoneGain(st, st.getHumanBattle(), true);
			case "FAFNIR" -> applyFafnirDeployEffect(st, true, false, defs);
			case "BAHAMUT" -> applyBahamutDeployEffect(st, true, false, defs);
			case "HALF_ELF" -> {
				ZoneFighter hzHe = st.getHumanBattle();
				if (hzHe != null) {
					int heb = halfElfLineagePowerBonusFromRest(st, st.getHumanRest(), defs);
					if (heb > 0) {
						hzHe.setTemporaryPowerBonus(hzHe.getTemporaryPowerBonus() + heb);
						st.addLog("ハーフエルフ: 強さ+" + heb);
					}
				}
			}
			case "SHOKIN" -> {
				// 隊長: 次の配置はコストぶん強化（重ねがけ可）
				st.setHumanNextDeployCostBonusTimes(st.getHumanNextDeployCostBonusTimes() + 1);
				st.addLog("隊長: 次の配置はコストぶん強化");
			}
			case "MECHANIC" -> {
				st.setHumanNextMechanicStacks(st.getHumanNextMechanicStacks() + 1);
				st.addLog("メカニック: 次の配置はマシン化（バトル終了まで）＋ターン終了までコスト+1、強さ+3");
			}
			case "BOT_BIKE" -> {
				ZoneFighter zb = st.getHumanBattle();
				if (zb != null && zb.getMain() != null && zb.getMain().getCardId() == BOT_BIKE_ID
						&& zoneCostIncludesMechanicFighter(zb, defs)) {
					zb.setBotBikeMechanicPowerBonus(3);
					st.addLog("ボットバイク: 次の相手ターン終了まで強さ+3");
				}
			}
			case "KINOKO" -> {
				List<String> pixieOpts = new ArrayList<>();
				for (BattleCard c : st.getHumanRest()) {
					if (restCardHasTribe(st, defs.get(c.getCardId()), c, "ELF")) {
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
					if (isHumanFighterInRestSlot(st, defs.get(c.getCardId()), c)) {
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
			case "ARTHUR" -> {
				List<String> arthurOpts = arthurKamuiHumanFighterRestOptionIds(st, true, defs);
				if (!arthurOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"アーサー（レストの人間ファイターを1枚選択）",
							true,
							"ARTHUR",
							0,
							arthurOpts
					));
				} else if (st.getActiveField() != null && st.getActiveField().getCardId() == FIELD_KAMUI_ID) {
					st.addLog("アーサー: レストに「種族：人間」のファイターがない");
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
							"助手（「研究者」を含むカード1枚・手札へ、バトル終了まで強さ+2）",
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
				List<String> ryuOpts = new ArrayList<>();
				ZoneFighter ownBattleRy = st.getHumanBattle();
				for (BattleCard c : st.getHumanRest()) {
					if (isTuckedUnderOwnFighter(ownBattleRy, c)) {
						continue;
					}
					if (restCardHasTribe(st, defs.get(c.getCardId()), c, "DRAGON")) {
						ryuOpts.add(c.getInstanceId());
					}
				}
				if (!ryuOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"ドラゴンの卵（レストのドラゴンを選択）",
							true,
							"RYUNOTAMAGO",
							0,
							ryuOpts
					));
				}
			}
			case "KORYU" -> {
				int elves = countAttributeInRest(st, st.getHumanRest(), defs, "ELF");
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
				// お互い手札から1枚ずつ選んでレストへ（片側のみならその側だけ。相手は後続の保留または CPU の簡易選択）
				boolean hadHumanHand = !st.getHumanHand().isEmpty();
				boolean hadCpuHand = !st.getCpuHand().isEmpty();
				if (hadHumanHand) {
					List<String> opts = new ArrayList<>();
					for (BattleCard c : st.getHumanHand()) opts.add(c.getInstanceId());
					String ability = hadCpuHand ? KENTOSHI_PAIR_FIRST_DEPLOY_CODE : KENTOSHI_SOLO_DEPLOY_CODE;
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
							"剣闘士（捨てるカードを選択）",
							true,
							ability,
							0,
							opts
					));
				} else if (hadCpuHand) {
					if (st.isPvp()) {
						List<String> opts = new ArrayList<>();
						for (BattleCard c : st.getCpuHand()) opts.add(c.getInstanceId());
						st.setPendingChoice(new PendingChoice(
								ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
								"剣闘士（捨てるカードを選択）",
								false,
								KENTOSHI_SOLO_DEPLOY_CODE,
								0,
								opts,
								true));
					} else {
						kentoshiAiDiscardOneFromHand(st.getCpuHand(), st.getCpuRest(), defs);
					}
				}
				if (hadHumanHand && hadCpuHand) {
					st.addLog("剣闘士: お互い手札を1枚レストへ");
				} else if (hadHumanHand) {
					st.addLog("剣闘士: 自分の手札を1枚レストへ（相手は手札なし）");
				} else if (hadCpuHand) {
					st.addLog("剣闘士: 相手の手札を1枚レストへ（自分は手札なし）");
				}
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
					moveZoneToRest(st.getCpuBattle(), st.getCpuRest(), st, st.getCpuHand(), defs);
					st.setCpuBattle(null);
					st.addLog("火炎竜: 相手ファイターをレストへ");
				}
			}
			case "DAKU_DORAGON" -> {
				st.setHumanStones(st.getHumanStones() + 2);
				st.addLog("ダークドラゴン: ストーン+2");
				if (st.getCpuBattle() != null && st.getCpuBattle().getMain() != null) {
					BattleCard om = st.getCpuBattle().getMain();
					if (CardAttributes.hasAttribute(defs.get(om.getCardId()), om, "DRAGON")) {
						moveZoneToRest(st.getCpuBattle(), st.getCpuRest(), st, st.getCpuHand(), defs);
						st.setCpuBattle(null);
						st.addLog("ダークドラゴン: 相手ドラゴンをレストへ");
					}
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
				st.addLog("ストーニア: ストーン+1");
			}
			case "CRYSTAKUL" -> {
				PendingEffect cpe = st.getPendingEffect();
				if (cpe != null && cpe.isCrystakulOptionalResolved()) {
					break;
				}
				if (st.getHumanStones() >= CRYSTAKUL_OPTIONAL_STONE_COST) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"クリスタクル（ストーン2・次の配置+3／次の相手ターン終了まで）",
							true,
							"CRYSTAKUL",
							CRYSTAKUL_OPTIONAL_STONE_COST,
							List.of()
					));
				}
			}
			case "MIRAJUKUL" -> offerMirajukulMirrorConfirmation(st, true, false, defs);
			case "STONIA" -> {
				if (st.getHumanBattle() != null) {
					int s = st.getHumanStones();
					st.addLog("ノクスクル: 自分のターンの終わりまで、所持ストーン" + s + "のぶん強さ+" + s);
				}
			}
			case "LUMINANK" -> {
				int n = st.getHumanStones();
				st.setHumanStones(n * 2);
				st.addLog("ルミナンク: ストーンを" + n + "から" + (n * 2) + "に");
			}
			case "FEZARIA" -> {
				if (st.getHumanStones() >= FEZARIA_OPTIONAL_STONE_COST
						&& restContainsFezariaPickableCarbuncle(st, st.getHumanRest(), st.getHumanBattle(), defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"フェザリア（ストーン3・フェザリア以外のカーバンクルを回収）",
							true,
							"FEZARIA",
							FEZARIA_OPTIONAL_STONE_COST,
							List.of()
					));
				}
			}
			case "MACHINE_GUNNER" -> machineGunDiscardOpponentStones(st, d, defs, true, false);
			case "SPEC777" -> {
				ZoneFighter z777 = st.getHumanBattle();
				if (z777 != null && z777.getMain() != null && z777.getMain().getCardId() == SPEC_777_ID) {
					int roll = 2 + ThreadLocalRandom.current().nextInt(6);
					z777.setSpec777RolledPower(roll);
					st.addLog("SPEC-777: 出目=" + roll);
					applySpec777DeployLossIfRollBelowOpponentAtDeploy(st, defs, true);
				}
			}
			case "SPEC666" -> {
				st.setSpec666NextHumanUndead(true);
				st.setSpec666NextCpuUndead(true);
				st.addLog("SPEC-666: 次に双方が配置するファイターはアンデッド扱い");
			}
			case "SPEC123" -> {
				ZoneFighter z123 = st.getHumanBattle();
				if (z123 != null && z123.getMain() != null && z123.getMain().getCardId() == SPEC_123_ID) {
					int gain = 1 + ThreadLocalRandom.current().nextInt(3);
					st.setHumanStones(st.getHumanStones() + gain);
					st.addLog("SPEC-123: ストーン+" + gain);
				}
			}
			case "SPEC0", "SPEC1" -> {
				ZoneFighter z0 = st.getHumanBattle();
				if (z0 == null || z0.getMain() == null || z0.getMain().getCardId() != SPEC_1_ID) {
					break;
				}
				List<String> spec1Opts = new ArrayList<>();
				for (BattleCard c : st.getHumanRest()) {
					if (isSpec1EligibleRestFighter(c, z0, defs)) {
						spec1Opts.add(c.getInstanceId());
					}
				}
				if (!spec1Opts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_DECK_TOP,
							"SPEC-1（「強さ1」のファイターを1枚、デッキ上へ）",
							true,
							"SPEC1",
							0,
							spec1Opts));
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
		deployedMain = st.getCpuBattle() != null ? st.getCpuBattle().getMain() : null;
		applySpec666UndeadToDeployedFighterIfPending(st, false, defs);
		if (st != null && deployAbilitySuppressedByOpponentLine(st, false, d)) {
			return;
		}
		if (st != null && hasMikaelsWrathOnZone(st.getHumanBattle())) {
			return;
		}
		deployedMain = st.getCpuBattle() != null ? st.getCpuBattle().getMain() : null;
		if (deployedMain != null && deployedMain.isBlankEffects()) {
			return;
		}
		CardDefinition fighterForKamuiGuest = deployedMain != null ? defs.get(deployedMain.getCardId()) : null;
		if (fieldKamuiSuppressesDeployEffects(st, fighterForKamuiGuest)) {
			return;
		}
		String code = d != null ? d.getAbilityDeployCode() : null;
		if (code == null || code.isBlank()) {
			if (d != null && isCrystakulCardDefinition(d)) {
				code = "CRYSTAKUL";
			} else if (d != null && isBotBikeCardDefinition(d)) {
				code = "BOT_BIKE";
			} else if (d != null && isSpec1CardDefinition(d)) {
				code = "SPEC1";
			} else if (d != null && isHarpPlayerCardDefinition(d)) {
				code = "HARP_PLAYER";
			} else if (d != null && isGravePriestCardDefinition(d)) {
				code = "GRAVE_PRIEST";
			} else if (d != null && isBelieverCardDefinition(d)) {
				code = "BELIEVER";
			} else if (d != null && isHalfElfCardDefinition(d)) {
				code = "HALF_ELF";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.KRAKEN_FIGHTER_CARD_ID) {
				code = "KRAKEN";
			} else if (d != null && d.getId() != null && d.getId() == RAMIEL_ID) {
				code = RAMIEL_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.SIREN_FIGHTER_CARD_ID) {
				code = "SIREN";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.POSEIDON_FIGHTER_CARD_ID) {
				code = "POSEIDON";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.MERMAID_FIGHTER_CARD_ID) {
				code = "MERMAID";
			} else if (d != null && d.getId() != null && d.getId() == KUSURI_ID) {
				code = "KUSURI";
			} else if (d != null && isResearcherFloraCardDefinition(d)) {
				code = "RESEARCHER_FLORA";
			} else if (d != null && isMangakaCardDefinition(d)) {
				code = "MANGAKA";
			} else if (d != null && isComicDinosaurCardDefinition(d)) {
				code = "COMIC_DINOSAUR";
			} else if (d != null && isBehemothCardDefinition(d)) {
				code = "BEHEMOTH";
			} else if (d != null && isFafnirCardDefinition(d)) {
				code = "FAFNIR";
			} else if (d != null && isBahamutCardDefinition(d)) {
				code = "BAHAMUT";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.PAGE_WALKER_FIGHTER_CARD_ID) {
				code = "PAGE_WALKER";
			} else if (d != null && d.getId() != null && d.getId() == KING_MAKER_ID) {
				code = "KING_MAKER";
			} else if (d != null && d.getId() != null && d.getId() == DOMINION_ID) {
				code = DOMINION_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == MINION_SOLDIER_ID) {
				code = MINION_SOLDIER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == INK_KING_ID) {
				code = "INK_KING";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.SKETCHER_FIGHTER_CARD_ID) {
				code = SKETCHER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.ZADKIEL_FIGHTER_CARD_ID) {
				code = ZADKIEL_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.LUCIFER_FIGHTER_CARD_ID) {
				code = LUCIFER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID) {
				code = FALLEN_ANGEL_LUCIFER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.CELESTIA_FIGHTER_CARD_ID) {
				code = CELESTIA_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.VIRTUAL_FIGHTER_CARD_ID) {
				code = VIRTUAL_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.SERAPHIM_FIGHTER_CARD_ID) {
				code = SERAPHIM_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.COMIC_WITCH_FIGHTER_CARD_ID) {
				code = COMIC_WITCH_DEPLOY_CODE;
			} else {
				return;
			}
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
			case "KUSURI" -> {
				ZoneFighter zb = st.getCpuBattle();
				if (zb != null && zb.getMain() != null && zb.getMain().getCardId() == KUSURI_ID) {
					int n = st.getCpuStones();
					zb.setKusuriOpponentDebuffFromDeployStones(n);
					st.addLog("薬売り: 配置時の所持ストーン" + n + "のぶん、相手ファイターの強さ−" + n);
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
					swapMainWithWolfIfPaid(st.getCpuBattle(), st);
				}
			}
			case "MIKO" -> {
				st.setCpuNextDeployBonus(st.getCpuNextDeployBonus() + 1);
				st.addLog("エルフの巫女: 次の配置+1");
			}
			case "MIRACLE" -> {
				st.setCpuStones(st.getCpuStones() + 1);
				st.addLog("奇跡: ストーンを1つ得た");
			}
			case "MIKAEL" -> applyMikaelMiracleDeckTransformOnDeploy(st, false, defs);
			case "MIKAEL_WRATH" -> st.addLog("ミカエルの怒りを配置した");
			case "MIKAEL_PUNCH" -> {
				ZoneFighter zf = st.getCpuBattle();
				if (zf != null) {
					zf.setZadkielOpponentTurnPowerBonus(3);
					st.addLog("ミカエルパンチ: 相手ターンの間、強さ+3");
				}
			}
			case "MIKAEL_STRATEGY" -> applyMikaelStrategyDeployEffect(st, false, false, defs);
			case "MIKAEL_MINION_A" -> {
				ZoneFighter zf = st.getCpuBattle();
				if (zf != null && zf.getMain() != null
						&& restContainsMikaelMinionExcludingTucked(zf, st.getCpuRest(),
								GameConstants.MIKAEL_MINION_B_CARD_ID)) {
					zf.getMain().setBattleEndPowerBonus(zf.getMain().getBattleEndPowerBonus() + 2);
					st.addLog("ミカエルの使いA: 強さ+2");
				}
			}
			case "MIKAEL_MINION_B" -> {
				ZoneFighter zf = st.getCpuBattle();
				if (zf != null
						&& restContainsMikaelMinionExcludingTucked(zf, st.getCpuRest(),
								GameConstants.MIKAEL_MINION_A_CARD_ID)) {
					zf.setZadkielOpponentTurnPowerBonus(zf.getZadkielOpponentTurnPowerBonus() + 1);
					st.addLog("ミカエルの使いB: 相手ターンの間、強さ+1");
				}
			}
			case "MIKAEL_FLASH" -> moveActiveFieldToDeployersRestForMikaelFlash(st, false, defs);
			case "FALLEN_ANGEL_LUCIFER" -> applyFallenAngelLuciferDeployEffect(st, false, false, defs);
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
			case "SEASERPENT" -> {
				if (st.getCpuStones() >= 1 && defs.get(GameConstants.SWORDFISH_TOKEN_CARD_ID) != null) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"シーサーペント",
							false,
							"SEASERPENT",
							1,
							List.of(),
							true
					));
				}
			}
			case "CELESTIA" -> {
				if (st.getCpuStones() >= 1 && canGrantMiracleSlotCard(st, false, defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"セレスティア（ストーン1・「奇跡」を2枚手札に）",
							false,
							CELESTIA_DEPLOY_CODE,
							1,
							List.of(),
							true));
				} else if (st.getCpuStones() < 1) {
					st.addLog("セレスティア: ストーンがないため効果はなかった");
				} else {
					st.addLog("セレスティア: 「奇跡」の定義がない");
				}
			}
			case "VIRTUAL" -> {
				st.setCpuStones(st.getCpuStones() + 2);
				st.addLog("ヴァーチャー: ストーンを2つ得た");
				if (canGrantMiracleSlotCard(st, false, defs)) {
					addMiracleCopiesToHandForPlayer(st.getCpuHand(), 1, st, false, defs);
					String nm = defs.get(miracleGrantCardId(st, false, defs)).getName();
					st.addLog("ヴァーチャー: 「" + (nm != null ? nm : "？") + "」を1枚手札に加えた");
				} else {
					st.addLog("ヴァーチャー: 「奇跡」の定義がない");
				}
			}
			case "RESEARCHER_FLORA" -> {
				if (st.getCpuStones() >= 1
						&& restContainsPickableElfForResearcherFlora(st, st.getCpuRest(), st.getCpuBattle(), defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"研究者フローラ（ストーン1・レストの「種族：エルフ」を1枚手札へ）",
							false,
							"RESEARCHER_FLORA",
							1,
							List.of(),
							true));
				}
			}
			case "MANGAKA" -> applyMangakaDeployEffect(st, false, false, defs, null);
			case "COMIC_DINOSAUR" -> beginComicDinosaurDeployHandChoice(st, false, true, defs);
			case "ZADKIEL" -> {
				List<String> mirGuestOpts = new ArrayList<>();
				for (BattleCard hc : st.getCpuHand()) {
					if (hc != null && hc.getInstanceId() != null
							&& hc.getCardId() == GameConstants.MIRACLE_TOKEN_CARD_ID) {
						mirGuestOpts.add(hc.getInstanceId());
					}
				}
				if (!mirGuestOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ザドキエル（「奇跡」を1枚レストへ置いてもよい。置いたなら次に配置するファイターは相手ターン中強さ+3）",
							false,
							ZADKIEL_DEPLOY_CODE,
							0,
							List.of(),
							true));
				} else {
					st.addLog("ザドキエル: 手札に「奇跡」がない");
				}
			}
			case "SERAPHIM" -> {
				List<String> mirSerGuestOpts = new ArrayList<>();
				for (BattleCard hc : st.getCpuHand()) {
					if (hc != null && hc.getInstanceId() != null
							&& hc.getCardId() == GameConstants.MIRACLE_TOKEN_CARD_ID) {
						mirSerGuestOpts.add(hc.getInstanceId());
					}
				}
				if (!mirSerGuestOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"セラフィム（「奇跡」を1枚レストへ置いてもよい。置いたなら、自分のレストの「種族：エンジェル」を1枚手札に）",
							false,
							SERAPHIM_DEPLOY_CODE,
							0,
							List.of(),
							true));
				} else {
					st.addLog("セラフィム: 手札に「奇跡」がない");
				}
			}
			case "COMIC_WITCH" -> {
				if (defs.get(INK_KNIGHT_ID) == null) {
					break;
				}
				List<String> cwGuestRest = comicWitchPickableRestInstanceIds(st, false);
				if (st.getCpuStones() >= 1 && !cwGuestRest.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"コミックウィッチ（ストーン1・レストから最大2枚を「インクナイト」に）",
							false,
							COMIC_WITCH_DEPLOY_CODE,
							1,
							List.of(),
							true));
				} else if (st.getCpuStones() < 1) {
					st.addLog("コミックウィッチ: ストーンがないため効果はなかった");
				} else {
					st.addLog("コミックウィッチ: レストに対象のカードがないため効果はなかった");
				}
			}
			case "MERMAID" -> applyMermaidDeployEffect(st, st.getCpuHand(), false, false, defs);
			case "SIREN" -> applySirenDeployEffect(st, false, false, defs);
			case "POSEIDON" -> applyPoseidonDeployEffect(st, false, false, defs);
			case "KRAKEN" -> {
				st.setCpuKrakenNextTurnSwordfishAdds(st.getCpuKrakenNextTurnSwordfishAdds() + 1);
				st.addLog("クラーケン: 次の自分のターンの開始時に「ソードフィッシュ」を1枚加える");
			}
			case "RAMIEL" -> {
				if (st.getCpuStones() >= 1 && canGrantMiracleSlotCard(st, false, defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"ラミエル（ストーン1・次の自分のターン開始時に「奇跡」1枚）",
							false,
							RAMIEL_DEPLOY_CODE,
							1,
							List.of(),
							true
					));
				} else if (st.getCpuStones() < 1) {
					st.addLog("ラミエル: ストーンがないため効果はなかった");
				} else {
					st.addLog("ラミエル: 「奇跡」の定義がない");
				}
			}
			case "LUCIFER" -> applyLuciferDeployEffect(st, false, false, defs);
			case "LEVIATHAN" -> {
				List<String> levGuestOpts = leviathanDragonMerfolkRestOptionIds(st, false, defs);
				if (!levGuestOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_UP_TO_TWO_FROM_REST_TO_HAND,
							"リヴァイアサン（レストのドラゴン／マーフォークを2枚まで）",
							false,
							"LEVIATHAN",
							0,
							levGuestOpts,
							true));
				} else {
					st.addLog("リヴァイアサン: レストにドラゴン／マーフォークのファイターがいない");
					advanceActiveFieldCountForLeviathan(st, 2, defs);
				}
			}
			case "PAGE_WALKER" -> {
				st.setCpuStones(st.getCpuStones() + 1);
				st.addLog("ページウォーカー: ストーン+1");
				advanceActiveFieldCountForLeviathan(st, 2, defs);
			}
			case "KING_MAKER" -> applyKingMakerDeployEffect(st, false, false, defs);
			case "DOMINION" -> applyDominionDeployEffect(st, false, false, defs);
			case "MINION_SOLDIER" -> applyMinionSoldierDeployEffect(st, false, false, defs);
			case "INK_KING" -> applyInkKingDeployEffect(st, false, false, defs);
			case "SKETCHER" -> applySketcherDeployEffect(st, false, false, defs, null);
			case "HARP_PLAYER" -> {
				st.setCpuNextElfOnlyBonus(st.getCpuNextElfOnlyBonus() + HARP_NEXT_ELF_POWER_BONUS);
				st.addLog("森のハープ弾き: 次に配置するエルフはターン終了まで強さ+" + HARP_NEXT_ELF_POWER_BONUS);
			}
			case "GRAVE_PRIEST" -> {
				List<String> gpGuestOpts = gravePriestUndeadHandOptionIds(st.getCpuHand(), defs, st, false);
				if (!gpGuestOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_UNDEAD_FIGHTER_FROM_HAND_FOR_COST,
							"墓守神父（手札の「墓守神父」以外の「種族：アンデッド」のファイターを1枚）",
							false,
							"GRAVE_PRIEST",
							0,
							gpGuestOpts,
							true));
				} else {
					st.addLog("墓守神父: 手札に対象のファイターがいない");
				}
			}
			case "BELIEVER" -> {
				int dbNg = moveAllDeathbounceFromRestToHand(st.getCpuRest(), st.getCpuHand());
				if (dbNg > 0) {
					st.addLog("信奉者: 霊園教会 デスバウンスを" + dbNg + "枚手札へ");
				} else {
					st.addLog("信奉者: レストに「霊園教会 デスバウンス」がない");
				}
			}
			case "BEHEMOTH" -> applyBehemothDeployStoneGain(st, st.getCpuBattle(), false);
			case "FAFNIR" -> applyFafnirDeployEffect(st, false, false, defs);
			case "BAHAMUT" -> applyBahamutDeployEffect(st, false, false, defs);
			case "HALF_ELF" -> {
				ZoneFighter hzHeG = st.getCpuBattle();
				if (hzHeG != null) {
					int hebG = halfElfLineagePowerBonusFromRest(st, st.getCpuRest(), defs);
					if (hebG > 0) {
						hzHeG.setTemporaryPowerBonus(hzHeG.getTemporaryPowerBonus() + hebG);
						st.addLog("ハーフエルフ: 強さ+" + hebG);
					}
				}
			}
			case "SHOKIN" -> {
				st.setCpuNextDeployCostBonusTimes(st.getCpuNextDeployCostBonusTimes() + 1);
				st.addLog("隊長: 次の配置はコストぶん強化");
			}
			case "MECHANIC" -> {
				st.setCpuNextMechanicStacks(st.getCpuNextMechanicStacks() + 1);
				st.addLog("メカニック: 次の配置はマシン化（バトル終了まで）＋ターン終了までコスト+1、強さ+3");
			}
			case "BOT_BIKE" -> {
				ZoneFighter zb = st.getCpuBattle();
				if (zb != null && zb.getMain() != null && zb.getMain().getCardId() == BOT_BIKE_ID
						&& zoneCostIncludesMechanicFighter(zb, defs)) {
					zb.setBotBikeMechanicPowerBonus(3);
					st.addLog("ボットバイク: 次の相手ターン終了まで強さ+3");
				}
			}
			case "KINOKO" -> {
				List<String> pixieGuestOpts = new ArrayList<>();
				for (BattleCard c : st.getCpuRest()) {
					if (restCardHasTribe(st, defs.get(c.getCardId()), c, "ELF")) {
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
					if (isHumanFighterInRestSlot(st, defs.get(c.getCardId()), c)) {
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
			case "ARTHUR" -> {
				List<String> arthurGuestOpts = arthurKamuiHumanFighterRestOptionIds(st, false, defs);
				if (!arthurGuestOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"アーサー（レストの人間ファイターを1枚選択）",
							false,
							"ARTHUR",
							0,
							arthurGuestOpts,
							true
					));
				} else if (st.getActiveField() != null && st.getActiveField().getCardId() == FIELD_KAMUI_ID) {
					st.addLog("アーサー: レストに「種族：人間」のファイターがない");
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
							"助手（「研究者」を含むカード1枚・手札へ、バトル終了まで強さ+2）",
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
				List<String> ryuGuestOpts = new ArrayList<>();
				ZoneFighter ownBattleRyG = st.getCpuBattle();
				for (BattleCard c : st.getCpuRest()) {
					if (isTuckedUnderOwnFighter(ownBattleRyG, c)) {
						continue;
					}
					if (restCardHasTribe(st, defs.get(c.getCardId()), c, "DRAGON")) {
						ryuGuestOpts.add(c.getInstanceId());
					}
				}
				if (!ryuGuestOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_HAND,
							"ドラゴンの卵（レストのドラゴンを選択）",
							false,
							"RYUNOTAMAGO",
							0,
							ryuGuestOpts,
							true
					));
				}
			}
			case "KORYU" -> {
				int elves = countAttributeInRest(st, st.getCpuRest(), defs, "ELF");
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
				// ゲストが配置: 手札があればゲストから選び、相手（ホスト）がまだ手札があれば続けてホストが選ぶ
				boolean hadGuestHand = !st.getCpuHand().isEmpty();
				boolean hadHostHand = !st.getHumanHand().isEmpty();
				if (hadGuestHand) {
					List<String> opts = new ArrayList<>();
					for (BattleCard c : st.getCpuHand()) opts.add(c.getInstanceId());
					String ability = hadHostHand ? KENTOSHI_PAIR_FIRST_DEPLOY_CODE : KENTOSHI_SOLO_DEPLOY_CODE;
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
							"剣闘士（捨てるカードを選択）",
							false,
							ability,
							0,
							opts,
							true
					));
				} else if (hadHostHand) {
					List<String> opts = new ArrayList<>();
					for (BattleCard c : st.getHumanHand()) opts.add(c.getInstanceId());
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
							"剣闘士（捨てるカードを選択）",
							true,
							KENTOSHI_SOLO_DEPLOY_CODE,
							0,
							opts));
				}
				if (hadGuestHand && hadHostHand) {
					st.addLog("剣闘士: お互い手札を1枚レストへ");
				} else if (hadGuestHand) {
					st.addLog("剣闘士: 自分の手札を1枚レストへ（相手は手札なし）");
				} else if (hadHostHand) {
					st.addLog("剣闘士: 相手の手札を1枚レストへ（自分は手札なし）");
				}
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
					moveZoneToRest(st.getHumanBattle(), st.getHumanRest(), st, st.getHumanHand(), defs);
					st.setHumanBattle(null);
					st.addLog("火炎竜: 相手ファイターをレストへ");
				}
			}
			case "DAKU_DORAGON" -> {
				st.setCpuStones(st.getCpuStones() + 2);
				st.addLog("ダークドラゴン: ストーン+2");
				if (st.getHumanBattle() != null && st.getHumanBattle().getMain() != null) {
					BattleCard om = st.getHumanBattle().getMain();
					if (CardAttributes.hasAttribute(defs.get(om.getCardId()), om, "DRAGON")) {
						moveZoneToRest(st.getHumanBattle(), st.getHumanRest(), st, st.getHumanHand(), defs);
						st.setHumanBattle(null);
						st.addLog("ダークドラゴン: 相手ドラゴンをレストへ");
					}
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
				st.addLog("ストーニア: ストーン+1");
			}
			case "CRYSTAKUL" -> {
				PendingEffect cpe = st.getPendingEffect();
				if (cpe != null && cpe.isCrystakulOptionalResolved()) {
					break;
				}
				if (st.getCpuStones() >= CRYSTAKUL_OPTIONAL_STONE_COST) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"クリスタクル（ストーン2・次の配置+3／次の相手ターン終了まで）",
							false,
							"CRYSTAKUL",
							CRYSTAKUL_OPTIONAL_STONE_COST,
							List.of(),
							true
					));
				}
			}
			case "MIRAJUKUL" -> offerMirajukulMirrorConfirmation(st, false, true, defs);
			case "STONIA" -> {
				if (st.getCpuBattle() != null) {
					int s = st.getCpuStones();
					st.addLog("ノクスクル: 自分のターンの終わりまで、所持ストーン" + s + "のぶん強さ+" + s);
				}
			}
			case "LUMINANK" -> {
				int n = st.getCpuStones();
				st.setCpuStones(n * 2);
				st.addLog("ルミナンク: ストーンを" + n + "から" + (n * 2) + "に");
			}
			case "FEZARIA" -> {
				if (st.getCpuStones() >= FEZARIA_OPTIONAL_STONE_COST
						&& restContainsFezariaPickableCarbuncle(st, st.getCpuRest(), st.getCpuBattle(), defs)) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.CONFIRM_OPTIONAL_STONE,
							"フェザリア（ストーン3・フェザリア以外のカーバンクルを回収）",
							false,
							"FEZARIA",
							FEZARIA_OPTIONAL_STONE_COST,
							List.of(),
							true
					));
				}
			}
			case "MACHINE_GUNNER" -> machineGunDiscardOpponentStones(st, d, defs, false, false);
			case "SPEC777" -> {
				ZoneFighter z777g = st.getCpuBattle();
				if (z777g != null && z777g.getMain() != null && z777g.getMain().getCardId() == SPEC_777_ID) {
					int roll = 2 + ThreadLocalRandom.current().nextInt(6);
					z777g.setSpec777RolledPower(roll);
					st.addLog("SPEC-777: 出目=" + roll);
					applySpec777DeployLossIfRollBelowOpponentAtDeploy(st, defs, false);
				}
			}
			case "SPEC666" -> {
				st.setSpec666NextHumanUndead(true);
				st.setSpec666NextCpuUndead(true);
				st.addLog("SPEC-666: 次に双方が配置するファイターはアンデッド扱い");
			}
			case "SPEC123" -> {
				ZoneFighter z123g = st.getCpuBattle();
				if (z123g != null && z123g.getMain() != null && z123g.getMain().getCardId() == SPEC_123_ID) {
					int gain = 1 + ThreadLocalRandom.current().nextInt(3);
					st.setCpuStones(st.getCpuStones() + gain);
					st.addLog("SPEC-123: ストーン+" + gain);
				}
			}
			case "SPEC0", "SPEC1" -> {
				ZoneFighter z0g = st.getCpuBattle();
				if (z0g == null || z0g.getMain() == null || z0g.getMain().getCardId() != SPEC_1_ID) {
					break;
				}
				List<String> spec1GuestOpts = new ArrayList<>();
				for (BattleCard c : st.getCpuRest()) {
					if (isSpec1EligibleRestFighter(c, z0g, defs)) {
						spec1GuestOpts.add(c.getInstanceId());
					}
				}
				if (!spec1GuestOpts.isEmpty()) {
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_REST_TO_DECK_TOP,
							"SPEC-1（「強さ1」のファイターを1枚、デッキ上へ）",
							false,
							"SPEC1",
							0,
							spec1GuestOpts,
							true));
				}
			}
			default -> {
			}
		}
	}

	private void applyDeployCpu(CpuBattleState st, CardDefinition d, Map<Short, CardDefinition> defs, Random rnd,
			BattleCard deployedMain) {
		deployedMain = st.getCpuBattle() != null ? st.getCpuBattle().getMain() : null;
		applySpec666UndeadToDeployedFighterIfPending(st, false, defs);
		if (st != null && deployAbilitySuppressedByOpponentLine(st, false, d)) {
			return;
		}
		if (st != null && hasMikaelsWrathOnZone(st.getHumanBattle())) {
			return;
		}
		deployedMain = st.getCpuBattle() != null ? st.getCpuBattle().getMain() : null;
		if (deployedMain != null && deployedMain.isBlankEffects()) {
			return;
		}
		CardDefinition fighterForKamuiCpu = deployedMain != null ? defs.get(deployedMain.getCardId()) : null;
		if (fieldKamuiSuppressesDeployEffects(st, fighterForKamuiCpu)) {
			return;
		}
		String code = d != null ? d.getAbilityDeployCode() : null;
		if (code == null || code.isBlank()) {
			if (d != null && isCrystakulCardDefinition(d)) {
				code = "CRYSTAKUL";
			} else if (d != null && isBotBikeCardDefinition(d)) {
				code = "BOT_BIKE";
			} else if (d != null && isSpec1CardDefinition(d)) {
				code = "SPEC1";
			} else if (d != null && isHarpPlayerCardDefinition(d)) {
				code = "HARP_PLAYER";
			} else if (d != null && isGravePriestCardDefinition(d)) {
				code = "GRAVE_PRIEST";
			} else if (d != null && isBelieverCardDefinition(d)) {
				code = "BELIEVER";
			} else if (d != null && isHalfElfCardDefinition(d)) {
				code = "HALF_ELF";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.KRAKEN_FIGHTER_CARD_ID) {
				code = "KRAKEN";
			} else if (d != null && d.getId() != null && d.getId() == RAMIEL_ID) {
				code = RAMIEL_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.SIREN_FIGHTER_CARD_ID) {
				code = "SIREN";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.POSEIDON_FIGHTER_CARD_ID) {
				code = "POSEIDON";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.MERMAID_FIGHTER_CARD_ID) {
				code = "MERMAID";
			} else if (d != null && d.getId() != null && d.getId() == KUSURI_ID) {
				code = "KUSURI";
			} else if (d != null && isResearcherFloraCardDefinition(d)) {
				code = "RESEARCHER_FLORA";
			} else if (d != null && isMangakaCardDefinition(d)) {
				code = "MANGAKA";
			} else if (d != null && isComicDinosaurCardDefinition(d)) {
				code = "COMIC_DINOSAUR";
			} else if (d != null && isBehemothCardDefinition(d)) {
				code = "BEHEMOTH";
			} else if (d != null && isFafnirCardDefinition(d)) {
				code = "FAFNIR";
			} else if (d != null && isBahamutCardDefinition(d)) {
				code = "BAHAMUT";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.PAGE_WALKER_FIGHTER_CARD_ID) {
				code = "PAGE_WALKER";
			} else if (d != null && d.getId() != null && d.getId() == KING_MAKER_ID) {
				code = "KING_MAKER";
			} else if (d != null && d.getId() != null && d.getId() == DOMINION_ID) {
				code = DOMINION_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == MINION_SOLDIER_ID) {
				code = MINION_SOLDIER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == INK_KING_ID) {
				code = "INK_KING";
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.SKETCHER_FIGHTER_CARD_ID) {
				code = SKETCHER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.ZADKIEL_FIGHTER_CARD_ID) {
				code = ZADKIEL_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.LUCIFER_FIGHTER_CARD_ID) {
				code = LUCIFER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.FALLEN_ANGEL_LUCIFER_CARD_ID) {
				code = FALLEN_ANGEL_LUCIFER_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.CELESTIA_FIGHTER_CARD_ID) {
				code = CELESTIA_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.VIRTUAL_FIGHTER_CARD_ID) {
				code = VIRTUAL_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.SERAPHIM_FIGHTER_CARD_ID) {
				code = SERAPHIM_DEPLOY_CODE;
			} else if (d != null && d.getId() != null && d.getId() == GameConstants.COMIC_WITCH_FIGHTER_CARD_ID) {
				code = COMIC_WITCH_DEPLOY_CODE;
			} else {
				return;
			}
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
				// あなたが先に選び、CPU は人間の選択のあとに簡易 AI で捨てる（両方手札がある場合）
				boolean hadHumanHand = !st.getHumanHand().isEmpty();
				boolean hadCpuHand = !st.getCpuHand().isEmpty();
				if (hadHumanHand) {
					List<String> opts = new ArrayList<>();
					for (BattleCard c : st.getHumanHand()) opts.add(c.getInstanceId());
					String ability = hadCpuHand ? KENTOSHI_PAIR_FIRST_DEPLOY_CODE : KENTOSHI_SOLO_DEPLOY_CODE;
					st.setPendingChoice(new PendingChoice(
							ChoiceKind.SELECT_ONE_FROM_HAND_TO_REST,
							"剣闘士（捨てるカードを選択）",
							true,
							ability,
							0,
							opts
					));
				} else if (hadCpuHand) {
					kentoshiAiDiscardOneFromHand(st.getCpuHand(), st.getCpuRest(), defs);
				}
				if (hadHumanHand && hadCpuHand) {
					st.addLog("剣闘士: お互い手札を1枚レストへ");
				} else if (hadHumanHand) {
					st.addLog("剣闘士: 自分の手札を1枚レストへ（相手は手札なし）");
				} else if (hadCpuHand) {
					st.addLog("剣闘士: 相手の手札を1枚レストへ（自分は手札なし）");
				}
			}
			case "KARYUDO" -> {
				if (!st.getHumanHand().isEmpty()) {
					int r = rnd.nextInt(st.getHumanHand().size());
					BattleCard c = st.getHumanHand().remove(r);
					st.getHumanDeck().add(0, c);
					st.addLog("CPU狩人: あなたの手札を1枚デッキの上へ");
				}
			}
			case "KAENRYU" -> {
				if (st.getHumanBattle() != null) {
					moveZoneToRest(st.getHumanBattle(), st.getHumanRest(), st, st.getHumanHand(), defs);
					st.setHumanBattle(null);
					st.addLog("CPU火炎竜: あなたのファイターをレストへ");
				}
			}
			case "DAKU_DORAGON" -> {
				st.setCpuStones(st.getCpuStones() + 2);
				st.addLog("CPUダークドラゴン: ストーン+2");
				if (st.getHumanBattle() != null && st.getHumanBattle().getMain() != null) {
					BattleCard om = st.getHumanBattle().getMain();
					if (CardAttributes.hasAttribute(defs.get(om.getCardId()), om, "DRAGON")) {
						moveZoneToRest(st.getHumanBattle(), st.getHumanRest(), st, st.getHumanHand(), defs);
						st.setHumanBattle(null);
						st.addLog("CPUダークドラゴン: 相手ドラゴンをレストへ");
					}
				}
			}
			case "GURIFON" -> {
				if (st.getHumanStones() > 0) {
					st.setHumanStones(st.getHumanStones() - 1);
					st.addLog("CPUグリフォン: あなたがストーンを1つ捨てた");
				}
			}
			case "KAZE_MAJIN" -> {
				st.setCpuStones(st.getCpuStones() + 2);
				st.addLog("CPU風魔人: ストーン+2");
			}
			case "KOSAKUIN" -> {
				// 用心棒（旧: 工作員）に変更されたため効果なし
			}
			case "KUSURI" -> {
				ZoneFighter zb = st.getCpuBattle();
				if (zb != null && zb.getMain() != null && zb.getMain().getCardId() == KUSURI_ID) {
					int n = st.getCpuStones();
					zb.setKusuriOpponentDebuffFromDeployStones(n);
					st.addLog("CPU薬売り: 配置時の所持ストーン" + n + "のぶん、相手ファイターの強さ−" + n);
				}
			}
			case "KAGAKUSHA" -> {
				if (st.getHumanBattle() != null && st.getCpuBattle() != null) {
					st.setPowerSwapActive(true);
					st.addLog("CPU科学者: 強さを入れ替えた");
				}
			}
			case "OKAMI_OTOKO" -> {
				if (st.getCpuBattle() != null) {
					swapMainWithWolfIfPaid(st.getCpuBattle(), st);
				}
			}
			case "MIKO" -> {
				// エルフの巫女: ストーン消費なしで、次回配置+1
				st.setCpuNextDeployBonus(st.getCpuNextDeployBonus() + 1);
				st.addLog("CPUエルフの巫女: 次の配置+1");
			}
			case "MIRACLE" -> {
				st.setCpuStones(st.getCpuStones() + 1);
				st.addLog("CPU奇跡: ストーンを1つ得た");
			}
			case "MIKAEL" -> applyMikaelMiracleDeckTransformOnDeploy(st, false, defs);
			case "MIKAEL_WRATH" -> st.addLog("CPUミカエルの怒りを配置した");
			case "MIKAEL_PUNCH" -> {
				ZoneFighter zf = st.getCpuBattle();
				if (zf != null) {
					zf.setZadkielOpponentTurnPowerBonus(3);
					st.addLog("CPUミカエルパンチ: 相手ターンの間、強さ+3");
				}
			}
			case "MIKAEL_STRATEGY" -> applyMikaelStrategyDeployEffect(st, false, true, defs);
			case "MIKAEL_MINION_A" -> {
				ZoneFighter zf = st.getCpuBattle();
				if (zf != null && zf.getMain() != null
						&& restContainsMikaelMinionExcludingTucked(zf, st.getCpuRest(),
								GameConstants.MIKAEL_MINION_B_CARD_ID)) {
					zf.getMain().setBattleEndPowerBonus(zf.getMain().getBattleEndPowerBonus() + 2);
					st.addLog("CPUミカエルの使いA: 強さ+2");
				}
			}
			case "MIKAEL_MINION_B" -> {
				ZoneFighter zf = st.getCpuBattle();
				if (zf != null
						&& restContainsMikaelMinionExcludingTucked(zf, st.getCpuRest(),
								GameConstants.MIKAEL_MINION_A_CARD_ID)) {
					zf.setZadkielOpponentTurnPowerBonus(zf.getZadkielOpponentTurnPowerBonus() + 1);
					st.addLog("CPUミカエルの使いB: 相手ターンの間、強さ+1");
				}
			}
			case "MIKAEL_FLASH" -> moveActiveFieldToDeployersRestForMikaelFlash(st, false, defs);
			case "FALLEN_ANGEL_LUCIFER" -> applyFallenAngelLuciferDeployEffect(st, false, true, defs);
			case "YOSEI" -> {
				// CPU: ストーンがあれば使う（ウッドエルフ：次のエルフ配置+3）
				if (st.getCpuStones() >= 1) {
					st.setCpuStones(st.getCpuStones() - 1);
					st.setCpuNextElfOnlyBonus(st.getCpuNextElfOnlyBonus() + 3);
					st.addLog("CPUウッドエルフ: 次のエルフ配置+3");
				}
			}
			case "SEASERPENT" -> {
				if (st.getCpuStones() >= 1 && defs.get(GameConstants.SWORDFISH_TOKEN_CARD_ID) != null) {
					st.setCpuStones(st.getCpuStones() - 1);
					addCopiesOfCardIdToHand(st.getCpuHand(), GameConstants.SWORDFISH_TOKEN_CARD_ID, 1, defs);
					st.addLog("CPUシーサーペント: ストーン1使用。「ソードフィッシュ」を1枚手札に加えた");
				}
			}
			case "CELESTIA" -> {
				if (st.getCpuStones() >= 1 && canGrantMiracleSlotCard(st, false, defs)) {
					st.setCpuStones(st.getCpuStones() - 1);
					addMiracleCopiesToHandForPlayer(st.getCpuHand(), 2, st, false, defs);
					st.addLog("CPUセレスティア: ストーン1使用。「奇跡」を2枚手札に加えた");
				} else if (st.getCpuStones() < 1) {
					st.addLog("CPUセレスティア: ストーンがないため効果はなかった");
				} else {
					st.addLog("CPUセレスティア: 「奇跡」の定義がない");
				}
			}
			case "VIRTUAL" -> {
				st.setCpuStones(st.getCpuStones() + 2);
				st.addLog("CPUヴァーチャー: ストーンを2つ得た");
				if (canGrantMiracleSlotCard(st, false, defs)) {
					addMiracleCopiesToHandForPlayer(st.getCpuHand(), 1, st, false, defs);
					String nm = defs.get(miracleGrantCardId(st, false, defs)).getName();
					st.addLog("CPUヴァーチャー: 「" + (nm != null ? nm : "？") + "」を1枚手札に加えた");
				} else {
					st.addLog("CPUヴァーチャー: 「奇跡」の定義がない");
				}
			}
			case "RESEARCHER_FLORA" -> {
				if (st.getCpuStones() >= 1) {
					ZoneFighter zb = st.getCpuBattle();
					for (int i = st.getCpuRest().size() - 1; i >= 0; i--) {
						BattleCard c = st.getCpuRest().get(i);
						if (isTuckedUnderOwnFighter(zb, c)) {
							continue;
						}
						if (restCardHasTribe(st, defs.get(c.getCardId()), c, "ELF")) {
							st.setCpuStones(st.getCpuStones() - 1);
							st.getCpuRest().remove(i);
							st.getCpuHand().add(0, c);
							st.addLog("CPU研究者フローラ: ストーン1使用。レストの「種族：エルフ」を手札へ");
							break;
						}
					}
				}
			}
			case "MANGAKA" -> applyMangakaDeployEffect(st, false, true, defs, rnd);
			case "COMIC_DINOSAUR" -> applyComicDinosaurDeployForCpu(st, defs, rnd);
			case "ZADKIEL" -> {
				String miracleInst = null;
				for (BattleCard c : st.getCpuHand()) {
					if (c != null && c.getInstanceId() != null && c.getCardId() == GameConstants.MIRACLE_TOKEN_CARD_ID) {
						miracleInst = c.getInstanceId();
						break;
					}
				}
				if (miracleInst != null) {
					BattleCard removed = removeByInstanceId(st.getCpuHand(), miracleInst);
					if (removed != null) {
						st.getCpuRest().add(removed);
						st.setCpuPendingZadkielNextDeployOppTurnPower3(true);
						st.addLog("CPUザドキエル: 「奇跡」をレストへ置いた");
					}
				} else {
					st.addLog("CPUザドキエル: 手札に「奇跡」がない");
				}
			}
			case "SERAPHIM" -> {
				String miracleInstSer = null;
				for (BattleCard c : st.getCpuHand()) {
					if (c != null && c.getInstanceId() != null && c.getCardId() == GameConstants.MIRACLE_TOKEN_CARD_ID) {
						miracleInstSer = c.getInstanceId();
						break;
					}
				}
				List<String> serAng = seraphimAngelRestOptionIds(st, false, defs);
				if (miracleInstSer != null && !serAng.isEmpty()) {
					BattleCard rmSer = removeByInstanceId(st.getCpuHand(), miracleInstSer);
					if (rmSer != null) {
						st.getCpuRest().add(rmSer);
						st.addLog("CPUセラフィム: 「奇跡」をレストへ置いた");
					}
					Random rSer = rnd != null ? rnd : ThreadLocalRandom.current();
					List<String> serPickList = seraphimAngelRestOptionIds(st, false, defs);
					if (!serPickList.isEmpty()) {
						int pickI = rSer.nextInt(serPickList.size());
						String instS = serPickList.get(pickI);
						BattleCard movedSer = removeByInstanceId(st.getCpuRest(), instS);
						if (movedSer != null) {
							st.getCpuHand().add(0, movedSer);
							st.addLog("CPUセラフィム: レストの「種族：エンジェル」を手札へ");
						}
					}
				} else if (miracleInstSer == null) {
					st.addLog("CPUセラフィム: 手札に「奇跡」がない");
				} else {
					st.addLog("CPUセラフィム: レストに対象のカードがないため「奇跡」を置かなかった");
				}
			}
			case "COMIC_WITCH" -> {
				if (defs.get(INK_KNIGHT_ID) == null) {
					break;
				}
				List<String> cwCpuOpts = comicWitchPickableRestInstanceIds(st, false);
				if (st.getCpuStones() >= 1 && !cwCpuOpts.isEmpty()) {
					st.setCpuStones(st.getCpuStones() - 1);
					Random r = rnd != null ? rnd : ThreadLocalRandom.current();
					int taken = 0;
					while (taken < 2 && !cwCpuOpts.isEmpty()) {
						int pick = r.nextInt(cwCpuOpts.size());
						String inst = cwCpuOpts.remove(pick);
						BattleCard removed = removeByInstanceId(st.getCpuRest(), inst);
						if (removed != null) {
							st.getCpuHand().add(0, new BattleCard(UUID.randomUUID().toString(), INK_KNIGHT_ID));
							taken++;
						}
					}
					st.addLog("CPUコミックウィッチ: ストーン1使用。「インクナイト」に" + taken + "枚変化して手札へ");
				} else if (st.getCpuStones() < 1) {
					st.addLog("CPUコミックウィッチ: ストーンがないため効果はなかった");
				} else {
					st.addLog("CPUコミックウィッチ: レストに対象のカードがないため効果はなかった");
				}
			}
			case "MERMAID" -> applyMermaidDeployEffect(st, st.getCpuHand(), false, true, defs);
			case "SIREN" -> applySirenDeployEffect(st, false, true, defs);
			case "POSEIDON" -> applyPoseidonDeployEffect(st, false, true, defs);
			case "KRAKEN" -> {
				st.setCpuKrakenNextTurnSwordfishAdds(st.getCpuKrakenNextTurnSwordfishAdds() + 1);
				st.addLog("CPUクラーケン: 次の自分のターンの開始時に「ソードフィッシュ」を1枚加える");
			}
			case "RAMIEL" -> {
				if (st.getCpuStones() >= 1 && canGrantMiracleSlotCard(st, false, defs)) {
					st.setCpuStones(st.getCpuStones() - 1);
					applyRamielDeployEffect(st, false, true);
				} else if (st.getCpuStones() < 1) {
					st.addLog("CPUラミエル: ストーンがないため効果はなかった");
				} else {
					st.addLog("CPUラミエル: 「奇跡」の定義がない");
				}
			}
			case "LUCIFER" -> applyLuciferDeployEffect(st, false, true, defs);
			case "LEVIATHAN" -> {
				int pickedLv = 0;
				for (int i = st.getCpuRest().size() - 1; i >= 0 && pickedLv < 2; i--) {
					BattleCard c = st.getCpuRest().get(i);
					if (isLeviathanPickableDragonOrMerfolkInRest(st, c, st.getCpuBattle(), defs)) {
						st.getCpuRest().remove(i);
						st.getCpuHand().add(0, c);
						pickedLv++;
					}
				}
				st.addLog("CPUリヴァイアサン: レストから" + pickedLv + "枚手札へ");
				advanceActiveFieldCountForLeviathan(st, 2, defs);
			}
			case "PAGE_WALKER" -> {
				st.setCpuStones(st.getCpuStones() + 1);
				st.addLog("CPUページウォーカー: ストーン+1");
				advanceActiveFieldCountForLeviathan(st, 2, defs);
			}
			case "KING_MAKER" -> applyKingMakerDeployEffect(st, false, true, defs);
			case "DOMINION" -> applyDominionDeployEffect(st, false, true, defs);
			case "MINION_SOLDIER" -> applyMinionSoldierDeployEffect(st, false, true, defs);
			case "INK_KING" -> applyInkKingDeployEffect(st, false, true, defs);
			case "SKETCHER" -> applySketcherDeployEffect(st, false, true, defs, rnd);
			case "HARP_PLAYER" -> {
				st.setCpuNextElfOnlyBonus(st.getCpuNextElfOnlyBonus() + HARP_NEXT_ELF_POWER_BONUS);
				st.addLog("CPU森のハープ弾き: 次に配置するエルフはターン終了まで強さ+" + HARP_NEXT_ELF_POWER_BONUS);
			}
			case "GRAVE_PRIEST" -> {
				List<BattleCard> gpEligible = new ArrayList<>();
				for (BattleCard c : st.getCpuHand()) {
					CardDefinition cd = defs.get(c.getCardId());
					if (!isGravePriestEligibleHandFighter(cd, c)) {
						continue;
					}
					if (effectiveDeployCost(cd, c, defs, st.getCpuRest(), st.getCpuNextMechanicStacks(), st) <= 0) {
						continue;
					}
					gpEligible.add(c);
				}
				if (gpEligible.isEmpty()) {
					st.addLog("CPU墓守神父: 手札に対象のファイターがいない");
				} else {
					BattleCard pick = null;
					int bestEc = -1;
					for (BattleCard c : gpEligible) {
						CardDefinition cd = defs.get(c.getCardId());
						int ec = effectiveDeployCost(cd, c, defs, st.getCpuRest(), 0, st);
						if (ec > bestEc) {
							bestEc = ec;
							pick = c;
						}
					}
					if (pick != null) {
						pick.setHandDeployCostModifier(pick.getHandDeployCostModifier() - GRAVE_PRIEST_HAND_COST_REDUCTION);
						st.addLog("CPU墓守神父: 手札のファイターのコストを-" + GRAVE_PRIEST_HAND_COST_REDUCTION + "（バトル終了まで）");
					}
				}
			}
			case "BELIEVER" -> {
				int dbCpu = moveAllDeathbounceFromRestToHand(st.getCpuRest(), st.getCpuHand());
				if (dbCpu > 0) {
					st.addLog("CPU信奉者: 霊園教会 デスバウンスを" + dbCpu + "枚手札へ");
				} else {
					st.addLog("CPU信奉者: レストに「霊園教会 デスバウンス」がない");
				}
			}
			case "BEHEMOTH" -> applyBehemothDeployStoneGain(st, st.getCpuBattle(), false);
			case "FAFNIR" -> applyFafnirDeployEffect(st, false, true, defs);
			case "BAHAMUT" -> applyBahamutDeployEffect(st, false, true, defs);
			case "HALF_ELF" -> {
				ZoneFighter hzHeCpu = st.getCpuBattle();
				if (hzHeCpu != null) {
					int hebCpu = halfElfLineagePowerBonusFromRest(st, st.getCpuRest(), defs);
					if (hebCpu > 0) {
						hzHeCpu.setTemporaryPowerBonus(hzHeCpu.getTemporaryPowerBonus() + hebCpu);
						st.addLog("CPUハーフエルフ: 強さ+" + hebCpu);
					}
				}
			}
			case "SHOKIN" -> {
				st.setCpuNextDeployCostBonusTimes(st.getCpuNextDeployCostBonusTimes() + 1);
				st.addLog("CPU隊長: 次の配置はコストぶん強化");
			}
			case "MECHANIC" -> {
				st.setCpuNextMechanicStacks(st.getCpuNextMechanicStacks() + 1);
				st.addLog("CPUメカニック: 次の配置はマシン化（バトル終了まで）＋ターン終了までコスト+1、強さ+3");
			}
			case "BOT_BIKE" -> {
				ZoneFighter zb = st.getCpuBattle();
				if (zb != null && zb.getMain() != null && zb.getMain().getCardId() == BOT_BIKE_ID
						&& zoneCostIncludesMechanicFighter(zb, defs)) {
					zb.setBotBikeMechanicPowerBonus(3);
					st.addLog("CPUボットバイク: 次の相手ターン終了まで強さ+3");
				}
			}
			case "KINOKO" -> {
				for (int i = st.getCpuRest().size() - 1; i >= 0; i--) {
					BattleCard c = st.getCpuRest().get(i);
					if (restCardHasTribe(st, defs.get(c.getCardId()), c, "ELF")) {
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
					if (isHumanFighterInRestSlot(st, defs.get(c.getCardId()), c)) {
						st.getCpuRest().remove(i);
						c.setBlankEffects(true);
						st.getCpuHand().add(0, c);
						st.addLog("CPU炭鉱夫: 人間ファイターを手札へ（効果なし）");
						break;
					}
				}
			}
			case "ARTHUR" -> {
				BattleCard fieldAr = st.getActiveField();
				if (fieldAr == null || fieldAr.getCardId() != FIELD_KAMUI_ID) {
					break;
				}
				ZoneFighter ownBattleAr = st.getCpuBattle();
				boolean picked = false;
				for (int i = st.getCpuRest().size() - 1; i >= 0; i--) {
					BattleCard c = st.getCpuRest().get(i);
					if (isTuckedUnderOwnFighter(ownBattleAr, c)) {
						continue;
					}
					if (isHumanFighterInRestSlot(st, defs.get(c.getCardId()), c)) {
						st.getCpuRest().remove(i);
						st.getCpuHand().add(0, c);
						st.addLog("CPUアーサー: 人間ファイターを手札へ");
						picked = true;
						break;
					}
				}
				if (!picked) {
					st.addLog("CPUアーサー: レストに「種族：人間」のファイターがない");
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
						c.setBattleEndPowerBonus(c.getBattleEndPowerBonus() + JOSHU_ASSISTANT_RESEARCHER_BATTLE_END_POWER_BONUS);
						st.getCpuHand().add(0, c);
						st.addLog("CPU助手: 名前に「研究者」を含むカードを手札へ（バトル終了まで強さ+"
								+ JOSHU_ASSISTANT_RESEARCHER_BATTLE_END_POWER_BONUS + "）");
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
				ZoneFighter ownBattleRyCpu = st.getCpuBattle();
				for (int i = 0; i < st.getCpuRest().size(); i++) {
					BattleCard c = st.getCpuRest().get(i);
					if (isTuckedUnderOwnFighter(ownBattleRyCpu, c)) {
						continue;
					}
					if (restCardHasTribe(st, defs.get(c.getCardId()), c, "DRAGON")) {
						st.getCpuRest().remove(i);
						st.getCpuHand().add(0, c);
						st.addLog("CPUドラゴンの卵: レストのドラゴンを1枚手札へ");
						break;
					}
				}
			}
			case "KORYU" -> {
				if (st.getCpuStones() >= 1 && st.getCpuBattle() != null) {
					int elves = countAttributeInRest(st, st.getCpuRest(), defs, "ELF");
					if (elves > 0) {
						st.setCpuStones(st.getCpuStones() - 1);
						st.setCpuKoryuBonus(elves);
						st.addLog("CPU古竜: 次の相手ターン終了まで +" + elves);
					}
				}
			}
			case "NOXSKUL" -> {
				st.setCpuStones(st.getCpuStones() + 1);
				st.addLog("CPUストーニア: ストーン+1");
			}
			case "CRYSTAKUL" -> {
				if (st.getCpuStones() >= CRYSTAKUL_OPTIONAL_STONE_COST) {
					st.setCpuStones(st.getCpuStones() - CRYSTAKUL_OPTIONAL_STONE_COST);
					st.setCpuNextCrystakulDeployBonus(st.getCpuNextCrystakulDeployBonus() + CRYSTAKUL_NEXT_DEPLOY_POWER);
					st.addLog("CPUクリスタクル: 次の配置+3（次の相手ターン終了まで）");
				}
			}
			case "MIRAJUKUL" -> applyMirageMirrorDeploy(st, false, defs, rnd);
			case "STONIA" -> {
				if (st.getCpuBattle() != null) {
					int s = st.getCpuStones();
					st.addLog("CPUノクスクル: 自分のターンの終わりまで、所持ストーン" + s + "のぶん強さ+" + s);
				}
			}
			case "LUMINANK" -> {
				int n = st.getCpuStones();
				st.setCpuStones(n * 2);
				st.addLog("CPUルミナンク: ストーンを" + n + "から" + (n * 2) + "に");
			}
			case "FEZARIA" -> {
				if (st.getCpuStones() >= FEZARIA_OPTIONAL_STONE_COST
						&& restContainsFezariaPickableCarbuncle(st, st.getCpuRest(), st.getCpuBattle(), defs)) {
					st.setCpuStones(st.getCpuStones() - FEZARIA_OPTIONAL_STONE_COST);
					int picked = 0;
					for (int i = st.getCpuRest().size() - 1; i >= 0 && picked < 2; i--) {
						BattleCard c = st.getCpuRest().get(i);
						if (isFezariaPickableCarbuncleInRest(st, c, st.getCpuBattle(), defs)) {
							st.getCpuRest().remove(i);
							st.getCpuHand().add(0, c);
							picked++;
						}
					}
					st.addLog("CPUフェザリア: ストーン3使用、フェザリア以外のカーバンクル" + picked + "枚回収");
				}
			}
			case "MACHINE_GUNNER" -> machineGunDiscardOpponentStones(st, d, defs, false, true);
			case "SPEC777" -> {
				ZoneFighter z777c = st.getCpuBattle();
				if (z777c != null && z777c.getMain() != null && z777c.getMain().getCardId() == SPEC_777_ID) {
					Random r = rnd != null ? rnd : new Random();
					int roll = 2 + r.nextInt(6);
					z777c.setSpec777RolledPower(roll);
					st.addLog("CPU SPEC-777: 出目=" + roll);
					applySpec777DeployLossIfRollBelowOpponentAtDeploy(st, defs, false);
				}
			}
			case "SPEC666" -> {
				st.setSpec666NextHumanUndead(true);
				st.setSpec666NextCpuUndead(true);
				st.addLog("CPU SPEC-666: 次に双方が配置するファイターはアンデッド扱い");
			}
			case "SPEC123" -> {
				ZoneFighter z123c = st.getCpuBattle();
				if (z123c != null && z123c.getMain() != null && z123c.getMain().getCardId() == SPEC_123_ID) {
					Random r = rnd != null ? rnd : new Random();
					int gain = 1 + r.nextInt(3);
					st.setCpuStones(st.getCpuStones() + gain);
					st.addLog("CPU SPEC-123: ストーン+" + gain);
				}
			}
			case "SPEC0", "SPEC1" -> {
				ZoneFighter z0c = st.getCpuBattle();
				if (z0c == null || z0c.getMain() == null || z0c.getMain().getCardId() != SPEC_1_ID) {
					break;
				}
				for (int i = st.getCpuRest().size() - 1; i >= 0; i--) {
					BattleCard c = st.getCpuRest().get(i);
					if (isSpec1EligibleRestFighter(c, z0c, defs)) {
						st.getCpuRest().remove(i);
						st.getCpuDeck().add(0, c);
						st.addLog("CPU SPEC-1: レストのファイターをデッキの上に置いた");
						break;
					}
				}
			}
			default -> {
			}
		}
	}

	private void swapMainWithWolfIfPaid(ZoneFighter z, CpuBattleState st) {
		if (z == null || z.getMain() == null || st == null) return;
		for (int i = 0; i < z.getCostUnder().size(); i++) {
			BattleCard c = z.getCostUnder().get(i);
			if (c != null && c.getCardId() == 21) {
				BattleCard oldMain = z.getMain();
				z.getCostUnder().set(i, oldMain);
				z.setMain(c);
				z.setBattleMainLineSeq(st.takeNextBattleMainLineSeq());
				st.addLog("狼男: 前列のメインとコストの人狼カードを入れ替えた");
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

	private int countAttributeInRest(CpuBattleState st, List<BattleCard> rest, Map<Short, CardDefinition> defs, String attr) {
		int n = 0;
		for (BattleCard c : rest) {
			if (restCardHasTribe(st, defs.get(c.getCardId()), c, attr)) {
				n++;
			}
		}
		return n;
	}

	/** レストのカードが「種族：マシン」のファイター（〈フィールド〉除外）か */
	private static boolean isMachineFighterInRest(CpuBattleState st, BattleCard c, Map<Short, CardDefinition> defs) {
		if (c == null || defs == null) {
			return false;
		}
		CardDefinition d = defs.get(c.getCardId());
		if (!isNonFieldFighterCardDef(d)) {
			return false;
		}
		return restCardHasTribe(st, d, c, "MACHINE");
	}

	/**
	 * 艦隊 HO-IVI-I3: レストのマシン・ファイターをすべてデッキに加え、デッキ全体をシャッフル。移動枚数を返す。
	 */
	private static int sweepMachineFightersFromRestToDeckAndShuffle(List<BattleCard> rest, List<BattleCard> deck,
			Map<Short, CardDefinition> defs, CpuBattleState st, Random rnd) {
		if (rest == null || deck == null || defs == null) {
			return 0;
		}
		Random r = rnd != null ? rnd : new Random();
		int n = 0;
		for (int i = rest.size() - 1; i >= 0; i--) {
			BattleCard c = rest.get(i);
			if (isMachineFighterInRest(st, c, defs)) {
				rest.remove(i);
				deck.add(c);
				n++;
			}
		}
		if (n > 0) {
			Collections.shuffle(deck, r);
		}
		return n;
	}

	/**
	 * 〈フィールド〉艦隊 HO-IVI-I3 配置直後: 両プレイヤーのレストからマシン・ファイターを各デッキへ（持続効果なし）。
	 */
	private void applyFleetHoIviFieldDeployBothSides(CpuBattleState st, Map<Short, CardDefinition> defs, Random rnd) {
		if (st == null || defs == null) {
			return;
		}
		int nh = sweepMachineFightersFromRestToDeckAndShuffle(st.getHumanRest(), st.getHumanDeck(), defs, st, rnd);
		int nc = sweepMachineFightersFromRestToDeckAndShuffle(st.getCpuRest(), st.getCpuDeck(), defs, st, rnd);
		if (nh + nc > 0) {
			st.addLog("艦隊 HO-IVI-I3: マシン・ファイターをデッキへ（あなたのレスト" + nh + "枚・" + opponentActorLogLabel(st) + "のレスト"
					+ nc + "枚）");
		} else {
			st.addLog("艦隊 HO-IVI-I3: レストに該当するマシン・ファイターはない");
		}
	}

	/**
	 * ハーフエルフ〈配置〉: レストに「種族：人間」が1枚以上あれば+2、「種族：エルフ」が1枚以上あれば+2（複合種族は両方満たし得る）。
	 */
	private static int halfElfLineagePowerBonusFromRest(CpuBattleState st, List<BattleCard> rest, Map<Short, CardDefinition> defs) {
		if (rest == null || defs == null) {
			return 0;
		}
		boolean seenHuman = false;
		boolean seenElf = false;
		for (BattleCard c : rest) {
			if (c == null) {
				continue;
			}
			CardDefinition d = defs.get(c.getCardId());
			if (!seenHuman && restCardHasTribe(st, d, c, "HUMAN")) {
				seenHuman = true;
			}
			if (!seenElf && restCardHasTribe(st, d, c, "ELF")) {
				seenElf = true;
			}
			if (seenHuman && seenElf) {
				break;
			}
		}
		return (seenHuman ? 2 : 0) + (seenElf ? 2 : 0);
	}

	/** 武器庫 VV-E4-PON: 〈フィールド〉が場にある間、種族：マシンのファイター（〈フィールド〉カード以外）の基礎コストは 1。 */
	private static boolean weaponDepotFieldActive(CpuBattleState st) {
		if (st == null || st.getActiveField() == null) {
			return false;
		}
		Short fid = st.getActiveField().getCardId();
		return fid != null && fid.shortValue() == WEAPON_DEPOT_FIELD_ID;
	}

	/** 〈フィールド〉以外のマシン・ファイター（種族は {@link CardAttributes#hasAttribute(CardDefinition, BattleCard, String)} と同じ） */
	private static boolean isMachineFighterForWeaponDepotCost(CardDefinition d, BattleCard c, CpuBattleState st,
			List<BattleCard> ownersRestForDiscount) {
		if (d == null || !isNonFieldFighterCardDef(d)) {
			return false;
		}
		if (st != null && ownersRestForDiscount != null && c != null) {
			boolean ownerHuman = ownersRestForDiscount == st.getHumanRest();
			boolean spec666 = ownerHuman ? st.isSpec666NextHumanUndead() : st.isSpec666NextCpuUndead();
			int mechStacks = ownerHuman ? st.getHumanNextMechanicStacks() : st.getCpuNextMechanicStacks();
			if (CardAttributes.hasAttributeForDeployPreview(d, c, spec666, mechStacks, "MACHINE")) {
				return true;
			}
		}
		return CardAttributes.hasAttribute(d, c, "MACHINE");
	}

	/** ネムリィ: レストのカーバンクル割引。炭鉱夫で無効化は割引なし。研究者アストリア・墓守神父: {@link BattleCard#getHandDeployCostModifier}。 */
	private int effectiveDeployCost(CardDefinition d, BattleCard deployedMain, Map<Short, CardDefinition> defs,
			List<BattleCard> ownersRestForDiscount, int mechanicExtraCost, CpuBattleState st) {
		if (d == null) {
			return 0;
		}
		// メカニックは「バトルゾーンに配置するファイター」のみ（〈フィールド〉のコスト・強さ加算はしない）
		if (isFieldCard(d)) {
			mechanicExtraCost = 0;
		}
		int base = d.getCost() != null ? d.getCost() : 0;
		if (st != null && weaponDepotFieldActive(st) && isMachineFighterForWeaponDepotCost(d, deployedMain, st,
				ownersRestForDiscount)) {
			base = 1;
		}
		int handAdj = deployedMain != null
				? deployedMain.getHandDeployCostModifier() + deployedMain.getDeathbounceHandCostStacks()
				: 0;
		if (deployedMain != null && deployedMain.isBlankEffects()) {
			int c = Math.max(0, base + mechanicExtraCost + handAdj);
			if (st != null && st.isWeeklyShonenCampGlobalDeployCostPlusOneThisTurn()) {
				c += 1;
			}
			return Math.max(0, c);
		}
		int core;
		if (d.getId() == null || d.getId() != NEMURY_ID) {
			core = base;
		} else {
			int disc = countAttributeInRest(st, ownersRestForDiscount, defs, "CARBUNCLE");
			core = Math.max(0, base - disc);
		}
		int shonen = st != null && st.isWeeklyShonenCampGlobalDeployCostPlusOneThisTurn() ? 1 : 0;
		return Math.max(0, core + mechanicExtraCost + handAdj + shonen);
	}

	/**
	 * 隊長「コストぶん強化」など、印字コストではなく《武器庫》・ネムリィ割引を反映した「そのカードのコスト」相当。
	 * メカニックの+コストや手札のコスト補正は含めない（{@link #effectiveDeployCost} の mechanic / handAdj とは別扱い）。
	 */
	private int deployCharacteristicCostForPowerBonuses(CardDefinition d, BattleCard deployedMain,
			Map<Short, CardDefinition> defs, List<BattleCard> discountRest, CpuBattleState st) {
		if (d == null) {
			return 0;
		}
		int base = d.getCost() != null ? d.getCost() : 0;
		if (st != null && weaponDepotFieldActive(st) && isMachineFighterForWeaponDepotCost(d, deployedMain, st,
				discountRest)) {
			base = 1;
		}
		if (deployedMain != null && deployedMain.isBlankEffects()) {
			int sh = st != null && st.isWeeklyShonenCampGlobalDeployCostPlusOneThisTurn() ? 1 : 0;
			return Math.max(0, base + sh);
		}
		if (d.getId() != null && d.getId() == NEMURY_ID) {
			int disc = countAttributeInRest(st, discountRest, defs, "CARBUNCLE");
			int sh = st != null && st.isWeeklyShonenCampGlobalDeployCostPlusOneThisTurn() ? 1 : 0;
			return Math.max(0, base - disc) + sh;
		}
		int shonen = st != null && st.isWeeklyShonenCampGlobalDeployCostPlusOneThisTurn() ? 1 : 0;
		return base + shonen;
	}

	/**
	 * マシンガンナー用: 自分のレストにいるファイターで、{@link #effectiveDeployCost}（メカニックの次回+コストは 0）が 1 になる枚数。
	 * 「コストが1」はネムリィ割引・研究者アストリアの手札補正・炭鉱夫無効など、配置時と同じ計算に合わせる。
	 */
	private int countRestFightersWithEffectiveCostOne(ZoneFighter ownBattle, List<BattleCard> rest,
			Map<Short, CardDefinition> defs, CpuBattleState st) {
		if (rest == null || defs == null) {
			return 0;
		}
		int n = 0;
		for (BattleCard c : rest) {
			if (c == null || isTuckedUnderOwnFighter(ownBattle, c)) {
				continue;
			}
			CardDefinition cd = defs.get(c.getCardId());
			if (!isNonFieldFighterCardDef(cd)) {
				continue;
			}
			if (effectiveDeployCost(cd, c, defs, rest, 0, st) == 1) {
				n++;
			}
		}
		return n;
	}

	/**
	 * マシンガンナー: 味方レストの該当ファイター1枚につき相手がストーン1つ捨てる（相手が持つ分のみ）。
	 */
	private void machineGunDiscardOpponentStones(CpuBattleState st, CardDefinition d, Map<Short, CardDefinition> defs,
			boolean deployerUsesHumanRest, boolean cpuAiDeploy) {
		int eligible = countRestFightersWithEffectiveCostOne(
				deployerUsesHumanRest ? st.getHumanBattle() : st.getCpuBattle(),
				deployerUsesHumanRest ? st.getHumanRest() : st.getCpuRest(),
				defs,
				st);
		if (eligible <= 0) {
			return;
		}
		int oppBefore = deployerUsesHumanRest ? st.getCpuStones() : st.getHumanStones();
		int lose = Math.min(eligible, Math.max(0, oppBefore));
		if (lose <= 0) {
			return;
		}
		if (deployerUsesHumanRest) {
			st.setCpuStones(oppBefore - lose);
		} else {
			st.setHumanStones(oppBefore - lose);
		}
		String name = d != null && d.getName() != null ? d.getName() : "マシンガンナー";
		if (cpuAiDeploy) {
			st.addLog("CPU" + name + ": あなたがストーンを" + lose + "つ捨てた");
		} else if (deployerUsesHumanRest) {
			st.addLog(name + ": " + opponentActorLogLabel(st) + "がストーンを" + lose + "つ捨てた");
		} else {
			st.addLog(name + ": 相手がストーンを" + lose + "つ捨てた");
		}
	}

	/** 〈宝石の地〉: 場にいる間、カーバンクル・ファイターは強さ+2（両プレイヤー・竜王の〈常時〉無効化後も適用） */
	private int fieldGloriaCarbunclePowerBonus(CpuBattleState st, CardDefinition fighterDef, BattleCard mainCard,
			Map<Short, CardDefinition> defs) {
		if (st == null || fighterDef == null || defs == null) {
			return 0;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != FIELD_GLORIA_ID) {
			return 0;
		}
		if (!CardAttributes.hasAttribute(fighterDef, mainCard, "CARBUNCLE")) {
			return 0;
		}
		return 2;
	}

	/** 週刊少年 CAMP: カウント6で+2、カウント2到達後さらに+4（種族：コミックの前列ファイター） */
	private static int weeklyShonenCampComicPowerForFighter(CpuBattleState st, CardDefinition fighterDef,
			BattleCard mainCard, Map<Short, CardDefinition> defs) {
		if (st == null || fighterDef == null || mainCard == null || defs == null) {
			return 0;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != GameConstants.WEEKLY_SHONEN_CAMP_FIELD_CARD_ID) {
			return 0;
		}
		if (!CardAttributes.hasAttribute(fighterDef, mainCard, "COMIC")) {
			return 0;
		}
		int p = 2;
		if (st.isWeeklyShonenCampCount2ComicBonus()) {
			p += 4;
		}
		return p;
	}

	/**
	 * 〈龍鱗海峡 ラグナロク〉: 場にある間、両者の前列で「効果なし。」のファイターは強さ+3（炭鉱夫で無効化されたインスタンス、
	 * または DB 上〈配置〉〈常時〉テキスト・能力コードがすべて空のバニラも対象）。竜王の〈常時〉無効化後も適用。
	 */
	private static int fieldRagnarokKoukaNashiPowerBonus(CpuBattleState st, CardDefinition fighterDef,
			BattleCard mainCard, Map<Short, CardDefinition> defs) {
		if (st == null || mainCard == null || defs == null) {
			return 0;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != FIELD_RAGNAROK_STRAIT_ID) {
			return 0;
		}
		if (!fighterQualifiesRagnarokKoukaNashi(fighterDef, mainCard)) {
			return 0;
		}
		return 3;
	}

	/** ラグナロクの「効果「効果なし。」」に該当するか */
	private static boolean fighterQualifiesRagnarokKoukaNashi(CardDefinition def, BattleCard main) {
		if (main != null && main.isBlankEffects()) {
			return true;
		}
		if (!isNonFieldFighterCardDef(def)) {
			return false;
		}
		return isKoukaNashiOrBlankAbilityText(def.getDeployHelp())
				&& isKoukaNashiOrBlankAbilityText(def.getPassiveHelp())
				&& isBlankAbilityField(def.getAbilityDeployCode())
				&& isBlankAbilityField(def.getAbilityPassiveCode());
	}

	private static boolean isBlankAbilityField(String s) {
		return s == null || s.isBlank();
	}

	/** 能力テキスト未設定、または表示・ルール上「効果なし。」のみ（ソードフィッシュ等） */
	private static boolean isKoukaNashiOrBlankAbilityText(String s) {
		if (isBlankAbilityField(s)) {
			return true;
		}
		String t = s.trim();
		return "効果なし。".equals(t) || "能力なし。".equals(t);
	}

	/**
	 * 〈探鉱の洞窟〉: カーバンクル・ファイターを配置したプレイヤーはストーン+1（両者・配置確定時）。
	 */
	private void applyFieldNebulaWhenCarbuncleFighterDeployed(CpuBattleState st, CardDefinition mainDef,
			BattleCard deployedMain, boolean deployerIsHuman) {
		if (st == null || mainDef == null) {
			return;
		}
		if (isFieldCard(mainDef)) {
			return;
		}
		ZoneFighter grantZone = deployerIsHuman ? st.getHumanBattle() : st.getCpuBattle();
		if (grantZone != null && grantZone.isFieldNebulaStoneGrantedForThisDeploy()) {
			return;
		}
		BattleCard field = st.getActiveField();
		if (field == null || field.getCardId() != FIELD_NEBULA_ID) {
			return;
		}
		if (!CardAttributes.hasAttribute(mainDef, deployedMain, "CARBUNCLE")) {
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

	/**
	 * 剣闘士（CPU 側）: ランダムではなく、弱いファイター・安いフィールドを優先してレストへ1枚送る。
	 */
	private void kentoshiAiDiscardOneFromHand(List<BattleCard> hand, List<BattleCard> rest,
			Map<Short, CardDefinition> defs) {
		if (hand == null || hand.isEmpty() || defs == null) {
			return;
		}
		List<BattleCard> fighters = new ArrayList<>();
		List<BattleCard> fields = new ArrayList<>();
		for (BattleCard c : hand) {
			CardDefinition d = defs.get(c.getCardId());
			if (d == null) {
				continue;
			}
			if (isFieldCard(d)) {
				fields.add(c);
			} else {
				fighters.add(c);
			}
		}
		BattleCard pick = null;
		if (!fighters.isEmpty()) {
			int bestBp = Integer.MAX_VALUE;
			int bestDiscardRr = Integer.MIN_VALUE;
			for (BattleCard c : fighters) {
				CardDefinition d = defs.get(c.getCardId());
				int bp = d != null && d.getBasePower() != null ? d.getBasePower() : 0;
				int rr = d != null ? rarityRank(d.getRarity()) : 0;
				boolean better = bp < bestBp
						|| (bp == bestBp && rr > bestDiscardRr)
						|| (bp == bestBp && rr == bestDiscardRr && pick != null && c.getInstanceId() != null
								&& pick.getInstanceId() != null
								&& c.getInstanceId().compareTo(pick.getInstanceId()) < 0);
				if (pick == null || better) {
					bestBp = bp;
					bestDiscardRr = rr;
					pick = c;
				}
			}
		} else if (!fields.isEmpty()) {
			int bestCost = Integer.MAX_VALUE;
			for (BattleCard c : fields) {
				CardDefinition d = defs.get(c.getCardId());
				int cst = d != null && d.getCost() != null ? d.getCost() : 0;
				boolean better = cst < bestCost
						|| (cst == bestCost && pick != null && c.getInstanceId() != null && pick.getInstanceId() != null
								&& c.getInstanceId().compareTo(pick.getInstanceId()) < 0);
				if (pick == null || better) {
					bestCost = cst;
					pick = c;
				}
			}
		}
		if (pick == null) {
			pick = hand.get(0);
		}
		BattleCard removed = removeByInstanceId(hand, pick.getInstanceId());
		if (removed != null) {
			rest.add(removed);
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

		// アドバンスド CPU: 〈フィールド〉差し替え後に合法配置が可能なら true（1 段だけ再帰）
		if (!forHuman && st.getCpuBattleMode() == CpuBattleMode.ADVANCED
				&& Boolean.TRUE.equals(st.getActiveFieldOwnerHuman()) && st.getActiveField() != null) {
			Random probe = new Random(31_337L);
			for (BattleCard fc : new ArrayList<>(hand)) {
				CardDefinition fd = defs.get(fc.getCardId());
				if (!isFieldCard(fd)) {
					continue;
				}
				int fcost = effectiveDeployCost(fd, fc, defs,
						st.getCpuRest(), st.getCpuNextMechanicStacks(), st);
				if (fcost > stones) {
					continue;
				}
				CpuBattleState s1 = copyStateForCpuSim(st);
				s1.setCpuStones(s1.getCpuStones() - fcost);
				BattleCard removed = removeByInstanceId(s1.getCpuHand(), fc.getInstanceId());
				if (removed == null) {
					continue;
				}
				replaceActiveField(s1, removed, false, defs);
				if (fd.getId() != null && fd.getId() == FLEET_HO_IVI_FIELD_ID) {
					applyFleetHoIviFieldDeployBothSides(s1, defs, probe);
				}
				if (canMakeLegalDeploy(s1, false, defs)) {
					return true;
				}
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
						if (isFieldCard(mainDef)) {
							continue;
						}
						List<BattleCard> discountRest = forHuman ? st.getHumanRest() : st.getCpuRest();
						int mechStacks = forHuman ? sim.getHumanNextMechanicStacks() : sim.getCpuNextMechanicStacks();
						int cost = effectiveDeployCost(mainDef, main, defs, discountRest, mechStacks, st);

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
								deployBonus += forHuman ? sim2.getHumanNextCrystakulDeployBonus() : sim2.getCpuNextCrystakulDeployBonus();
								if (forHuman) sim2.setHumanNextMechanicStacks(0);
								else sim2.setCpuNextMechanicStacks(0);
								ZoneFighter z = new ZoneFighter();
								assignBattleZoneMain(z, pickedMain, sim2);
								z.setCostUnder(paid);
								z.setCostPayCardCount(needCards);
								int levelUpDeployCan = luRest * 2 + luSt * 2;
								applyCrystakulBonusesToDeployedZone(sim2, z, deployBonus, levelUpDeployCan, forHuman);
								applyPendingZadkielBonusToNewlyDeployedZone(sim2, z, forHuman);
								retireOwnBattleZoneBeforeNewDeploy(sim2, forHuman, false, defs);
								if (forHuman) sim2.setHumanBattle(z);
								else sim2.setCpuBattle(z);

								applyFieldNebulaWhenCarbuncleFighterDeployed(sim2, mainDef, pickedMain, forHuman);

								// 配置能力反映
								if (forHuman) applyDeployHuman(sim2, mainDef, defs, pickedMain);
								else applyDeployCpu(sim2, mainDef, defs, new Random(31_337L), pickedMain);
								applyMechanicMachineTribeToDeployedFighterIfUsed(sim2, forHuman, mechStacks, defs);
								applyChojuGigaTribeIfPending(sim2, forHuman, defs);

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
