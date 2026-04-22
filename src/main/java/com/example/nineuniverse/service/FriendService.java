package com.example.nineuniverse.service;

import com.example.nineuniverse.domain.AppUser;
import com.example.nineuniverse.domain.FriendInboundNotice;
import com.example.nineuniverse.domain.FriendListRow;
import com.example.nineuniverse.domain.FriendRequest;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.repository.FriendRequestMapper;
import com.example.nineuniverse.repository.FriendshipMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendService {

	private final AppUserMapper appUserMapper;
	private final FriendRequestMapper friendRequestMapper;
	private final FriendshipMapper friendshipMapper;

	public int countPendingInbound(long userId) {
		return friendRequestMapper.countPendingInboundForUser(userId);
	}

	public List<FriendInboundNotice> listPendingInbound(long userId) {
		return friendRequestMapper.findPendingInboundForUser(userId);
	}

	public List<FriendListRow> listFriends(long userId) {
		return friendshipMapper.listFriendsForUser(userId);
	}

	public boolean areFriends(long userA, long userB) {
		if (userA == userB) {
			return false;
		}
		return friendshipMapper.existsPair(userA, userB);
	}

	@Transactional
	public void sendRequest(long fromUserId, String targetUsernameRaw) {
		String targetUsername = targetUsernameRaw != null ? targetUsernameRaw.trim() : "";
		if (targetUsername.isEmpty()) {
			throw new IllegalArgumentException("ユーザーIDを入力してください");
		}
		AppUser target = appUserMapper.findByUsername(targetUsername);
		if (target == null) {
			throw new IllegalArgumentException("そのユーザーIDのユーザーは見つかりません");
		}
		long toUserId = target.getId();
		if (toUserId == fromUserId) {
			throw new IllegalArgumentException("自分自身には申請できません");
		}
		if (friendshipMapper.existsPair(fromUserId, toUserId)) {
			throw new IllegalStateException("すでにフレンドです");
		}
		if (friendRequestMapper.findPendingFromTo(fromUserId, toUserId) != null) {
			throw new IllegalStateException("すでにフレンド申請を送っています");
		}
		if (friendRequestMapper.findPendingFromTo(toUserId, fromUserId) != null) {
			throw new IllegalStateException("相手からフレンド申請が届いています。「届いた申請」から承諾または拒否してください");
		}
		var row = new FriendRequest();
		row.setFromUserId(fromUserId);
		row.setToUserId(toUserId);
		row.setStatus("PENDING");
		try {
			friendRequestMapper.insert(row);
		} catch (DataIntegrityViolationException e) {
			throw new IllegalStateException("すでにフレンド申請を送っています", e);
		}
	}

	@Transactional
	public void acceptRequest(long currentUserId, long requestId) {
		FriendRequest fr = friendRequestMapper.findById(requestId);
		if (fr == null || !"PENDING".equals(fr.getStatus())) {
			throw new IllegalArgumentException("申請が見つからないか、処理済みです");
		}
		if (fr.getToUserId() != currentUserId) {
			throw new IllegalStateException("この申請を承諾できません");
		}
		long low = Math.min(fr.getFromUserId(), fr.getToUserId());
		long high = Math.max(fr.getFromUserId(), fr.getToUserId());
		try {
			friendshipMapper.insert(low, high);
		} catch (DataIntegrityViolationException e) {
			throw new IllegalStateException("すでにフレンドです", e);
		}
		friendRequestMapper.deletePendingBetween(fr.getFromUserId(), fr.getToUserId());
	}

	@Transactional
	public void rejectRequest(long currentUserId, long requestId) {
		FriendRequest fr = friendRequestMapper.findById(requestId);
		if (fr == null || !"PENDING".equals(fr.getStatus())) {
			throw new IllegalArgumentException("申請が見つからないか、処理済みです");
		}
		if (fr.getToUserId() != currentUserId) {
			throw new IllegalStateException("この申請を拒否できません");
		}
		friendRequestMapper.updateStatus(requestId, "REJECTED");
	}
}
