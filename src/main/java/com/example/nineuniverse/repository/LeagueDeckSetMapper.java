package com.example.nineuniverse.repository;

import com.example.nineuniverse.domain.LeagueDeckSet;
import com.example.nineuniverse.domain.LeagueDeckSetSummary;
import java.util.List;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

public interface LeagueDeckSetMapper {

	List<LeagueDeckSet> findByUserId(@Param("userId") long userId);

	LeagueDeckSet findByIdAndUserId(@Param("id") long id, @Param("userId") long userId);

	LeagueDeckSetSummary findSummaryByIdAndUserId(@Param("id") long id, @Param("userId") long userId);

	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(LeagueDeckSet row);

	int updateName(@Param("id") long id, @Param("userId") long userId, @Param("name") String name);

	int delete(@Param("id") long id, @Param("userId") long userId);
}
