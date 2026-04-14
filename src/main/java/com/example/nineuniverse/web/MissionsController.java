package com.example.nineuniverse.web;

import com.example.nineuniverse.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionsController {

	private final MissionService missionService;

	@GetMapping
	public String missions(Model model) {
		long uid = CurrentUser.require().getId();
		missionService.ensureDailyMissions(uid);
		missionService.ensureWeeklyMissions(uid);
		model.addAttribute("missions", missionService.todayMissions(uid));
		model.addAttribute("weeklyMissions", missionService.currentWeekMissions(uid));
		model.addAttribute("missionHasUnclaimedReward", missionService.hasUnclaimedMissionRewards(uid));
		return "missions";
	}

	@PostMapping("/claim/daily")
	public String claimDaily(@RequestParam short slot, RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		try {
			missionService.claimDailyReward(uid, slot);
			ra.addFlashAttribute("missionMsg", "報酬を受け取りました。");
		} catch (IllegalArgumentException | IllegalStateException e) {
			ra.addFlashAttribute("missionErr", e.getMessage());
		}
		return "redirect:/missions";
	}

	@PostMapping("/claim/weekly")
	public String claimWeekly(@RequestParam short slot, RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		try {
			missionService.claimWeeklyReward(uid, slot);
			ra.addFlashAttribute("missionMsg", "報酬を受け取りました。");
		} catch (IllegalArgumentException | IllegalStateException e) {
			ra.addFlashAttribute("missionErr", e.getMessage());
		}
		return "redirect:/missions";
	}
}
