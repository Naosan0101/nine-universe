package com.example.nineuniverse.repository;

import com.example.nineuniverse.domain.FriendInboundNotice;
import com.example.nineuniverse.domain.FriendRequest;
import java.util.List;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

public interface FriendRequestMapper {

	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(FriendRequest row);

	FriendRequest findById(@Param("id") long id);

	List<FriendInboundNotice> findPendingInboundForUser(@Param("toUserId") long toUserId);

	int countPendingInboundForUser(@Param("toUserId") long toUserId);

	FriendRequest findPendingFromTo(@Param("fromUserId") long fromUserId, @Param("toUserId") long toUserId);

	int updateStatus(@Param("id") long id, @Param("status") String status);

	int deletePendingBetween(@Param("userA") long userA, @Param("userB") long userB);
}
