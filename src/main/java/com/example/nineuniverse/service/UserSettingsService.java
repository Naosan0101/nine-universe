package com.example.nineuniverse.service;

import com.example.nineuniverse.domain.AppUser;
import com.example.nineuniverse.repository.AppUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

	public static final int DISPLAY_NAME_MAX_LEN = 64;

	private final AppUserMapper appUserMapper;

	@Transactional
	public void updateProfile(long userId, String displayNameRaw, String cpuThinkSpeedRaw) {
		String speed = normalizeCpuThinkSpeed(cpuThinkSpeedRaw);
		String name = normalizeDisplayName(displayNameRaw);
		int n = appUserMapper.updateProfileSettings(userId, name, speed);
		if (n == 0) {
			throw new IllegalStateException("ユーザーが見つかりません");
		}
	}

	public static String normalizeDisplayName(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new IllegalArgumentException("ユーザー名を入力してください。");
		}
		String t = raw.trim();
		if (t.length() > DISPLAY_NAME_MAX_LEN) {
			throw new IllegalArgumentException("ユーザー名は " + DISPLAY_NAME_MAX_LEN + " 文字以内にしてください。");
		}
		return t;
	}

	public static String normalizeCpuThinkSpeed(String raw) {
		if (raw == null || raw.isBlank()) {
			return "NORMAL";
		}
		return switch (raw.trim().toUpperCase()) {
			case "FAST", "NORMAL", "SLOW" -> raw.trim().toUpperCase();
			default -> "NORMAL";
		};
	}

	/** セッション上の {@link AppUser} を DB と揃える（設定保存直後用） */
	public void refreshUserInMemory(AppUser sessionUser, long userId) {
		AppUser fresh = appUserMapper.findById(userId);
		if (fresh == null || sessionUser == null) {
			return;
		}
		sessionUser.setDisplayName(fresh.getDisplayName());
		sessionUser.setCpuThinkSpeed(fresh.getCpuThinkSpeed());
	}
}
