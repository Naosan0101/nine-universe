package com.example.nineuniverse.repository;

import com.example.nineuniverse.domain.CardIdCount;
import com.example.nineuniverse.domain.DeckEntry;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DeckEntryMapper {
	List<DeckEntry> findByDeckId(@Param("deckId") long deckId);

	/** ユーザーの全デッキに入っている各カードの枚数 */
	List<CardIdCount> countCardsInUserDecks(@Param("userId") long userId);

	int deleteByDeckId(@Param("deckId") long deckId);

	int insert(@Param("deckId") long deckId, @Param("slot") short slot, @Param("cardId") short cardId);
}
