package com.example.nineuniverse.dev;

import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.repository.UserCollectionMapper;
import com.example.nineuniverse.security.AccountUserDetails;
import com.example.nineuniverse.service.CardCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ローカル検証用アカウント {@value DevTestUserLoginBaselineService#USERNAME} の経済状態を、
 * ログインのたびに固定ベースラインへ戻す。
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

	@Value("${app.dev.testuser-login-reset-require-default-port:true}")
	private boolean requireDefaultPort8080;

	@Value("${server.port:8080}")
	private int serverPort;

	public void resetIfApplicable(Authentication authentication) {
		if (!shouldApply()) {
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

	private boolean shouldApply() {
		if (!loginResetEnabled) {
			return false;
		}
		return !requireDefaultPort8080 || serverPort == 8080;
	}

	@Transactional
	public void resetUser(long userId) {
		appUserMapper.updateCoinsAndMarkWelcomeHomeBonusGranted(userId, BASELINE_GEMS);
		userCollectionMapper.deleteByUserId(userId);
		for (var c : cardCatalogService.all()) {
			userCollectionMapper.insertQuantity(userId, c.getId(), BASELINE_EACH_CARD);
		}
	}
}
