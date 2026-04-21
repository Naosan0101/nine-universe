package com.example.nineuniverse.repository;

import com.example.nineuniverse.domain.UserCollectionRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserCollectionMapper {
	List<UserCollectionRow> findByUserId(@Param("userId") long userId);

	Integer findQuantity(@Param("userId") long userId, @Param("cardId") short cardId);

	int upsertAdd(@Param("userId") long userId, @Param("cardId") short cardId, @Param("delta") int delta);

	int deleteByUserId(@Param("userId") long userId);

	int insertQuantity(@Param("userId") long userId, @Param("cardId") short cardId, @Param("quantity") int quantity);

	/** 所持が十分なときだけ減算。更新行数 1 で成功。 */
	int subtractQuantityIfEnough(@Param("userId") long userId, @Param("cardId") short cardId, @Param("delta") int delta);

	int deleteZeroQuantityRow(@Param("userId") long userId, @Param("cardId") short cardId);
}
