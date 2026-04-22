package com.example.nineuniverse.web;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriUtils;

/**
 * Thymeleaf 3.1+ では #request が既定で使えないため、JS 用にコンテキストパスをモデルへ渡す。
 */
@ControllerAdvice
public class GlobalThymeleafModelAdvice {

	@Value("${app.web-desktop-migration-notice.enabled:true}")
	private boolean webDesktopMigrationNoticeEnabled;

	@Value("${app.web-desktop-migration-notice.installer-resource-path:/downloads/Nine Universe Setup 0.1.0.exe}")
	private String webDesktopMigrationInstallerResourcePath;

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
		String cp = request.getContextPath();
		String prefix = (cp == null || cp.isEmpty()) ? "" : cp;
		String path = webDesktopMigrationInstallerResourcePath;
		if (path == null || path.isBlank()) {
			return prefix;
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return prefix + UriUtils.encodePath(path, StandardCharsets.UTF_8);
	}
}
