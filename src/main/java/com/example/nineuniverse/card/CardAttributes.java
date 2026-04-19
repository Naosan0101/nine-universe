package com.example.nineuniverse.card;

import com.example.nineuniverse.battle.BattleCard;
import com.example.nineuniverse.domain.CardDefinition;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 種族コードの判定。複合種族は {@code ELF_UNDEAD} のようにアンダースコア区切り（順不同は想定しない）。
 */
public final class CardAttributes {

	private CardAttributes() {
	}

	/** ソート・一覧の「代表種族」（複合は先頭セグメント） */
	public static String primarySegment(String attribute) {
		if (attribute == null || attribute.isBlank()) {
			return "";
		}
		int u = attribute.indexOf('_');
		return u < 0 ? attribute : attribute.substring(0, u);
	}

	public static Set<String> segments(String attribute) {
		if (attribute == null || attribute.isBlank()) {
			return Set.of();
		}
		return Arrays.stream(attribute.split("_"))
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static boolean hasAttribute(CardDefinition def, String tribe) {
		if (def == null || tribe == null) {
			return false;
		}
		return hasAttribute(def.getAttribute(), tribe);
	}

	public static boolean hasAttribute(String attribute, String tribe) {
		if (tribe == null) {
			return false;
		}
		if (attribute == null || attribute.isBlank()) {
			return false;
		}
		if (tribe.equals(attribute)) {
			return true;
		}
		return segments(attribute).contains(tribe);
	}

	/**
	 * バトル中インスタンスの種族上書き（SPEC-666 等）があればそれを優先して判定する。
	 */
	public static boolean hasAttribute(CardDefinition def, BattleCard battleCard, String tribe) {
		if (def == null) {
			return false;
		}
		if (battleCard != null && battleCard.getBattleTribeOverride() != null
				&& !battleCard.getBattleTribeOverride().isBlank()) {
			return hasAttribute(battleCard.getBattleTribeOverride(), tribe);
		}
		return hasAttribute(def, tribe);
	}

	/**
	 * 手札から配置する直前のボーナス計算用: SPEC-666 により次のそのスロットの配置がアンデッド扱いになる場合、
	 * まだ {@link BattleCard} に上書きが無い段階でも種族はアンデッドのみとして判定する。
	 */
	public static boolean hasAttributeForDeployPreview(CardDefinition def, BattleCard battleCard,
			boolean spec666NextSlotPending, String tribe) {
		if (def == null || tribe == null) {
			return false;
		}
		if (spec666NextSlotPending
				&& (battleCard == null || battleCard.getBattleTribeOverride() == null
						|| battleCard.getBattleTribeOverride().isBlank())) {
			return hasAttribute("UNDEAD", tribe);
		}
		return hasAttribute(def, battleCard, tribe);
	}
}
