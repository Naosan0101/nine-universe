package com.example.nineuniverse.web;

import jakarta.servlet.http.HttpServletRequest;
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

	/** 空でなければポップアップのリンク先をこの絶対 URL に差し替え（CDN 等）。未設定時は {@code /downloads/nine-universe-setup-0.1.0.exe}。 */
	@Value("${app.web-desktop-migration-notice.installer-absolute-url:}")
	private String webDesktopMigrationInstallerAbsoluteUrl;

	@ModelAttribute("contextPath")
	public String contextPath(HttpServletRequest request) {
		String cp = request.getContextPath();
		return cp != null ? cp : "";
	}

	@ModelAttribute("webDesktopMigrationNoticeEnabled")
	public boolean webDesktopMigrationNoticeEnabled() {
		return webDesktopMigrationNoticeEnabled;
	}

	@ModelAttribute("webDesktopMigrationInstallerHref")
	public String webDesktopMigrationInstallerHref(HttpServletRequest request) {
		if (!webDesktopMigrationNoticeEnabled) {
			return "";
		}
		if (webDesktopMigrationInstallerAbsoluteUrl != null && !webDesktopMigrationInstallerAbsoluteUrl.isBlank()) {
			return webDesktopMigrationInstallerAbsoluteUrl.trim();
		}
		String cp = request.getContextPath();
		String prefix = (cp == null || cp.isEmpty()) ? "" : cp;
		return prefix + "/downloads/nine-universe-setup-0.1.0.exe";
	}
}
