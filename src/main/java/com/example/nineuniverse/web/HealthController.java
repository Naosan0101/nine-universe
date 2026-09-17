package com.example.nineuniverse.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Render 等のヘルスチェック用。ログイン画面より軽く、起動直後に 200 を返す。
 */
@RestController
public class HealthController {

	@GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
	public String health() {
		return "ok";
	}
}
