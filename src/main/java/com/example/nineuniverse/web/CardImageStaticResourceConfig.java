package com.example.nineuniverse.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * {@code /images/cards/**} を明示登録し、{@link UnicodeFormFallbackResourceResolver} で NFC/NFD の両方を解決する。
 * より具体的なパターンで既定の {@code /**} 静的配信と競合したときに優先されるよう、設定の順序を高くする。
 */
@Configuration(proxyBeanMethods = false)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CardImageStaticResourceConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/images/cards/**")
				.addResourceLocations("classpath:/static/images/cards/")
				.resourceChain(false)
				.addResolver(new UnicodeFormFallbackResourceResolver());
	}
}
