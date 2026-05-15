package com.example.nineuniverse.service;

import com.example.nineuniverse.domain.Deck;
import com.example.nineuniverse.domain.DeckEntry;
import com.example.nineuniverse.domain.LeagueDeckSet;
import com.example.nineuniverse.domain.LeagueDeckSetSummary;
import com.example.nineuniverse.repository.DeckEntryMapper;
import com.example.nineuniverse.repository.DeckMapper;
import com.example.nineuniverse.repository.LeagueDeckSetMapper;
import com.example.nineuniverse.repository.UserCollectionMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeckService {

	private final DeckMapper deckMapper;
	private final DeckEntryMapper deckEntryMapper;
	private final UserCollectionMapper userCollectionMapper;
	private final LeagueDeckSetMapper leagueDeckSetMapper;

	/** カジュアル用のデッキ一覧（リーグ用の子デッキは含まない） */
	public List<Deck> listCasualDecks(long userId) {
		return deckMapper.findCasualByUserId(userId);
	}

	/** @deprecated 互換のため。{@link #listCasualDecks(long)} と同じ */
	public List<Deck> listDecks(long userId) {
		return listCasualDecks(userId);
	}

	public List<LeagueDeckSetSummary> listLeagueDeckSetSummaries(long userId) {
		List<LeagueDeckSet> sets = leagueDeckSetMapper.findByUserId(userId);
		List<LeagueDeckSetSummary> out = new ArrayList<>();
		for (LeagueDeckSet s : sets) {
			LeagueDeckSetSummary sum = leagueDeckSetMapper.findSummaryByIdAndUserId(s.getId(), userId);
			if (sum != null) {
				out.add(sum);
			}
		}
		return out;
	}

	public LeagueDeckSet requireLeagueSet(long userId, long setId) {
		LeagueDeckSet s = leagueDeckSetMapper.findByIdAndUserId(setId, userId);
		if (s == null) {
			throw new IllegalArgumentException("リーグデッキが見つかりません");
		}
		return s;
	}

	public LeagueDeckSetSummary requireLeagueSummary(long userId, long setId) {
		LeagueDeckSetSummary sum = leagueDeckSetMapper.findSummaryByIdAndUserId(setId, userId);
		if (sum == null) {
			throw new IllegalArgumentException("リーグデッキが見つかりません");
		}
		return sum;
	}

	public Deck requireDeck(long userId, long deckId) {
		Deck d = deckMapper.findByIdAndUserId(deckId, userId);
		if (d == null) {
			throw new IllegalArgumentException("デッキが見つかりません");
		}
		return d;
	}

	/** リーグ用スロットの表示名（未設定時は「デッキ1」「デッキ2」） */
	public static String leagueDeckSlotDisplayName(String storedName, int slot1Or2) {
		if (storedName != null && !storedName.isBlank()) {
			return storedName.trim();
		}
		return slot1Or2 == 2 ? "デッキ2" : "デッキ1";
	}

	/**
	 * リーグ子デッキがまだユーザー命名されていないか（空、または旧自動生成名「…・デッキ1/2」）。
	 */
	public boolean isLeagueDeckNameUnset(Deck deck) {
		if (deck == null || deck.getLeagueSlot() == null) {
			return false;
		}
		String name = deck.getName();
		if (name == null || name.isBlank()) {
			return true;
		}
		int slot = deck.getLeagueSlot();
		return (slot == 1 && name.endsWith("・デッキ1")) || (slot == 2 && name.endsWith("・デッキ2"));
	}

	/** デッキ編集画面のデッキ名欄（未命名のリーグ子デッキはカジュアル新規作成と同様に空） */
	public String deckNameForLeagueEditForm(Deck deck) {
		if (isLeagueDeckNameUnset(deck)) {
			return "";
		}
		return deck.getName() != null ? deck.getName() : "";
	}

	public List<DeckEntry> entries(long deckId) {
		return deckEntryMapper.findByDeckId(deckId);
	}

	@Transactional
	public long createDeck(long userId, String name, List<Short> cardIds) {
		validateEight(cardIds, userId);
		Deck d = new Deck();
		d.setUserId(userId);
		d.setName(name.trim().isEmpty() ? "マイデッキ" : name.trim());
		d.setLeagueSetId(null);
		d.setLeagueSlot(null);
		deckMapper.insert(d);
		if (d.getId() == null) {
			throw new IllegalStateException("デッキIDの採番に失敗しました（DB設定を確認してください）");
		}
		saveEntries(d.getId(), cardIds);
		return d.getId();
	}

	/**
	 * リーグデッキセットを新規作成する。デッキ1・2は空のままなので、あとからそれぞれ8枚ずつ編集する。
	 */
	@Transactional
	public LeagueDeckSetSummary createLeagueDeckSet(long userId, String setName) {
		String base = setName == null || setName.trim().isEmpty() ? "リーグデッキ" : setName.trim();
		LeagueDeckSet row = new LeagueDeckSet();
		row.setUserId(userId);
		row.setName(base);
		leagueDeckSetMapper.insert(row);
		if (row.getId() == null) {
			throw new IllegalStateException("リーグデッキセットIDの採番に失敗しました");
		}
		Deck d1 = new Deck();
		d1.setUserId(userId);
		d1.setName("");
		d1.setLeagueSetId(row.getId());
		d1.setLeagueSlot(1);
		deckMapper.insert(d1);
		Deck d2 = new Deck();
		d2.setUserId(userId);
		d2.setName("");
		d2.setLeagueSetId(row.getId());
		d2.setLeagueSlot(2);
		deckMapper.insert(d2);
		if (d1.getId() == null || d2.getId() == null) {
			throw new IllegalStateException("デッキIDの採番に失敗しました");
		}
		saveEntries(d1.getId(), List.of());
		saveEntries(d2.getId(), List.of());
		LeagueDeckSetSummary sum = leagueDeckSetMapper.findSummaryByIdAndUserId(row.getId(), userId);
		if (sum == null) {
			throw new IllegalStateException("リーグデッキの読み取りに失敗しました");
		}
		return sum;
	}

	@Transactional
	public void updateLeagueSetName(long userId, long setId, String name) {
		requireLeagueSet(userId, setId);
		String n = name == null || name.trim().isEmpty() ? "リーグデッキ" : name.trim();
		leagueDeckSetMapper.updateName(setId, userId, n);
	}

	@Transactional
	public void deleteLeagueSet(long userId, long setId) {
		requireLeagueSet(userId, setId);
		leagueDeckSetMapper.delete(setId, userId);
	}

	@Transactional
	public void updateDeck(long userId, long deckId, String name, List<Short> cardIds) {
		Deck self = requireDeck(userId, deckId);
		validateEight(cardIds, userId);
		validateLeagueSiblingDisjoint(self, cardIds);
		String resolvedName;
		if (self.getLeagueSetId() != null) {
			resolvedName = name == null ? "" : name.trim();
		} else {
			resolvedName = name.trim().isEmpty() ? "マイデッキ" : name.trim();
		}
		deckMapper.updateName(deckId, userId, resolvedName);
		deckEntryMapper.deleteByDeckId(deckId);
		saveEntries(deckId, cardIds);
	}

	private void validateLeagueSiblingDisjoint(Deck self, List<Short> cardIds) {
		if (self.getLeagueSetId() == null || self.getLeagueSlot() == null) {
			return;
		}
		int sibSlot = self.getLeagueSlot() == 1 ? 2 : 1;
		Long sibId = deckMapper.findIdByLeagueSetAndSlot(self.getLeagueSetId(), sibSlot);
		if (sibId == null) {
			return;
		}
		List<Short> sibCards = cardIdsForDeck(sibId);
		Set<Short> mine = new HashSet<>(cardIds);
		for (Short c : sibCards) {
			if (mine.contains(c)) {
				throw new IllegalArgumentException(
						"リーグデッキでは、片方のデッキに入れたカードはもう片方のデッキに入れられません");
			}
		}
	}

	public void requireLeagueSetBattleReady(long userId, long leagueSetId) {
		requireLeagueSet(userId, leagueSetId);
		long d1 = deckIdForLeagueSlot(userId, leagueSetId, 1);
		long d2 = deckIdForLeagueSlot(userId, leagueSetId, 2);
		List<Short> c1 = cardIdsForDeck(d1);
		List<Short> c2 = cardIdsForDeck(d2);
		if (c1.size() != 8 || c2.size() != 8) {
			throw new IllegalArgumentException("リーグデッキのデッキ1・デッキ2を、それぞれ8枚ずつ編集してください");
		}
		Set<Short> s1 = new HashSet<>(c1);
		for (Short x : c2) {
			if (s1.contains(x)) {
				throw new IllegalArgumentException(
						"リーグデッキでは、片方のデッキに入れたカードはもう片方のデッキに入れられません");
			}
		}
	}

	private void saveEntries(long deckId, List<Short> cardIds) {
		for (int i = 0; i < cardIds.size(); i++) {
			deckEntryMapper.insert(deckId, (short) (i + 1), cardIds.get(i));
		}
	}

	@Transactional
	public void deleteDeck(long userId, long deckId) {
		Deck d = requireDeck(userId, deckId);
		if (d.getLeagueSetId() != null) {
			throw new IllegalArgumentException("リーグデッキの子デッキはセットごと削除してください");
		}
		deckMapper.delete(deckId, userId);
	}

	private void validateEight(List<Short> cardIds, long userId) {
		if (cardIds == null || cardIds.size() != 8) {
			throw new IllegalArgumentException("デッキは8枚必要です");
		}
		Map<Short, Integer> inDeck = new HashMap<>();
		for (Short id : cardIds) {
			inDeck.merge(id, 1, Integer::sum);
			if (inDeck.get(id) > 2) {
				throw new IllegalArgumentException("同じカードは2枚までです");
			}
			int owned = userCollectionMapper.findQuantity(userId, id);
			if (owned < inDeck.get(id)) {
				throw new IllegalArgumentException("所有枚数を超えています");
			}
		}
	}

	public List<Short> cardIdsForDeck(long deckId) {
		return deckEntryMapper.findByDeckId(deckId).stream().map(DeckEntry::getCardId).toList();
	}

	public long deckIdForLeagueSlot(long userId, long leagueSetId, int slot1Or2) {
		requireLeagueSet(userId, leagueSetId);
		Long id = deckMapper.findIdByLeagueSetAndSlot(leagueSetId, slot1Or2);
		if (id == null) {
			throw new IllegalStateException("リーグデッキの構成が不正です");
		}
		return id;
	}
}
