package com.example.nineuniverse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class GameConstantsAnnouncementListTest {

	private static final ZoneId ZONE = ZoneId.of("Asia/Tokyo");

	@Test
	void shouldListAnnouncement_onlyWithinTwoWeekLookback() {
		LocalDate today = LocalDate.of(2026, 6, 25);
		LocalDate recent = LocalDate.of(2026, 6, 20);
		LocalDate old = LocalDate.of(2026, 4, 14);
		LocalDateTime created = LocalDateTime.of(2025, 1, 1, 0, 0);

		assertTrue(GameConstants.shouldListAnnouncementForUser(today, created, ZONE, recent));
		assertFalse(GameConstants.shouldListAnnouncementForUser(today, created, ZONE, old));
	}

	@Test
	void shouldListAnnouncement_sameForNewAndExistingUsers() {
		LocalDate today = LocalDate.of(2026, 6, 25);
		LocalDate old = LocalDate.of(2026, 5, 1);
		LocalDateTime newUser = LocalDateTime.of(2026, 6, 24, 12, 0);
		LocalDateTime oldUser = LocalDateTime.of(2024, 1, 1, 0, 0);

		assertFalse(GameConstants.shouldListAnnouncementForUser(today, newUser, ZONE, old));
		assertFalse(GameConstants.shouldListAnnouncementForUser(today, oldUser, ZONE, old));
	}
}
