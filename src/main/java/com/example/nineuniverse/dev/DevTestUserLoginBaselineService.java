package com.example.nineuniverse.dev;

import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.repository.UserCollectionMapper;
import com.example.nineuniverse.security.AccountUserDetails;
import com.example.nineuniverse.service.CardCatalogService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ローカル検証用アカウント {@value DevTestUserLoginBaselineService#USERNAME} が、
 * {@code localhost} / {@code 127.0.0.1} 等からログインしたときだけ、経済状態を固定ベースラインへ戻す。
 * Remember-Me 等で {@link org.springframework.security.web.authentication.AuthenticationSuccessHandler} が
 * 走らない場合でも、ライブラリ／デッキ画面表示前に {@link #syncTestuserCollectionOnlyIfLocal(HttpServletRequest)} で
 * コレクションをカタログ全カード各 {@value DevTestUserLoginBaselineService#BASELINE_EACH_CARD} 枚に揃える。
 */
@Service
@RequiredArgsConstructor
public class DevTestUserLoginBaselineService {

	static final String USERNAME = "testuser";
	static final int BASELINE_GEMS = 9999;
	static final int BASELINE_EACH_CARD = 20;

	private final AppUserMapper appUserMapper;
	private final UserCollectionMapper userCollectionMapper;
	private final CardCatalogService cardCatalogService;

	@Value("${app.dev.testuser-login-reset:true}")
	private boolean loginResetEnabled;

	public void resetIfApplicable(Authentication authentication, HttpServletRequest request) {
		if (!shouldApply(request)) {
			return;
		}
		if (authentication == null || !(authentication.getPrincipal() instanceof AccountUserDetails d)) {
			return;
		}
		if (!USERNAME.equalsIgnoreCase(d.getUsername())) {
			return;
		}
		Long uid = d.getUser().getId();
		if (uid == null) {
			return;
		}
		resetUser(uid);
	}

	/**
	 * localhost 上の {@value DevTestUserLoginBaselineService#USERNAME} 向けに、所持をカタログ全カード各
	 * {@value DevTestUserLoginBaselineService#BASELINE_EACH_CARD} 枚へ揃える。
	 * 新カード追加後や Remember-Me 直後でもライブラリで未所持にならないようにする。
	 */
	@Transactional
	public void syncTestuserCollectionOnlyIfLocal(HttpServletRequest request) {
		if (!loginResetEnabled) {
			return;
		}
		if (request == null || !isLocalhostServerName(request.getServerName())) {
			return;
		}
		var auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof AccountUserDetails d)) {
			return;
		}
		if (!USERNAME.equalsIgnoreCase(d.getUsername())) {
			return;
		}
		Long uid = d.getUser().getId();
		if (uid == null) {
			return;
		}
		applyCollectionBaselineQuantities(uid);
	}

	private boolean shouldApply(HttpServletRequest request) {
		if (!loginResetEnabled) {
			return false;
		}
		return request != null && isLocalhostServerName(request.getServerName());
	}

	/** ブラウザが {@code localhost} / ループバックでアクセスしているときだけ true（LAN IP 経由ではリセットしない）。 */
	static boolean isLocalhostServerName(String serverName) {
		if (serverName == null || serverName.isBlank()) {
			return false;
		}
		String h = serverName.trim().toLowerCase(Locale.ROOT);
		if ("localhost".equals(h) || "127.0.0.1".equals(h)) {
			return true;
		}
		if ("::1".equals(h) || "0:0:0:0:0:0:0:1".equals(h)) {
			return true;
		}
		return "[::1]".equals(serverName.trim());
	}

	@Transactional
	public void resetUser(long userId) {
		appUserMapper.updateCoinsAndMarkWelcomeHomeBonusGranted(userId, BASELINE_GEMS);
		appUserMapper.setRecycleCrystal(userId, 0);
		appUserMapper.setStarterGiftStandard1Remaining(userId, 0);
		applyCollectionBaselineQuantities(userId);
	}

	private void applyCollectionBaselineQuantities(long userId) {
		for (var c : cardCatalogService.all()) {
			userCollectionMapper.upsertExactQuantity(userId, c.getId(), BASELINE_EACH_CARD);
		}
	}
}
