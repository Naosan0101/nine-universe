package com.example.nineuniverse.repository;

import com.example.nineuniverse.domain.Deck;
import java.util.List;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

public interface DeckMapper {
	List<Deck> findByUserId(@Param("userId") long userId);

	/** カジュアルデッキのみ（リーグ用の子デッキ行は含まない） */
	List<Deck> findCasualByUserId(@Param("userId") long userId);

	Deck findByIdAndUserId(@Param("id") long id, @Param("userId") long userId);

	Long findIdByLeagueSetAndSlot(@Param("leagueSetId") long leagueSetId, @Param("slot") int slot);

	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(Deck deck);

	int updateName(@Param("id") long id, @Param("userId") long userId, @Param("name") String name);

	int delete(@Param("id") long id, @Param("userId") long userId);
}
