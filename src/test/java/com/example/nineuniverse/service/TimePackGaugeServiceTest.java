package com.example.nineuniverse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.nineuniverse.GameConstants;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TimePackGaugeServiceTest {

	@Test
	void computeSnapshot_midCycle_hasOnePack() {
		long dur = GameConstants.TIME_PACK_CYCLE_DURATION_MS;
		Instant cycleStart = Instant.EPOCH;
		Instant now = cycleStart.plusMillis(dur * 3 / 4);
		var snap = TimePackGaugeService.computeSnapshot(cycleStart, now);
		assertEquals(1, snap.availablePacks());
		assertEquals(0.75, snap.fillRatio(), 1e-12);
	}

	@Test
	void afterClaimingOneTimerPack_surplusMillisCarryIntoNextGauge() {
		long dur = GameConstants.TIME_PACK_CYCLE_DURATION_MS;
		long half = dur / 2;
		Instant cycleStart = Instant.EPOCH;
		long elapsed = dur * 3 / 4;
		Instant now = cycleStart.plusMillis(elapsed);
		long carry = Math.max(0L, elapsed - half);
		Instant newStart = now.minusMillis(carry);
		var after = TimePackGaugeService.computeSnapshot(newStart, now);
		assertEquals(0, after.availablePacks());
		assertEquals(0.25, after.fillRatio(), 1e-12);
	}
}
