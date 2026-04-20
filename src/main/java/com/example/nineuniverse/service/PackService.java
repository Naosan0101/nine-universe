package com.example.nineuniverse.service;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.domain.AppUser;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.repository.UserCollectionMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PackService {

	public enum PackType {
		STANDARD(3),
		WINDY_HILL(4),
		EVIL_THREAT(5),
		STANDARD_2(3),
		JEWEL_UTOPIA(4),
		IRON_FLEET(5);

		public final int cost;

		PackType(int cost) {
			this.cost = cost;
		}
	}

	private final AppUserMapper appUserMapper;
	private final UserCollectionMapper userCollectionMapper;
	private final CardCatalogService cardCatalogService;
	private final MissionService missionService;

	@Transactional
	public List<CardDefinition> openPack(long userId) {
		return openPack(userId, PackType.STANDARD);
	}

	@Transactional
	public List<CardDefinition> openPack(long userId, PackType type) {
		AppUser u = appUserMapper.findById(userId);
		if (u == null) {
			throw new IllegalStateException("ユーザーが見つかりません");
		}
		PackType t = type != null ? type : PackType.STANDARD;
		if (u.getCoins() < t.cost) {
			throw new IllegalArgumentException("ジェムが足りません（" + t.cost + "ジェム必要）");
		}
		appUserMapper.updateCoins(userId, u.getCoins() - t.cost);
		return pullPackIntoCollection(userId, t, true);
	}

	/**
	 * ホームの時間ゲージなど：ジェムを消費せず、スタンダードと同じ排出で開封する。
	 */
	@Transactional
	public List<CardDefinition> openStandardPackWithoutGemCost(long userId) {
		return pullPackIntoCollection(userId, PackType.STANDARD, false);
	}

	/**
	 * 新規登録プレゼントの「スタンダードパック1」を1パック開封（残数を1減らしてから、ジェム消費なしで {@link PackType#STANDARD} と同じ排出）。
	 */
	@Transactional
	public List<CardDefinition> openStarterGiftStandard1Pack(long userId) {
		int n = appUserMapper.decrementStarterGiftStandard1IfPositive(userId);
		if (n == 0) {
			throw new IllegalArgumentException("開封できるプレゼントのスタンダードパック1がありません");
		}
		return openStandardPackWithoutGemCost(userId);
	}

	private List<CardDefinition> pullPackIntoCollection(long userId, PackType t, boolean paidWithGems) {
		Random rnd = new Random();
		List<CardDefinition> pulled = new ArrayList<>();
		List<CardDefinition> all = filterCardsForPack(cardCatalogService.all(), t);
		if (all.isEmpty()) {
			all = cardCatalogService.all();
		}
		for (int i = 0; i < GameConstants.PACK_CARD_COUNT; i++) {
			CardDefinition c = pickWeightedByRarity(all, rnd);
			userCollectionMapper.upsertAdd(userId, c.getId(), 1);
			pulled.add(c);
		}
		if (paidWithGems) {
			missionService.onPaidPackOpened(userId);
		} else {
			missionService.onBonusPackOpened(userId);
		}
		return pulled;
	}

	/**
	 * 購入画面の「詳細」用：開封と同じ集合をレア度降順→名前で並べた一覧。
	 */
	public List<CardDefinition> sortedEligibleCardsForPreview(PackType type) {
		List<CardDefinition> list = new ArrayList<>(eligibleCardsForPack(type));
		list.sort(Comparator
				.comparingInt(PackService::raritySortKey)
				.thenComparing(c -> c.getName() != null ? c.getName() : "", String.CASE_INSENSITIVE_ORDER));
		return list;
	}

	public List<CardDefinition> eligibleCardsForPack(PackType type) {
		List<CardDefinition> all = filterCardsForPack(cardCatalogService.all(), type);
		if (all.isEmpty()) {
			return new ArrayList<>(cardCatalogService.all());
		}
		return all;
	}

	private static int raritySortKey(CardDefinition c) {
		String r = c != null ? c.getRarity() : null;
		if (r == null || r.isBlank()) {
			return 3;
		}
		return switch (r.trim()) {
			case "Reg" -> 0;
			case "Ep" -> 1;
			case "R" -> 2;
			case "C" -> 3;
			default -> 4;
		};
	}

	private static List<CardDefinition> filterCardsForPack(List<CardDefinition> all, PackType type) {
		if (all == null || all.isEmpty()) return List.of();
		PackType t = type != null ? type : PackType.STANDARD;
		List<CardDefinition> out = new ArrayList<>();
		// 「スタンダードパック1」は WINDY_HILL / EVIL_THREAT の収録カードも排出対象。
		// ただし将来パックのカードは混ざらないよう、明示的に STD/WH/ET のみ対象にする。
		List<String> wants = switch (t) {
			case STANDARD -> List.of("STD", "WH", "ET");
			case WINDY_HILL -> List.of("WH");
			case EVIL_THREAT -> List.of("ET");
			case STANDARD_2 -> List.of("JU", "IF");
			case JEWEL_UTOPIA -> List.of("JU");
			case IRON_FLEET -> List.of("IF");
		};
		for (CardDefinition c : all) {
			if (c == null) continue;
			String pi = c.getPackInitial();
			String got = (pi != null && !pi.isBlank()) ? pi.trim().toUpperCase() : "STD";
			if (!wants.contains(got)) continue;
			out.add(c);
		}
		return out;
	}

	private static CardDefinition pickWeightedByRarity(List<CardDefinition> all, Random rnd) {
		if (all == null || all.isEmpty()) {
			throw new IllegalStateException("カード定義が空です");
		}
		String target = rollRarity(rnd);
		List<CardDefinition> pool = new ArrayList<>();
		for (CardDefinition c : all) {
			String r = c != null ? c.getRarity() : null;
			if (r == null || r.isBlank()) {
				r = "C";
			}
			if (target.equalsIgnoreCase(r.trim())) {
				pool.add(c);
			}
		}
		List<CardDefinition> pickFrom = pool.isEmpty() ? all : pool;
		return pickFrom.get(rnd.nextInt(pickFrom.size()));
	}

	/**
	 * 排出率: Reg 2% / Ep 10% / R 30% / C 58%
	 */
	private static String rollRarity(Random rnd) {
		int x = rnd.nextInt(100); // 0..99
		if (x < 2) return "Reg";
		if (x < 12) return "Ep";
		if (x < 42) return "R";
		return "C";
	}
}
