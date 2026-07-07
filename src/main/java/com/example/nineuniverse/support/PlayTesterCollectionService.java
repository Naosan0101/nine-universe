package com.example.nineuniverse.support;

import com.example.nineuniverse.repository.UserCollectionMapper;
import com.example.nineuniverse.security.AccountUserDetails;
import com.example.nineuniverse.service.CardCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー名末尾 {@code _PlayTester} のアカウント向けに、所持カードをカタログ全件各
 * {@value #EACH_CARD_QUANTITY} 枚へ揃える。
 */
@Service
@RequiredArgsConstructor
public class PlayTesterCollectionService {

	public static final int EACH_CARD_QUANTITY = 2;

	private final UserCollectionMapper userCollectionMapper;
	private final CardCatalogService cardCatalogService;

	public void syncOnLogin(Authentication authentication) {
		Long uid = playTesterUserId(authentication);
		if (uid != null) {
			applyCollection(uid);
		}
	}

	public void syncIfPlayTester() {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		Long uid = playTesterUserId(auth);
		if (uid != null) {
			applyCollection(uid);
		}
	}

	private static Long playTesterUserId(Authentication auth) {
		if (auth == null || !(auth.getPrincipal() instanceof AccountUserDetails d)) {
			return null;
		}
		if (!PlayTesterSupport.isPlayTesterUsername(d.getUsername())) {
			return null;
		}
		Long uid = d.getUser().getId();
		return uid;
	}

	@Transactional
	public void applyCollection(long userId) {
		for (var c : cardCatalogService.all()) {
			userCollectionMapper.upsertExactQuantity(userId, c.getId(), EACH_CARD_QUANTITY);
		}
	}
}
