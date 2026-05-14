package com.example.nineuniverse.web;

import com.example.nineuniverse.web.dto.DesktopClientUpdateResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
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
	public ResponseEntity<DesktopClientUpdateResponse> desktopClientUpdate(HttpServletRequest request) {
		String urlOut = harmonizeLoopbackInstallerUrlForRequest(installerUrl, request);
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore())
				.body(new DesktopClientUpdateResponse(latestVersion, urlOut));
	}

	/**
	 * 設定が {@code http(s)://localhost} / {@code 127.0.0.1} / {@code [::1]} 等のループバック向けのとき、
	 * 実際のリクエストの scheme・host・port に合わせて返す。
	 * プレビューや開発で {@code localhost} と {@code 127.0.0.1} を混在させても、ページと API の URL が揃う。
	 */
	static String harmonizeLoopbackInstallerUrlForRequest(String configured, HttpServletRequest request) {
		if (configured == null || configured.isBlank()) {
			return "";
		}
		String t = configured.trim();
		final URI u;
		try {
			u = URI.create(t);
		} catch (IllegalArgumentException e) {
			return t;
		}
		String scheme = u.getScheme();
		if (scheme == null) {
			return t;
		}
		String ls = scheme.toLowerCase(Locale.ROOT);
		if (!"http".equals(ls) && !"https".equals(ls)) {
			return t;
		}
		String host = u.getHost();
		if (host == null || !isLoopbackInstallerHost(host)) {
			return t;
		}
		try {
			String reqScheme = request.getScheme();
			String reqHost = request.getServerName();
			int reqPort = request.getServerPort();
			int uriPort = isDefaultWebPort(reqScheme, reqPort) ? -1 : reqPort;
			String path = u.getRawPath();
			if (path == null || path.isEmpty()) {
				path = "/";
			}
			URI rebuilt = new URI(reqScheme, null, reqHost, uriPort, path, u.getRawQuery(), null);
			return rebuilt.toASCIIString();
		} catch (URISyntaxException e) {
			return t;
		}
	}

	private static boolean isLoopbackInstallerHost(String host) {
		String h = host.trim().toLowerCase(Locale.ROOT);
		return "localhost".equals(h)
				|| "127.0.0.1".equals(h)
				|| "[::1]".equals(h)
				|| "::1".equals(h)
				|| "0:0:0:0:0:0:0:1".equals(h);
	}

	private static boolean isDefaultWebPort(String scheme, int port) {
		return ("http".equalsIgnoreCase(scheme) && port == 80)
				|| ("https".equalsIgnoreCase(scheme) && port == 443);
	}
}
