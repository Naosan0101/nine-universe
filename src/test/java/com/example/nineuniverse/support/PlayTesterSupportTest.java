package com.example.nineuniverse.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayTesterSupportTest {

	@Test
	void isPlayTesterUsername_trueWhenSuffixMatches() {
		assertTrue(PlayTesterSupport.isPlayTesterUsername("alice_PlayTester"));
		assertTrue(PlayTesterSupport.isPlayTesterUsername("  bob_PlayTester  "));
	}

	@Test
	void isPlayTesterUsername_falseOtherwise() {
		assertFalse(PlayTesterSupport.isPlayTesterUsername(null));
		assertFalse(PlayTesterSupport.isPlayTesterUsername(""));
		assertFalse(PlayTesterSupport.isPlayTesterUsername("PlayTester"));
		assertFalse(PlayTesterSupport.isPlayTesterUsername("alice_playtester"));
		assertFalse(PlayTesterSupport.isPlayTesterUsername("alice_PlayTester2"));
	}
}
