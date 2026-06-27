package com.example.nineuniverse.support;

/**
 * ユーザー名末尾 {@code _PlayTester} のアカウント向け。シーズン段階ロックをすべて解除する。
 */
public final class PlayTesterSupport {

	public static final String USERNAME_SUFFIX = "_PlayTester";

	private PlayTesterSupport() {
	}

	public static boolean isPlayTesterUsername(String username) {
		if (username == null || username.isBlank()) {
			return false;
		}
		return username.trim().endsWith(USERNAME_SUFFIX);
	}
}
