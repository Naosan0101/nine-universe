package com.example.nineuniverse.web;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Electron 等が認証なしで現在の静的アセット世代を取得する（デプロイ後の強制リロード用）。
 */
@RestController
public class ClientAssetVersionController {

	private final StaticAssetVersionHolder staticAssetVersion;

	public ClientAssetVersionController(StaticAssetVersionHolder staticAssetVersion) {
		this.staticAssetVersion = staticAssetVersion;
	}

	@GetMapping(value = "/api/client-asset-version", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> clientAssetVersion() {
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore())
				.body(staticAssetVersion.get());
	}
}
