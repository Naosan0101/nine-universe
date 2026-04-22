package com.example.nineuniverse.repository;

import com.example.nineuniverse.domain.NicknameEpithet;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NicknameEpithetMapper {

	String findPhraseById(@Param("id") long id);

	List<NicknameEpithet> listOwnedByKind(@Param("userId") long userId, @Param("kind") String kind);

	List<Long> listUnownedIdsByKind(@Param("userId") long userId, @Param("kind") String kind);

	int insertOwned(@Param("userId") long userId, @Param("epithetId") long epithetId);

	int countOwnedByKind(@Param("userId") long userId, @Param("kind") String kind);

	int countCatalogByKind(@Param("kind") String kind);

	boolean userOwns(@Param("userId") long userId, @Param("epithetId") long epithetId);

	/** デフォルトのビギナー二つ名を入手済みにする（新規ユーザー登録直後） */
	int grantBeginnerOwned(@Param("userId") long userId);
}
