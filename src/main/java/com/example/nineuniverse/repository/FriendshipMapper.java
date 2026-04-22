package com.example.nineuniverse.repository;

import com.example.nineuniverse.domain.FriendListRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FriendshipMapper {

	int insert(@Param("userLowId") long userLowId, @Param("userHighId") long userHighId);

	boolean existsPair(@Param("userA") long userA, @Param("userB") long userB);

	List<FriendListRow> listFriendsForUser(@Param("userId") long userId);
}
