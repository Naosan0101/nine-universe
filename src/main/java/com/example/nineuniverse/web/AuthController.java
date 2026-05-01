package com.example.nineuniverse.web;

import com.example.nineuniverse.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

	private final RegistrationService registrationService;

	/**
	 * {@code nu_install=1} はデスクトップ版が再インストール直前にログイン画面へ遷移するときのみ付与する。
	 * このときはセッションが残っていてもテンプレートを返し、リダイレクトでホームへ飛ばさない。
	 */
	@GetMapping("/login")
	public String login(
			Authentication authentication,
			@RequestParam(value = "nu_install", required = false) String nuInstall) {
		if (!"1".equals(nuInstall)
				&& authentication != null
				&& authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {
			return "redirect:/home";
		}
		return "login";
	}

	@GetMapping("/register")
	public String registerForm() {
		return "register";
	}

	@PostMapping("/register")
	public String register(@RequestParam String username, @RequestParam String password, RedirectAttributes ra) {
		try {
			registrationService.register(username, password);
			ra.addFlashAttribute("msg", "登録しました。ログインしてください。");
			return "redirect:/login";
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/register";
		}
	}
}
