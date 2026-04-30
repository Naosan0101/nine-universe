package com.example.nineuniverse.web;

import com.example.nineuniverse.GameConstants;
import java.text.Normalizer;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ASCII の {@code /pack-art/{key}.png} からクラスパス上の PNG を返す（旧 URL・外部リンク用）。
 * 画面テンプレートは {@link com.example.nineuniverse.GameConstants#packArtImageWebPath} の {@code /images/cards/…} を優先する。
 */
@Controller
public class PackArtController {

	private static final String CARDS_DIR = "static/images/cards/";

	private static final Map<String, String> LOGICAL_FILE_BY_KEY = Map.of(
			"standard1", GameConstants.PACK_ART_FILE_STANDARD_1,
			"standard2", GameConstants.PACK_ART_FILE_STANDARD_2,
			"windyHill", GameConstants.PACK_ART_FILE_WINDY_HILL,
			"evilThreat", GameConstants.PACK_ART_FILE_EVIL_THREAT,
			"jewelUtopia", GameConstants.PACK_ART_FILE_JEWEL_UTOPIA,
			"ironFleet", GameConstants.PACK_ART_FILE_IRON_FLEET);

	@GetMapping("/pack-art/{key}.png")
	public ResponseEntity<Resource> packArtPng(@PathVariable("key") String key) {
		String logical = LOGICAL_FILE_BY_KEY.get(key);
		if (logical == null) {
			return ResponseEntity.notFound().build();
		}
		Resource body = resolvePackResource(logical);
		if (body == null || !body.exists()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok()
				.contentType(MediaType.IMAGE_PNG)
				.header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
				.body(body);
	}

	/** NFC 名で見つからなければ NFD 名も試す（Git 由来の表記ゆれ用）。 */
	private static Resource resolvePackResource(String logicalFileName) {
		String trimmed = logicalFileName.trim();
		String nfc = Normalizer.normalize(trimmed, Normalizer.Form.NFC);
		ClassPathResource rNfc = new ClassPathResource(CARDS_DIR + nfc);
		if (rNfc.exists()) {
			return rNfc;
		}
		String nfd = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
		if (!nfd.equals(nfc)) {
			ClassPathResource rNfd = new ClassPathResource(CARDS_DIR + nfd);
			if (rNfd.exists()) {
				return rNfd;
			}
		}
		return null;
	}
}
