package com.example.nineuniverse.security;

import com.example.nineuniverse.dev.DevTestUserLoginBaselineService;
import com.example.nineuniverse.support.PlayTesterCollectionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * ログイン成功後に {@link DevTestUserLoginBaselineService} と {@link PlayTesterCollectionService} を適用し、
 * 従来どおり /home へ遷移する。
 */
public class DevTestUserAwareAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final DevTestUserLoginBaselineService devTestUserLoginBaselineService;
	private final PlayTesterCollectionService playTesterCollectionService;
	private final AuthenticationSuccessHandler delegate;

	public DevTestUserAwareAuthenticationSuccessHandler(DevTestUserLoginBaselineService baseline,
			PlayTesterCollectionService playTesterCollectionService) {
		this.devTestUserLoginBaselineService = baseline;
		this.playTesterCollectionService = playTesterCollectionService;
		var inner = new SavedRequestAwareAuthenticationSuccessHandler();
		inner.setDefaultTargetUrl("/home");
		inner.setAlwaysUseDefaultTargetUrl(true);
		this.delegate = inner;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		devTestUserLoginBaselineService.resetIfApplicable(authentication, request);
		playTesterCollectionService.syncOnLogin(authentication);
		delegate.onAuthenticationSuccess(request, response, authentication);
	}
}
