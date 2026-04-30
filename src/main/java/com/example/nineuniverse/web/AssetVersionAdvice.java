package com.example.nineuniverse.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Thymeleaf の {@code @{/css/app.css(v=${assetVersion})}} 用。
 * 本番の {@code max-age=7d} でも、デプロイのたびに URL が変わりブラウザが新しい静的ファイルを取りにいく。
 * <p>
 * 同一 JAR のまま全クライアントに CSS/JS の再取得だけさせたいときは
 * {@code app.static-asset-version-suffix} を変えて再起動する（ビルド時刻または dev に接尾辞を付ける）。
 */
@ControllerAdvice
@RequiredArgsConstructor
public class AssetVersionAdvice {

	private final StaticAssetVersionHolder staticAssetVersion;

	@ModelAttribute("assetVersion")
	public String assetVersion() {
		return staticAssetVersion.get();
	}
}
