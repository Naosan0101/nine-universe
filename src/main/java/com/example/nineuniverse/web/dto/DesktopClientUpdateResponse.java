package com.example.nineuniverse.web.dto;

/**
 * {@code GET /api/desktop-client-update} の JSON。デスクトップシェルがバージョン比較に使う。
 */
public class DesktopClientUpdateResponse {

	private String latestVersion;
	private String installerUrl;

	public DesktopClientUpdateResponse() {
	}

	public DesktopClientUpdateResponse(String latestVersion, String installerUrl) {
		this.latestVersion = latestVersion != null ? latestVersion : "";
		this.installerUrl = installerUrl != null ? installerUrl : "";
	}

	public String getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(String latestVersion) {
		this.latestVersion = latestVersion;
	}

	public String getInstallerUrl() {
		return installerUrl;
	}

	public void setInstallerUrl(String installerUrl) {
		this.installerUrl = installerUrl;
	}
}
