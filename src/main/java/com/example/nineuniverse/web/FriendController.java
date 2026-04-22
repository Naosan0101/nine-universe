package com.example.nineuniverse.web;

import com.example.nineuniverse.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

	private final FriendService friendService;

	@GetMapping
	public String friendsPage(Model model) {
		long uid = CurrentUser.require().getId();
		model.addAttribute("pendingInbound", friendService.listPendingInbound(uid));
		model.addAttribute("friends", friendService.listFriends(uid));
		return "friends";
	}

	@PostMapping("/request")
	public String sendRequest(@RequestParam String targetUsername, RedirectAttributes ra) {
		try {
			friendService.sendRequest(CurrentUser.require().getId(), targetUsername);
			ra.addFlashAttribute("flashFriendSuccess", "フレンド申請を送りました。");
		} catch (Exception e) {
			ra.addFlashAttribute("flashFriendError", e.getMessage());
		}
		return "redirect:/friends";
	}

	@PostMapping("/accept")
	public String accept(@RequestParam long requestId, RedirectAttributes ra) {
		try {
			friendService.acceptRequest(CurrentUser.require().getId(), requestId);
			ra.addFlashAttribute("flashFriendSuccess", "フレンドになりました。");
		} catch (Exception e) {
			ra.addFlashAttribute("flashFriendError", e.getMessage());
		}
		return "redirect:/friends";
	}

	@PostMapping("/reject")
	public String reject(@RequestParam long requestId, RedirectAttributes ra) {
		try {
			friendService.rejectRequest(CurrentUser.require().getId(), requestId);
			ra.addFlashAttribute("flashFriendSuccess", "申請を拒否しました。");
		} catch (Exception e) {
			ra.addFlashAttribute("flashFriendError", e.getMessage());
		}
		return "redirect:/friends";
	}
}
