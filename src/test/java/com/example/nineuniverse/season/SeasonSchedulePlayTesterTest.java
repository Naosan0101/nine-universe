package com.example.nineuniverse.season;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.nineuniverse.service.PackService.PackType;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SeasonSchedulePlayTesterTest {

	@AfterEach
	void tearDown() {
		SeasonUnlockContext.clear();
	}

	@Test
	void fullUnlockContext_grantsTier3PacksAndLeagueInEarlySeason() {
		LocalDate tier1 = LocalDate.of(2026, 6, 15);
		assertEquals(1, SeasonSchedule.unlockTier(tier1));

		SeasonUnlockContext.enableFullUnlock();
		assertEquals(3, SeasonSchedule.unlockTier(tier1));
		assertTrue(SeasonSchedule.isPackUnlocked(PackType.STANDARD_3, tier1));
		assertTrue(SeasonSchedule.isPackUnlocked(PackType.OCEAN_TIDE, tier1));
		assertTrue(SeasonSchedule.isLeagueBattleUnlocked(tier1));
		assertTrue(SeasonSchedule.isCpuAdvancedUnlocked(tier1));
		assertTrue(SeasonSchedule.isPackInitialVisible("OT", tier1));
		assertTrue(SeasonSchedule.isPackInitialVisible("CS", tier1));
	}
}
