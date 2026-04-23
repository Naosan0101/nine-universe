package com.example.nineuniverse.service;

import com.example.nineuniverse.domain.PvpFriendInvite;
import com.example.nineuniverse.domain.PvpInviteNotice;
import com.example.nineuniverse.repository.PvpFriendInviteMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PvpFriendInviteService {

	/** 相手が承諾しないまま経過したら招待と待機ルームを無効化するまでの時間 */
	public static final Duration PENDING_INVITE_TTL = Duration.ofMinutes(3);

	public record PvpInviteCreated(long inviteId, String matchId) {
	}

	private final PvpFriendInviteMapper pvpFriendInviteMapper;
	private final PvpBattleService pvpBattleService;
	private final FriendService friendService;

	public int countPendingInbound(long userId) {
		return pvpFriendInviteMapper.countPendingInboundForUser(userId);
	}

	public List<PvpInviteNotice> listPendingInbound(long userId) {
		return pvpFriendInviteMapper.findPendingInboundForUser(userId);
	}

	public List<PvpInviteNotice> listPendingOutbound(long userId) {
		return pvpFriendInviteMapper.findPendingOutboundForUser(userId);
	}

	public Long findPendingInviteIdForChallengerRoom(String matchId, long challengerUserId) {
		return pvpFriendInviteMapper.findInviteIdByMatchAndChallenger(matchId, challengerUserId);
	}

	/** 相手ユーザーが閲覧・承諾できる保留中の招待のみ返す */
	public PvpFriendInvite findPendingInviteForOpponent(long inviteId, long opponentUserId) {
		PvpFriendInvite inv = pvpFriendInviteMapper.findById(inviteId);
		if (inv == null || !"PENDING".equals(inv.getStatus())) {
			return null;
		}
		if (inv.getOpponentUserId() != opponentUserId) {
			return null;
		}
		return inv;
	}

	/**
	 * フレンドに対戦を申し込み、ホスト待機用の部屋を作成する。
	 */
	public PvpInviteCreated createInvite(long challengerUserId, long friendUserId, long challengerDeckId) {
		if (!friendService.areFriends(challengerUserId, friendUserId)) {
			throw new IllegalStateException("フレンドのみに対戦申し込みができます");
		}
		var m = pvpBattleService.createWaitingRoom(challengerUserId, challengerDeckId, friendUserId);
		var inv = new PvpFriendInvite();
		inv.setMatchId(m.getId());
		inv.setChallengerUserId(challengerUserId);
		inv.setOpponentUserId(friendUserId);
		inv.setStatus("PENDING");
		try {
			pvpFriendInviteMapper.insert(inv);
		} catch (DataIntegrityViolationException e) {
			pvpBattleService.removeMatch(m.getId());
			throw new IllegalStateException("この相手への対戦申し込みがすでに保留中です", e);
		}
		Long id = inv.getId();
		if (id == null) {
			pvpBattleService.removeMatch(m.getId());
			throw new IllegalStateException("対戦申し込みの保存に失敗しました");
		}
		return new PvpInviteCreated(id, m.getId());
	}

	@Transactional
	public void acceptInvite(long inviteId, long opponentUserId, long opponentDeckId) {
		PvpFriendInvite inv = pvpFriendInviteMapper.findById(inviteId);
		if (inv == null || !"PENDING".equals(inv.getStatus())) {
			throw new IllegalArgumentException("招待が見つからないか、すでに処理されています");
		}
		if (inv.getOpponentUserId() != opponentUserId) {
			throw new IllegalStateException("この招待を承諾できません");
		}
		if (pvpBattleService.get(inv.getMatchId()) == null) {
			pvpFriendInviteMapper.updateStatus(inviteId, "DECLINED");
			throw new IllegalStateException("招待の有効期限が切れたか、相手が取り消しました（承諾は申し込みから3分以内に行ってください）");
		}
		pvpBattleService.join(inv.getMatchId(), opponentUserId, opponentDeckId);
		pvpFriendInviteMapper.updateStatus(inviteId, "ACCEPTED");
	}

	@Transactional
	public void declineInvite(long inviteId, long opponentUserId) {
		PvpFriendInvite inv = pvpFriendInviteMapper.findById(inviteId);
		if (inv == null || !"PENDING".equals(inv.getStatus())) {
			throw new IllegalArgumentException("招待が見つからないか、すでに処理されています");
		}
		if (inv.getOpponentUserId() != opponentUserId) {
			throw new IllegalStateException("この招待を拒否できません");
		}
		pvpBattleService.removeMatch(inv.getMatchId());
		pvpFriendInviteMapper.updateStatus(inviteId, "DECLINED");
	}

	@Transactional
	public void cancelInvite(long inviteId, long challengerUserId) {
		PvpFriendInvite inv = pvpFriendInviteMapper.findById(inviteId);
		if (inv == null || !"PENDING".equals(inv.getStatus())) {
			throw new IllegalArgumentException("招待が見つからないか、すでに処理されています");
		}
		if (inv.getChallengerUserId() != challengerUserId) {
			throw new IllegalStateException("この招待を取り消せません");
		}
		pvpBattleService.removeMatch(inv.getMatchId());
		pvpFriendInviteMapper.updateStatus(inviteId, "CANCELLED");
	}

	/**
	 * 作成から {@link #PENDING_INVITE_TTL} を過ぎた保留招待を EXPIRED にし、対応する待機ルームをメモリから削除する。
	 */
	@Transactional
	public void expireStalePendingInvites() {
		Instant cutoff = Instant.now().minus(PENDING_INVITE_TTL);
		for (PvpFriendInvite row : pvpFriendInviteMapper.findPendingInvitesCreatedBefore(cutoff)) {
			int n = pvpFriendInviteMapper.expireByIdIfPending(row.getId(), cutoff);
			if (n == 1) {
				pvpBattleService.removeMatch(row.getMatchId());
			}
		}
	}
}
