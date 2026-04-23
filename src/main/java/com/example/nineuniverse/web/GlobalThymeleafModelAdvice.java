package com.example.nineuniverse.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Thymeleaf 3.1+ では #request が既定で使えないため、JS 用にコンテキストパスをモデルへ渡す。
 */
@ControllerAdvice
public class GlobalThymeleafModelAdvice {

	@Value("${app.web-desktop-migration-notice.enabled:true}")
	private boolean webDesktopMigrationNoticeEnabled;

	/** 空でなければポップアップのリンク先をこの絶対 URL に差し替え（CDN 等）。未設定時は {@code /downloads/nine-universe-setup-0.1.1.exe}。 */
	@Value("${app.web-desktop-migration-notice.installer-absolute-url:}")
	private String webDesktopMigrationInstallerAbsoluteUrl;

	/** 空でなければ Mac 用リンクをこの絶対 URL に差し替え。未設定時は {@code /downloads/nine-universe-0.1.1.dmg}。 */
	@Value("${app.web-desktop-migration-notice.installer-mac-absolute-url:}")
	private String webDesktopMigrationInstallerMacAbsoluteUrl;

	/**
	 * 非空のとき、デスクトップアプリの {@code appVersion} がこれ未満ならログイン前に全画面で更新を促す（Electron のみ）。
	 * 例: {@code 0.1.1}。空のときは無効。
	 */
	@Value("${app.desktop-client.minimum-version:}")
	private String desktopMinimumVersionProperty;

	@ModelAttribute("contextPath")
	public String contextPath(HttpServletRequest request) {
		String cp = request.getContextPath();
		return cp != null ? cp : "";
	}

	@ModelAttribute("webDesktopMigrationNoticeEnabled")
	public boolean webDesktopMigrationNoticeEnabled(HttpServletRequest request) {
		if (!this.webDesktopMigrationNoticeEnabled) {
			return false;
		}
		if (isLocalHostRequest(request)) {
			return false;
		}
		/* デスクトップアプリ内蔵ブラウザでは Web 向けのインストーラ案内は不要（ホームのお知らせ内リンクも同属性で抑制） */
		if (isDesktopAppEmbeddedRequest(request)) {
			return false;
		}
		return true;
	}

	private static boolean isLocalHostRequest(HttpServletRequest request) {
		String serverName = request.getServerName();
		if (serverName == null || serverName.isBlank()) {
			return false;
		}
		String h = serverName.trim().toLowerCase(Locale.ROOT);
		return "localhost".equals(h)
				|| "127.0.0.1".equals(h)
				|| "::1".equals(h)
				|| "0:0:0:0:0:0:0:1".equals(h);
	}

	/**
	 * Electron 埋め込み（本ゲームのデスクトップ版）からのリクエスト。既定 User-Agent に {@code Electron/} が含まれる。
	 * Web ブラウザ向け「アプリをダウンロード」の案内を出さないために使う（強制更新用の別属性とは独立）。
	 */
	private static boolean isDesktopAppEmbeddedRequest(HttpServletRequest request) {
		String ua = request.getHeader("User-Agent");
		if (ua == null || ua.isBlank()) {
			return false;
		}
		return ua.contains("Electron");
	}

	@ModelAttribute("webDesktopMigrationInstallerHref")
	public String webDesktopMigrationInstallerHref(HttpServletRequest request) {
		if (!webDesktopMigrationNoticeEnabled(request)) {
			return "";
		}
		return resolveWindowsInstallerHref(request);
	}

	@ModelAttribute("webDesktopMigrationInstallerMacHref")
	public String webDesktopMigrationInstallerMacHref(HttpServletRequest request) {
		if (!webDesktopMigrationNoticeEnabled(request)) {
			return "";
		}
		return resolveMacInstallerHref(request);
	}

	@ModelAttribute("desktopClientUpdateGateActive")
	public boolean desktopClientUpdateGateActive() {
		return desktopMinimumVersionProperty != null && !desktopMinimumVersionProperty.isBlank();
	}

	@ModelAttribute("desktopClientMinimumVersion")
	public String desktopClientMinimumVersion() {
		if (!desktopClientUpdateGateActive()) {
			return "";
		}
		return desktopMinimumVersionProperty.trim();
	}

	/**
	 * デスクトップ版の強制更新案内用。Web 版移行バナーのオンオフとは独立（インストーラ URL は同一プロパティを参照）。
	 */
	@ModelAttribute("desktopClientUpdateInstallerWinHref")
	public String desktopClientUpdateInstallerWinHref(HttpServletRequest request) {
		if (!desktopClientUpdateGateActive()) {
			return "";
		}
		return resolveWindowsInstallerHref(request);
	}

	@ModelAttribute("desktopClientUpdateInstallerMacHref")
	public String desktopClientUpdateInstallerMacHref(HttpServletRequest request) {
		if (!desktopClientUpdateGateActive()) {
			return "";
		}
		return resolveMacInstallerHref(request);
	}

	private String resolveWindowsInstallerHref(HttpServletRequest request) {
		if (webDesktopMigrationInstallerAbsoluteUrl != null && !webDesktopMigrationInstallerAbsoluteUrl.isBlank()) {
			return webDesktopMigrationInstallerAbsoluteUrl.trim();
		}
		String cp = request.getContextPath();
		String prefix = (cp == null || cp.isEmpty()) ? "" : cp;
		return prefix + "/downloads/nine-universe-setup-0.1.1.exe";
	}

	private String resolveMacInstallerHref(HttpServletRequest request) {
		if (webDesktopMigrationInstallerMacAbsoluteUrl != null && !webDesktopMigrationInstallerMacAbsoluteUrl.isBlank()) {
			return webDesktopMigrationInstallerMacAbsoluteUrl.trim();
		}
		String cp = request.getContextPath();
		String prefix = (cp == null || cp.isEmpty()) ? "" : cp;
		return prefix + "/downloads/nine-universe-0.1.1.dmg";
	}
}
