package com.example.nineuniverse.web;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

/**
 * Thymeleaf の {@code assetVersion} と同一の文字列（ビルド時刻 ± 接尾辞）。
 * デスクトップシェルがデプロイ検知用に {@link ClientAssetVersionController} 経由で取得する。
 */
@Component
public class StaticAssetVersionHolder {

	private final String version;

	public StaticAssetVersionHolder(
			ObjectProvider<BuildProperties> buildProperties,
			@Value("${app.static-asset-version-suffix:}") String assetVersionSuffix) {
		String suffix = assetVersionSuffix == null ? "" : assetVersionSuffix.trim();
		BuildProperties bp = buildProperties.getIfAvailable();
		if (bp != null && bp.getTime() != null) {
			String base = Long.toString(bp.getTime().toEpochMilli());
			this.version = suffix.isEmpty() ? base : base + "-" + suffix;
		} else {
			this.version = suffix.isEmpty() ? "dev" : "dev-" + suffix;
		}
	}

	public String get() {
		return version;
	}
}
