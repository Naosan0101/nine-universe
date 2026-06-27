package com.example.nineuniverse.season;

/**
 * リクエストスコープ相当: PlayTester 向けに {@link SeasonSchedule#unlockTier} を最大段階にする。
 */
public final class SeasonUnlockContext {

	private static final ThreadLocal<Boolean> FULL_UNLOCK = new ThreadLocal<>();

	private SeasonUnlockContext() {
	}

	public static void enableFullUnlock() {
		FULL_UNLOCK.set(Boolean.TRUE);
	}

	public static boolean isFullUnlock() {
		return Boolean.TRUE.equals(FULL_UNLOCK.get());
	}

	public static void clear() {
		FULL_UNLOCK.remove();
	}
}
