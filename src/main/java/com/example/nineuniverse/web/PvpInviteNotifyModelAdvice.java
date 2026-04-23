package com.example.nineuniverse.web;

import com.example.nineuniverse.security.AccountUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class PvpInviteNotifyModelAdvice {

	/**
	 * 未ログイン・匿名では既定 true（テンプレート未使用）。ログイン中は DB の設定を反映。
	 */
	@ModelAttribute("pvpInviteNotifyEnabled")
	public boolean pvpInviteNotifyEnabled(Authentication authentication) {
		if (authentication == null
				|| !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return true;
		}
		if (authentication.getPrincipal() instanceof AccountUserDetails d) {
			Boolean v = d.getUser().getPvpInviteNotifyEnabled();
			return v == null || Boolean.TRUE.equals(v);
		}
		return true;
	}
}
