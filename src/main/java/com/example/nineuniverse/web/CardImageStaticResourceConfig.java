package com.example.nineuniverse.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * {@code /images/cards/**} を明示登録し、{@link UnicodeFormFallbackResourceResolver} で NFC/NFD の両方を解決する。
 * <p>{@link ResourceHandlerRegistry#setOrder} は登録される<strong>すべて</strong>の静的ハンドラの優先度を変えるため、
 * ここでは呼ばない（{@code HIGHEST_PRECEDENCE} にすると {@code /**} がコントローラより先になり、画面が 404 になり得る）。
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
