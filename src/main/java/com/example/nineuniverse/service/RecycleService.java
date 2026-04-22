package com.example.nineuniverse.service;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.domain.CardIdCount;
import com.example.nineuniverse.domain.DeckEntry;
import com.example.nineuniverse.domain.LibraryCardView;
import com.example.nineuniverse.domain.UserCollectionRow;
import com.example.nineuniverse.card.CardAttributes;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.repository.DeckEntryMapper;
import com.example.nineuniverse.repository.UserCollectionMapper;
import com.example.nineuniverse.web.dto.RecycleInventoryLine;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecycleService {

	private final AppUserMapper appUserMapper;
	private final UserCollectionMapper userCollectionMapper;
	private final DeckEntryMapper deckEntryMapper;
	private final CardCatalogService cardCatalogService;
	private final LibraryService libraryService;
	private final PackService packService;

	public static int crystalPerRarity(String rarity) {
		if (rarity == null || rarity.isBlank()) {
			return GameConstants.RECYCLE_CRYSTAL_PER_CARD_C;
		}
		return switch (rarity.trim()) {
			case "Reg" -> GameConstants.RECYCLE_CRYSTAL_PER_CARD_REG;
			case "Ep" -> GameConstants.RECYCLE_CRYSTAL_PER_CARD_EP;
			case "R" -> GameConstants.RECYCLE_CRYSTAL_PER_CARD_R;
			default -> GameConstants.RECYCLE_CRYSTAL_PER_CARD_C;
		};
	}

	public List<RecycleInventoryLine> recycleInventory(long userId) {
		Map<Short, Integer> qty = userCollectionMapper.findByUserId(userId).stream()
				.collect(Collectors.toMap(r -> r.getCardId(), r -> r.getQuantity()));
		Map<Short, Integer> deck = new HashMap<>();
		for (CardIdCount cc : deckEntryMapper.countCardsInUserDecks(userId)) {
			deck.put(cc.getCardId(), cc.getCopies());
		}
		List<RecycleInventoryLine> out = new ArrayList<>();
		for (CardDefinition c : cardCatalogService.all()) {
			int owned = qty.getOrDefault(c.getId(), 0);
			int inDecks = deck.getOrDefault(c.getId(), 0);
			int recyclable = Math.max(0, owned);
			LibraryCardView v = libraryService.viewForCardDefinition(c);
			v.setQuantity(owned);
			v.setOwned(owned > 0);
			RecycleInventoryLine line = new RecycleInventoryLine();
			line.setCard(v);
			line.setOwned(owned);
			line.setInDecks(inDecks);
			line.setRecyclable(recyclable);
			line.setCrystalPerCard(crystalPerRarity(c.getRarity()));
			out.add(line);
		}
		out.sort(Comparator
				.comparingInt((RecycleInventoryLine x) -> x.getCard().getCost())
				.thenComparing((RecycleInventoryLine x) -> CardAttributes.primarySegment(x.getCard().getAttribute()))
				.thenComparing(x -> x.getCard().getAttribute())
				.thenComparingInt(x -> x.getCard().getBasePower())
				.thenComparing(x -> x.getCard().getName()));
		return out;
	}

	/**
	 * 各カードについてコレクション上 2 枚を残し、それを超える分をリサイクルする。
	 * デッキにしかない分は {@link #recycleCards} 側で枠の差し替えにより処理する。
	 */
	@Transactional
	public int recycleSurplusKeepingTwoPerCard(long userId) {
		Map<Short, Integer> req = new HashMap<>();
		for (RecycleInventoryLine line : recycleInventory(userId)) {
			int owned = line.getOwned();
			int toRecycle = Math.max(0, owned - 2);
			if (toRecycle > 0) {
				Short idObj = line.getCard().getId();
				if (idObj == null) {
					continue;
				}
				short cardId = idObj;
				req.put(cardId, toRecycle);
			}
		}
		if (req.isEmpty()) {
			throw new IllegalArgumentException("変換する余剰カードがありません。");
		}
		return recycleCards(userId, req);
	}

	@Transactional
	public int recycleCards(long userId, Map<Short, Integer> requested) {
		if (requested == null || requested.isEmpty()) {
			throw new IllegalArgumentException("リサイクルする枚数を指定してください。");
		}
		Map<Short, CardDefinition> catalog = cardCatalogService.mapById();
		Map<Short, Integer> ownedCounts = new HashMap<>();
		for (UserCollectionRow row : userCollectionMapper.findByUserId(userId)) {
			if (row.getCardId() != null && row.getQuantity() != null && row.getQuantity() > 0) {
				ownedCounts.put(row.getCardId(), row.getQuantity());
			}
		}
		Map<Short, Integer> deckCounts = new HashMap<>();
		for (CardIdCount cc : deckEntryMapper.countCardsInUserDecks(userId)) {
			deckCounts.put(cc.getCardId(), cc.getCopies());
		}
		int totalCrystal = 0;
		List<int[]> ops = new ArrayList<>();
		for (Map.Entry<Short, Integer> e : new TreeMap<>(requested).entrySet()) {
			if (e.getKey() == null || e.getValue() == null) {
				continue;
			}
			short cardId = e.getKey();
			int q = e.getValue();
			if (q <= 0) {
				continue;
			}
			CardDefinition def = catalog.get(cardId);
			if (def == null) {
				throw new IllegalArgumentException("不明なカードです。");
			}
			int owned = ownedCounts.getOrDefault(cardId, 0);
			if (q > owned) {
				throw new IllegalArgumentException(
						(def.getName() != null ? def.getName() : "カード") + " は所持 " + owned + " 枚のため、最大 " + owned + " 枚までリサイクルできます。");
			}
			int inD = deckCounts.getOrDefault(cardId, 0);
			int notInDecks = Math.max(0, owned - inD);
			int fromDeck = Math.max(0, q - notInDecks);
			for (int i = 0; i < fromDeck; i++) {
				DeckEntry slot = deckEntryMapper.findFirstSlotByUserAndCard(userId, cardId);
				if (slot == null || slot.getDeckId() == null || slot.getSlot() == null) {
					throw new IllegalStateException("リサイクル処理に失敗しました（デッキ枠が見つかりません）。");
				}
				short replacement = pickReplacementCardId(slot.getDeckId(), cardId, ownedCounts, deckCounts);
				if (replacement <= 0) {
					throw new IllegalArgumentException(
							"デッキに入っているカードをクリスタルに変換するには、デッキに入れていない余剰の別カードが少なくとも1枚必要です。"
									+ "デッキ編集で該当カードを他のカードと入れ替えてから、再度お試しください。");
				}
				deckEntryMapper.updateSlotCard(slot.getDeckId(), slot.getSlot(), replacement);
				deckCounts.merge(cardId, -1, Integer::sum);
				deckCounts.merge(replacement, 1, Integer::sum);
			}
			totalCrystal += crystalPerRarity(def.getRarity()) * q;
			ops.add(new int[] { cardId, q });
		}
		if (ops.isEmpty()) {
			throw new IllegalArgumentException("リサイクルする枚数を指定してください。");
		}
		for (int[] op : ops) {
			short cardId = (short) op[0];
			int q = op[1];
			int u = userCollectionMapper.subtractQuantityIfEnough(userId, cardId, q);
			if (u != 1) {
				throw new IllegalStateException("リサイクル処理に失敗しました（所持枚数が足りません）。");
			}
			userCollectionMapper.deleteZeroQuantityRow(userId, cardId);
			ownedCounts.merge(cardId, -q, Integer::sum);
		}
		appUserMapper.addRecycleCrystalDelta(userId, totalCrystal);
		return totalCrystal;
	}

	/**
	 * デッキ {@code deckId} の1枠を、{@code recyclingCardId} 以外のカードで埋める。
	 * 「所持 − 全デッキ枚数」が1以上かつ、そのデッキ内では同一カード2枚まで、を満たすID最小のカードを選ぶ。
	 */
	private short pickReplacementCardId(
			long deckId,
			short recyclingCardId,
			Map<Short, Integer> ownedCounts,
			Map<Short, Integer> deckCounts) {
		List<CardDefinition> defs = new ArrayList<>(cardCatalogService.all());
		defs.sort(Comparator.comparingInt(CardDefinition::getId));
		for (CardDefinition d : defs) {
			short id = d.getId();
			if (id == recyclingCardId) {
				continue;
			}
			int spare = ownedCounts.getOrDefault(id, 0) - deckCounts.getOrDefault(id, 0);
			if (spare < 1) {
				continue;
			}
			if (deckEntryMapper.countInDeck(deckId, id) >= 2) {
				continue;
			}
			return id;
		}
		return 0;
	}

	@Transactional
	public List<CardDefinition> openEpicPlusRecyclePack(long userId, PackService.PackType standardPack) {
		requireRecycleStandardPack(standardPack);
		int n = appUserMapper.subtractRecycleCrystalIfEnough(userId, GameConstants.RECYCLE_SHOP_EPIC_PLUS_PACK_CRYSTAL);
		if (n != 1) {
			throw new IllegalArgumentException("クリスタルが足りません（" + GameConstants.RECYCLE_SHOP_EPIC_PLUS_PACK_CRYSTAL + "必要）。");
		}
		return pullRecycleAssuredFourPack(userId, standardPack, true);
	}

	@Transactional
	public List<CardDefinition> openLegendaryRecyclePack(long userId, PackService.PackType standardPack) {
		requireRecycleStandardPack(standardPack);
		int n = appUserMapper.subtractRecycleCrystalIfEnough(userId, GameConstants.RECYCLE_SHOP_LEGENDARY_PACK_CRYSTAL);
		if (n != 1) {
			throw new IllegalArgumentException("クリスタルが足りません（" + GameConstants.RECYCLE_SHOP_LEGENDARY_PACK_CRYSTAL + "必要）。");
		}
		return pullRecycleAssuredFourPack(userId, standardPack, false);
	}

	private void requireRecycleStandardPack(PackService.PackType standardPack) {
		if (!packService.isRecycleStandardBundlePack(standardPack)) {
			throw new IllegalArgumentException("スタンダードパックの指定が不正です。");
		}
	}

	/**
	 * 1〜3枚目: レジェンダリーなし。Ep 10% / R 30% / C 60%。4枚目: レジェパックはレジェ確定、エピック以上パックはレジェ10%・エピック90%。
	 */
	private List<CardDefinition> pullRecycleAssuredFourPack(
			long userId, PackService.PackType standardPack, boolean epicPlusLastSlot) {
		List<CardDefinition> pool = new ArrayList<>(packService.eligibleCardsForPack(standardPack));
		if (pool.isEmpty()) {
			throw new IllegalStateException("排出対象のカード定義がありません。");
		}
		Random rnd = new Random();
		List<CardDefinition> pulled = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			CardDefinition c = pickRecycleNonLegendarySlot(pool, rnd);
			userCollectionMapper.upsertAdd(userId, c.getId(), 1);
			pulled.add(c);
		}
		CardDefinition fourth = epicPlusLastSlot ? pickRecycleFourthEpicOrLegendary(pool, rnd) : pickRecycleFourthLegendary(pool, rnd);
		userCollectionMapper.upsertAdd(userId, fourth.getId(), 1);
		pulled.add(fourth);
		return pulled;
	}

	private static String normRarity(CardDefinition c) {
		String r = c != null ? c.getRarity() : null;
		if (r == null || r.isBlank()) {
			return "C";
		}
		return r.trim();
	}

	private static List<CardDefinition> filterRarityIgnoreCase(List<CardDefinition> pool, String rarityCode) {
		List<CardDefinition> out = new ArrayList<>();
		for (CardDefinition c : pool) {
			if (rarityCode.equalsIgnoreCase(normRarity(c))) {
				out.add(c);
			}
		}
		return out;
	}

	private static List<CardDefinition> withoutLegendary(List<CardDefinition> pool) {
		List<CardDefinition> out = new ArrayList<>();
		for (CardDefinition c : pool) {
			if (!"Reg".equalsIgnoreCase(normRarity(c))) {
				out.add(c);
			}
		}
		return out;
	}

	/** リサイクル確定パックの1〜3枚目: Ep 10% / R 30% / C 60%（レジェなし）。 */
	private static String rollRecycleThreeSlotRarity(Random rnd) {
		int x = rnd.nextInt(100);
		if (x < 10) {
			return "Ep";
		}
		if (x < 40) {
			return "R";
		}
		return "C";
	}

	private CardDefinition pickRecycleNonLegendarySlot(List<CardDefinition> pool, Random rnd) {
		List<CardDefinition> noLeg = withoutLegendary(pool);
		if (noLeg.isEmpty()) {
			throw new IllegalStateException("排出対象のカード定義が不足しています。");
		}
		String preferred = rollRecycleThreeSlotRarity(rnd);
		List<String> order = new ArrayList<>();
		order.add(preferred);
		for (String t : List.of("Ep", "R", "C")) {
			if (!t.equalsIgnoreCase(preferred)) {
				order.add(t);
			}
		}
		for (String tier : order) {
			List<CardDefinition> sub = filterRarityIgnoreCase(noLeg, tier);
			if (!sub.isEmpty()) {
				return sub.get(rnd.nextInt(sub.size()));
			}
		}
		return noLeg.get(rnd.nextInt(noLeg.size()));
	}

	private static CardDefinition pickUniformIfNonEmpty(List<CardDefinition> sub, Random rnd) {
		if (sub == null || sub.isEmpty()) {
			return null;
		}
		return sub.get(rnd.nextInt(sub.size()));
	}

	private static CardDefinition pickRecycleFourthLegendary(List<CardDefinition> pool, Random rnd) {
		List<CardDefinition> regs = filterRarityIgnoreCase(pool, "Reg");
		if (regs.isEmpty()) {
			throw new IllegalStateException("このパックにレジェンダリーが存在しません。");
		}
		return regs.get(rnd.nextInt(regs.size()));
	}

	/** 4枚目: レジェ 10% / エピック 90%。片方が空ならもう一方にフォールバック。 */
	private static CardDefinition pickRecycleFourthEpicOrLegendary(List<CardDefinition> pool, Random rnd) {
		List<CardDefinition> regs = filterRarityIgnoreCase(pool, "Reg");
		List<CardDefinition> eps = filterRarityIgnoreCase(pool, "Ep");
		boolean wantLegendary = rnd.nextInt(100) < 10;
		if (wantLegendary) {
			CardDefinition c = pickUniformIfNonEmpty(regs, rnd);
			if (c != null) {
				return c;
			}
			c = pickUniformIfNonEmpty(eps, rnd);
			if (c != null) {
				return c;
			}
		} else {
			CardDefinition c = pickUniformIfNonEmpty(eps, rnd);
			if (c != null) {
				return c;
			}
			c = pickUniformIfNonEmpty(regs, rnd);
			if (c != null) {
				return c;
			}
		}
		throw new IllegalStateException("このパックにエピック以上が存在しません。");
	}

	@Transactional
	public void claimLegendaryPick(long userId, short cardId) {
		int n = appUserMapper.subtractRecycleCrystalIfEnough(userId, GameConstants.RECYCLE_SHOP_LEGENDARY_PICK_CRYSTAL);
		if (n != 1) {
			throw new IllegalArgumentException("クリスタルが足りません（" + GameConstants.RECYCLE_SHOP_LEGENDARY_PICK_CRYSTAL + "必要）。");
		}
		CardDefinition c = cardCatalogService.mapById().get(cardId);
		if (c == null) {
			throw new IllegalArgumentException("不明なカードです。");
		}
		String r = c.getRarity();
		if (r == null || !"Reg".equalsIgnoreCase(r.trim())) {
			throw new IllegalArgumentException("レジェンダリーカードを選んでください。");
		}
		userCollectionMapper.upsertAdd(userId, cardId, 1);
	}

	/**
	 * 固定レート（{@link GameConstants#RECYCLE_CRYSTAL_PER_GEM}）のクリスタルを1ジェムに交換する（1回につき1ジェム）。
	 */
	@Transactional
	public void exchangeThousandCrystalForOneGem(long userId) {
		int n = appUserMapper.subtractRecycleCrystalIfEnough(userId, GameConstants.RECYCLE_CRYSTAL_PER_GEM);
		if (n != 1) {
			throw new IllegalArgumentException(
					"クリスタルが足りません（" + GameConstants.RECYCLE_CRYSTAL_PER_GEM + "必要）。");
		}
		appUserMapper.addCoinsDelta(userId, 1);
	}
}
