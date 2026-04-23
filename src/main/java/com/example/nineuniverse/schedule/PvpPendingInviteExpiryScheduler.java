package com.example.nineuniverse.schedule;

import com.example.nineuniverse.service.PvpFriendInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PvpPendingInviteExpiryScheduler {

	private final PvpFriendInviteService pvpFriendInviteService;

	@Scheduled(fixedDelay = 15000L)
	public void expireStaleInvites() {
		pvpFriendInviteService.expireStalePendingInvites();
	}
}
