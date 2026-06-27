package com.example.nineuniverse.security;

import com.example.nineuniverse.season.SeasonUnlockContext;
import com.example.nineuniverse.support.PlayTesterSupport;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * ユーザー名が {@code _PlayTester} で終わるログイン中ユーザーに、全パック・リーグ等のシーズンロック解除を適用する。
 */
@Component
public class PlayTesterSeasonUnlockFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof AccountUserDetails details) {
				if (PlayTesterSupport.isPlayTesterUsername(details.getUsername())) {
					SeasonUnlockContext.enableFullUnlock();
				}
			}
			filterChain.doFilter(request, response);
		} finally {
			SeasonUnlockContext.clear();
		}
	}
}
