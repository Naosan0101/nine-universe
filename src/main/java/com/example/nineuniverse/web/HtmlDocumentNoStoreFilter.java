package com.example.nineuniverse.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Locale;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Thymeleaf 等の HTML 応答にキャッシュ抑止ヘッダを付与する。
 * デスクトップ／ブラウザで「初回だけ古い HTML が残り、?v= の古い CSS が読まれる」事象を減らす。
 * 静的リソース（/css 等）は {@code spring.web.resources} の設定のまま。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HtmlDocumentNoStoreFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		var wrapped = new HttpServletResponseWrapper(response) {
			@Override
			public void setContentType(String type) {
				super.setContentType(type);
				if (type != null && type.toLowerCase(Locale.ROOT).contains("text/html")) {
					setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate");
					setHeader(HttpHeaders.PRAGMA, "no-cache");
				}
			}
		};
		filterChain.doFilter(request, wrapped);
	}
}
