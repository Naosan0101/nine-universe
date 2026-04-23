package com.example.nineuniverse.repository;

import com.example.nineuniverse.domain.PvpFriendInvite;
import com.example.nineuniverse.domain.PvpInviteNotice;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

public interface PvpFriendInviteMapper {

	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(PvpFriendInvite row);

	PvpFriendInvite findById(@Param("id") long id);

	PvpFriendInvite findPendingByMatchId(@Param("matchId") String matchId);

	List<PvpInviteNotice> findPendingInboundForUser(@Param("opponentUserId") long opponentUserId);

	List<PvpInviteNotice> findPendingOutboundForUser(@Param("challengerUserId") long challengerUserId);

	int countPendingInboundForUser(@Param("opponentUserId") long opponentUserId);

	int updateStatus(@Param("id") long id, @Param("status") String status);

	Long findInviteIdByMatchAndChallenger(@Param("matchId") String matchId, @Param("challengerUserId") long challengerUserId);

	List<PvpFriendInvite> findPendingInvitesCreatedBefore(@Param("cutoff") Instant cutoff);

	/** まだ PENDING かつ created_at が cutoff より前なら EXPIRED にする。更新行数 0 なら他処理済み。 */
	int expireByIdIfPending(@Param("id") long id, @Param("cutoff") Instant cutoff);
}
