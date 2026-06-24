package com.example.nineuniverse.season;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 認証済みリクエストのたびに、シーズン区切りを跨いでいれば全ユーザー進行データをリセットする。
 */
@Component
@RequiredArgsConstructor
public class SeasonResetFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(SeasonResetFilter.class);

	private final SeasonResetService seasonResetService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
			try {
				seasonResetService.ensureCurrentPeriodReset();
			} catch (Exception e) {
				log.error("シーズンリセット判定に失敗しました（リクエストは継続します）: {}", request.getRequestURI(), e);
			}
		}
		filterChain.doFilter(request, response);
	}
}
