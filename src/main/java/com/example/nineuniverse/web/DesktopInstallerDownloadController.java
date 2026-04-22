package com.example.nineuniverse.web;

import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * デスクトップ版インストーラ（exe）を {@code Content-Disposition: attachment} で返す。
 * 静的配信＋{@code <a download>} だと環境によって失敗や不正なファイル名になることがあるため専用エンドポイントにする。
 */
@RestController
public class DesktopInstallerDownloadController {

	private static final String CANONICAL_RESOURCE = "static/downloads/nine-universe-setup-0.1.0.exe";
	private static final String LEGACY_SPACED_RESOURCE = "static/downloads/Nine Universe Setup 0.1.0.exe";
	private static final String DOWNLOAD_FILENAME = "Nine Universe Setup 0.1.0.exe";

	@GetMapping("/downloads/nine-universe-setup-0.1.0.exe")
	public ResponseEntity<Resource> desktopInstaller() {
		Resource body = resolveInstallerBody();
		if (body == null) {
			return ResponseEntity.notFound().build();
		}
		ContentDisposition disposition = ContentDisposition.attachment()
				.filename(DOWNLOAD_FILENAME, StandardCharsets.UTF_8)
				.build();
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.body(body);
	}

	private static Resource resolveInstallerBody() {
		ClassPathResource canonical = new ClassPathResource(CANONICAL_RESOURCE);
		if (canonical.exists()) {
			return canonical;
		}
		ClassPathResource legacy = new ClassPathResource(LEGACY_SPACED_RESOURCE);
		if (legacy.exists()) {
			return legacy;
		}
		return null;
	}
}
