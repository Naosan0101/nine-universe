package com.example.nineuniverse.domain;

/**
 * ログインID（{@link AppUser#getUsername()}）と表示名（{@link AppUser#getDisplayName()}）の扱い。
 */
public final class UserDisplayNames {

	private UserDisplayNames() {
	}

	public static String effectiveDisplayName(AppUser u) {
		if (u == null) {
			return "";
		}
		String d = u.getDisplayName();
		if (d != null && !d.isBlank()) {
			return d.trim();
		}
		return u.getUsername() != null ? u.getUsername() : "";
	}
}
