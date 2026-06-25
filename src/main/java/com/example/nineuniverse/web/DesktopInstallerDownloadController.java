package com.example.nineuniverse.web;

import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * デスクトップ版インストーラ（Windows: exe、Mac: dmg）を {@code Content-Disposition: attachment} で返す。
 * 静的配信＋{@code <a download>} だと環境によって失敗や不正なファイル名になることがあるため専用エンドポイントにする。
 */
@RestController
public class DesktopInstallerDownloadController {

	private static final String DOWNLOAD_WIN_FILENAME = "Nine Universe Setup 0.1.5.exe";
	private static final String DOWNLOAD_MAC_FILENAME = "Nine Universe 0.1.5.dmg";

	private static final String[] WINDOWS_INSTALLER_RESOURCE_CANDIDATES = {
			"static/downloads/nine-universe-setup-0.1.5.exe",
			"static/downloads/Nine Universe Setup 0.1.5.exe",
			"static/downloads/nine-universe-setup-0.1.4.exe",
			"static/downloads/Nine Universe Setup 0.1.4.exe",
	};

	private static final String[] MAC_INSTALLER_RESOURCE_CANDIDATES = {
			"static/downloads/nine-universe-0.1.5.dmg",
			"static/downloads/Nine Universe-0.1.5-universal.dmg",
			"static/downloads/nine-universe-0.1.4.dmg",
			"static/downloads/Nine Universe-0.1.4-universal.dmg",
	};

	@GetMapping("/downloads/nine-universe-setup-0.1.5.exe")
	public ResponseEntity<Resource> desktopInstallerWindows() {
		Resource body = resolveWindowsInstallerBody();
		if (body == null) {
			return ResponseEntity.notFound().build();
		}
		ContentDisposition disposition = ContentDisposition.attachment()
				.filename(DOWNLOAD_WIN_FILENAME, StandardCharsets.UTF_8)
				.build();
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.body(body);
	}

	@GetMapping("/downloads/nine-universe-0.1.5.dmg")
	public ResponseEntity<Resource> desktopInstallerMac() {
		Resource body = resolveMacInstallerBody();
		if (body == null) {
			return ResponseEntity.notFound().build();
		}
		ContentDisposition disposition = ContentDisposition.attachment()
				.filename(DOWNLOAD_MAC_FILENAME, StandardCharsets.UTF_8)
				.build();
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.body(body);
	}

	@GetMapping("/downloads/nine-universe-setup-0.1.4.exe")
	public ResponseEntity<Void> redirect014WindowsInstaller() {
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/downloads/nine-universe-setup-0.1.5.exe")
						.build()
						.toUri())
				.build();
	}

	@GetMapping("/downloads/nine-universe-setup-0.1.3.exe")
	public ResponseEntity<Void> redirect013WindowsInstaller() {
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/downloads/nine-universe-setup-0.1.5.exe")
						.build()
						.toUri())
				.build();
	}

	@GetMapping("/downloads/nine-universe-setup-0.1.2.exe")
	public ResponseEntity<Void> redirect012WindowsInstaller() {
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/downloads/nine-universe-setup-0.1.5.exe")
						.build()
						.toUri())
				.build();
	}

	@GetMapping("/downloads/nine-universe-setup-0.1.1.exe")
	public ResponseEntity<Void> redirectLegacyWindowsInstaller() {
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/downloads/nine-universe-setup-0.1.5.exe")
						.build()
						.toUri())
				.build();
	}

	@GetMapping("/downloads/nine-universe-0.1.4.dmg")
	public ResponseEntity<Void> redirect014MacInstaller() {
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/downloads/nine-universe-0.1.5.dmg")
						.build()
						.toUri())
				.build();
	}

	@GetMapping("/downloads/nine-universe-0.1.3.dmg")
	public ResponseEntity<Void> redirect013MacInstaller() {
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/downloads/nine-universe-0.1.5.dmg")
						.build()
						.toUri())
				.build();
	}

	@GetMapping("/downloads/nine-universe-0.1.2.dmg")
	public ResponseEntity<Void> redirect012MacInstaller() {
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/downloads/nine-universe-0.1.5.dmg")
						.build()
						.toUri())
				.build();
	}

	@GetMapping("/downloads/nine-universe-0.1.1.dmg")
	public ResponseEntity<Void> redirectLegacyMacInstaller() {
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/downloads/nine-universe-0.1.5.dmg")
						.build()
						.toUri())
				.build();
	}

	private static Resource resolveWindowsInstallerBody() {
		return firstExistingResource(WINDOWS_INSTALLER_RESOURCE_CANDIDATES);
	}

	private static Resource resolveMacInstallerBody() {
		return firstExistingResource(MAC_INSTALLER_RESOURCE_CANDIDATES);
	}

	private static Resource firstExistingResource(String[] candidates) {
		for (String path : candidates) {
			ClassPathResource resource = new ClassPathResource(path);
			if (resource.exists()) {
				return resource;
			}
		}
		return null;
	}
}
