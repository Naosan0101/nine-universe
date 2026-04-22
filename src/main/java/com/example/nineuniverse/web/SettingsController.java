package com.example.nineuniverse.web;

import com.example.nineuniverse.domain.AppUser;
import com.example.nineuniverse.domain.UserDisplayNames;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.security.AccountUserDetails;
import com.example.nineuniverse.service.NicknameEpithetService;
import com.example.nineuniverse.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SettingsController {

	private final AppUserMapper appUserMapper;
	private final UserSettingsService userSettingsService;
	private final NicknameEpithetService nicknameEpithetService;

	@GetMapping("/settings")
	public String settings(Model model) {
		long uid = CurrentUser.require().getId();
		AppUser u = appUserMapper.findById(uid);
		if (u == null) {
			return "redirect:/home";
		}
		model.addAttribute("user", u);
		model.addAttribute("effectiveDisplayName", UserDisplayNames.effectiveDisplayName(u));
		model.addAttribute("cpuThinkSpeed", UserSettingsService.normalizeCpuThinkSpeed(u.getCpuThinkSpeed()));
		model.addAttribute("displayNameMaxLen", UserSettingsService.DISPLAY_NAME_MAX_LEN);
		model.addAttribute("epithetOwnedUpper", nicknameEpithetService.listOwnedUpper(uid));
		model.addAttribute("epithetOwnedLower", nicknameEpithetService.listOwnedLower(uid));
		model.addAttribute("selectedEpithetUpperId", u.getSelectedEpithetUpperId());
		model.addAttribute("selectedEpithetLowerId", u.getSelectedEpithetLowerId());
		return "settings";
	}

	@PostMapping("/settings/save")
	public String save(
			@RequestParam("displayName") String displayName,
			@RequestParam(value = "cpuThinkSpeed", required = false) String cpuThinkSpeed,
			@RequestParam("epithetUpperId") long epithetUpperId,
			@RequestParam("epithetLowerId") long epithetLowerId,
			RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		try {
			userSettingsService.updateProfileAndEpithets(uid, displayName, cpuThinkSpeed, epithetUpperId, epithetLowerId);
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.getPrincipal() instanceof AccountUserDetails d) {
				userSettingsService.refreshUserInMemory(d.getUser(), uid);
			}
			ra.addFlashAttribute("flashSettingsSuccess", "設定を保存しました。");
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("flashSettingsError", e.getMessage());
		} catch (IllegalStateException e) {
			ra.addFlashAttribute("flashSettingsError", e.getMessage());
		}
		return "redirect:/settings";
	}

	/** 二つ名のみ保存（表示名・CPU速度などは変更しない） */
	@PostMapping("/settings/save-epithets")
	public String saveEpithetsOnly(
			@RequestParam("epithetUpperId") long epithetUpperId,
			@RequestParam("epithetLowerId") long epithetLowerId,
			RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		try {
			nicknameEpithetService.updateSelection(uid, epithetUpperId, epithetLowerId);
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.getPrincipal() instanceof AccountUserDetails d) {
				userSettingsService.refreshUserInMemory(d.getUser(), uid);
			}
			ra.addFlashAttribute("flashSettingsSuccess", "二つ名を保存しました。");
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("flashSettingsError", e.getMessage());
		} catch (IllegalStateException e) {
			ra.addFlashAttribute("flashSettingsError", e.getMessage());
		}
		return "redirect:/settings";
	}
}
