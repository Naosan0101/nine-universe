package com.example.nineuniverse.web;

import com.example.nineuniverse.web.dto.DesktopClientUpdateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * デスクトップアプリ向けに、配布中の最新版バージョンとインストーラ URL を返す。
 * {@code installer-url} が空のときはクライアント側で更新促しを出さない想定。
 */
@RestController
public class DesktopClientUpdateController {

	private final String latestVersion;
	private final String installerUrl;

	public DesktopClientUpdateController(
			@Value("${app.desktop-client.update.latest-version:}") String latestVersion,
			@Value("${app.desktop-client.update.installer-url:}") String installerUrl) {
		this.latestVersion = latestVersion == null ? "" : latestVersion.trim();
		this.installerUrl = installerUrl == null ? "" : installerUrl.trim();
	}

	@GetMapping(value = "/api/desktop-client-update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DesktopClientUpdateResponse> desktopClientUpdate() {
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore())
				.body(new DesktopClientUpdateResponse(latestVersion, installerUrl));
	}
}
