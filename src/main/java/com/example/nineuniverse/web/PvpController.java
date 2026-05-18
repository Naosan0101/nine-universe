package com.example.nineuniverse.web;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.domain.AppUser;
import com.example.nineuniverse.domain.PvpFriendInvite;
import com.example.nineuniverse.domain.UserDisplayNames;
import com.example.nineuniverse.pvp.PvpMatch;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.service.DeckService;
import com.example.nineuniverse.service.FriendService;
import com.example.nineuniverse.service.NicknameEpithetService;
import com.example.nineuniverse.service.PvpBattleService;
import com.example.nineuniverse.service.PvpFriendInviteService;
import com.example.nineuniverse.web.dto.CpuBattleChoiceRequest;
import com.example.nineuniverse.web.dto.CpuBattleCommitRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/battle/pvp")
@RequiredArgsConstructor
public class PvpController {

	private final PvpBattleService pvpBattleService;
	private final PvpFriendInviteService pvpFriendInviteService;
	private final FriendService friendService;
	private final DeckService deckService;
	private final AppUserMapper appUserMapper;
	private final NicknameEpithetService nicknameEpithetService;

	@GetMapping(value = "/pending-inbound.json", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> pendingInboundPoll() {
		pvpFriendInviteService.expireStalePendingInvites();
		long uid = CurrentUser.require().getId();
		var inbound = pvpFriendInviteService.listPendingInbound(uid);
		int count = inbound.size();
		String navigateUrl = "/battle/pvp";
		String latestFormat = null;
		if (!inbound.isEmpty()) {
			latestFormat = inbound.get(0).getFormat();
			if (PvpMatch.Format.LEAGUE.name().equals(latestFormat)) {
				navigateUrl = "/battle/pvp/league";
			} else if (PvpMatch.Format.CASUAL.name().equals(latestFormat)) {
				navigateUrl = "/battle/pvp/casual";
			}
		}
		Map<String, Object> body = new HashMap<>();
		body.put("count", count);
		body.put("navigateUrl", navigateUrl);
		if (latestFormat != null) {
			body.put("latestFormat", latestFormat);
		}
		return body;
	}

	@GetMapping
	public String pvpHub() {
		return "pvp-hub";
	}

	@GetMapping("/casual")
	public String casualMenu(Model model) {
		pvpFriendInviteService.expireStalePendingInvites();
		long uid = CurrentUser.require().getId();
		model.addAttribute("decks", deckService.listDecks(uid));
		model.addAttribute("friends", friendService.listFriends(uid));
		model.addAttribute("pvpPendingInbound",
				pvpFriendInviteService.listPendingInbound(uid, PvpMatch.Format.CASUAL));
		model.addAttribute("pvpPendingOutbound", pvpFriendInviteService.listPendingOutbound(uid));
		return "pvp-menu";
	}

	@GetMapping("/league")
	public String leagueMenu(Model model) {
		pvpFriendInviteService.expireStalePendingInvites();
		long uid = CurrentUser.require().getId();
		model.addAttribute("leagueSets", deckService.listLeagueDeckSetSummaries(uid));
		model.addAttribute("friends", friendService.listFriends(uid));
		model.addAttribute("pvpPendingInbound",
				pvpFriendInviteService.listPendingInbound(uid, PvpMatch.Format.LEAGUE));
		model.addAttribute("pvpPendingOutbound", pvpFriendInviteService.listPendingOutbound(uid));
		return "pvp-league-menu";
	}

	@PostMapping("/challenge")
	public String sendChallengeCasual(@RequestParam long friendUserId, @RequestParam long deckId, RedirectAttributes ra) {
		try {
			pvpFriendInviteService.expireStalePendingInvites();
			long uid = CurrentUser.require().getId();
			var created = pvpFriendInviteService.createInvite(uid, friendUserId, deckId, false);
			return "redirect:/battle/pvp/room/" + created.matchId();
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/battle/pvp/casual";
		}
	}

	@PostMapping("/league/challenge")
	public String sendChallengeLeague(@RequestParam long friendUserId, @RequestParam long leagueSetId, RedirectAttributes ra) {
		try {
			pvpFriendInviteService.expireStalePendingInvites();
			long uid = CurrentUser.require().getId();
			var created = pvpFriendInviteService.createInvite(uid, friendUserId, leagueSetId, true);
			return "redirect:/battle/pvp/room/" + created.matchId();
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/battle/pvp/league";
		}
	}

	@GetMapping("/room/{id}")
	public String hostRoom(@PathVariable String id, Model model, RedirectAttributes ra) {
		pvpFriendInviteService.expireStalePendingInvites();
		long uid = CurrentUser.require().getId();
		var m = pvpBattleService.get(id);
		if (m == null) {
			ra.addFlashAttribute("error", "対戦が見つかりません");
			return "redirect:/battle/pvp";
		}
		if (m.getHostUserId() != uid) {
			ra.addFlashAttribute("error", "この待機画面はホストのみが開けます");
			return "redirect:/battle/pvp";
		}
		if (pvpBattleService.isStarted(id)) {
			return "redirect:/battle/pvp/play/" + id;
		}
		if (m.getFormat() == PvpMatch.Format.LEAGUE && pvpBattleService.isGuestJoined(id)) {
			return "redirect:/battle/pvp/league-pick/" + id;
		}
		model.addAttribute("matchId", id);
		Long inviteId = pvpFriendInviteService.findPendingInviteIdForChallengerRoom(id, uid);
		model.addAttribute("pvpInviteId", inviteId);
		return "pvp-wait";
	}

	@PostMapping("/invite/cancel")
	public String cancelInvite(@RequestParam long inviteId, RedirectAttributes ra) {
		try {
			pvpFriendInviteService.cancelInvite(inviteId, CurrentUser.require().getId());
			ra.addFlashAttribute("flashPvpInfo", "対戦申し込みを取り消しました。");
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/battle/pvp";
	}

	@GetMapping("/invite/{inviteId}/join")
	public String inviteJoinForm(@PathVariable long inviteId, Model model, RedirectAttributes ra) {
		pvpFriendInviteService.expireStalePendingInvites();
		long uid = CurrentUser.require().getId();
		PvpFriendInvite inv = pvpFriendInviteService.findPendingInviteForOpponent(inviteId, uid);
		if (inv == null) {
			ra.addFlashAttribute("error", "招待が見つかりません");
			return "redirect:/battle/pvp";
		}
		var m = pvpBattleService.get(inv.getMatchId());
		if (m == null) {
			ra.addFlashAttribute("error", "この招待はすでに取り消されています");
			return "redirect:/battle/pvp";
		}
		if (pvpBattleService.isStarted(inv.getMatchId())) {
			return "redirect:/battle/pvp/play/" + inv.getMatchId();
		}
		model.addAttribute("inviteId", inviteId);
		model.addAttribute("matchId", inv.getMatchId());
		model.addAttribute("challengerName", resolveDisplayName(inv.getChallengerUserId()));
		model.addAttribute("pvpMatchFormat", m.getFormat().name());
		model.addAttribute("decks", deckService.listDecks(uid));
		model.addAttribute("leagueSets", deckService.listLeagueDeckSetSummaries(uid));
		return "pvp-invite-join";
	}

	@PostMapping("/invite/{inviteId}/join")
	public String inviteJoinSubmit(@PathVariable long inviteId,
			@RequestParam(required = false) Long deckId,
			@RequestParam(required = false) Long leagueSetId,
			RedirectAttributes ra) {
		try {
			pvpFriendInviteService.expireStalePendingInvites();
			long uid = CurrentUser.require().getId();
			PvpFriendInvite inv = pvpFriendInviteService.findPendingInviteForOpponent(inviteId, uid);
			if (inv == null) {
				ra.addFlashAttribute("error", "招待が見つかりません");
				return "redirect:/battle/pvp";
			}
			String matchId = inv.getMatchId();
			var m = pvpBattleService.get(matchId);
			if (m != null && m.getFormat() == PvpMatch.Format.LEAGUE) {
				pvpFriendInviteService.acceptInvite(inviteId, uid, null, leagueSetId);
				return "redirect:/battle/pvp/league-pick/" + matchId;
			}
			pvpFriendInviteService.acceptInvite(inviteId, uid, deckId, null);
			return "redirect:/battle/pvp/play/" + matchId;
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/battle/pvp/invite/" + inviteId + "/join";
		}
	}

	@GetMapping("/league-pick/{id}")
	public String leaguePick(@PathVariable String id, Model model, HttpServletRequest request, RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		var m = pvpBattleService.get(id);
		if (m == null) {
			ra.addFlashAttribute("error", "対戦が見つかりません");
			return "redirect:/battle/pvp";
		}
		try {
			pvpBattleService.requireParticipant(m, uid);
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/battle/pvp";
		}
		if (m.getFormat() != PvpMatch.Format.LEAGUE) {
			return "redirect:/battle/pvp/play/" + id;
		}
		if (m.getGuestUserId() == null) {
			ra.addFlashAttribute("error", "相手の参加を待っています");
			return "redirect:/battle/pvp";
		}
		if (pvpBattleService.isStarted(id)) {
			return "redirect:/battle/pvp/play/" + id;
		}
		model.addAttribute("matchId", id);
		model.addAttribute("iAmHost", m.getHostUserId() == uid);
		String leaguePickSlot1Label = "デッキ1";
		String leaguePickSlot2Label = "デッキ2";
		Long myLeagueSetId = m.getHostUserId() == uid ? m.getHostLeagueSetId() : m.getGuestLeagueSetId();
		if (myLeagueSetId != null) {
			try {
				var leagueSum = deckService.requireLeagueSummary(uid, myLeagueSetId);
				if (leagueSum.getDeckSlot1Name() != null && !leagueSum.getDeckSlot1Name().isBlank()) {
					leaguePickSlot1Label = leagueSum.getDeckSlot1Name().trim();
				}
				if (leagueSum.getDeckSlot2Name() != null && !leagueSum.getDeckSlot2Name().isBlank()) {
					leaguePickSlot2Label = leagueSum.getDeckSlot2Name().trim();
				}
			} catch (RuntimeException ignored) {
				// フォールバック: デッキ1・2
			}
		}
		model.addAttribute("leaguePickSlot1Label", leaguePickSlot1Label);
		model.addAttribute("leaguePickSlot2Label", leaguePickSlot2Label);
		String cp = request.getContextPath();
		model.addAttribute("contextPath", cp != null ? cp : "");
		return "pvp-league-pick";
	}

	@PostMapping("/invite/{inviteId}/decline")
	public String inviteDecline(@PathVariable long inviteId, RedirectAttributes ra) {
		try {
			pvpFriendInviteService.declineInvite(inviteId, CurrentUser.require().getId());
			ra.addFlashAttribute("flashPvpInfo", "対戦申し込みを断りました。");
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/battle/pvp";
	}

	@GetMapping("/join/{id}")
	public String legacyJoinRedirect(@SuppressWarnings("unused") @PathVariable String id, RedirectAttributes ra) {
		ra.addFlashAttribute("error", "URLからの参加はできません。「だれかと対戦」から招待を承諾してください。");
		return "redirect:/battle/pvp";
	}

	@GetMapping("/play/{id}")
	public String play(@PathVariable String id, Model model, RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		var m = pvpBattleService.get(id);
		if (m == null) {
			ra.addFlashAttribute("error", "対戦が見つかりません");
			return "redirect:/battle/pvp";
		}
		try {
			pvpBattleService.requireParticipant(m, uid);
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/battle/pvp";
		}
		if (!pvpBattleService.isStarted(id)) {
			if (m.getFormat() == PvpMatch.Format.LEAGUE && m.getGuestUserId() != null) {
				return "redirect:/battle/pvp/league-pick/" + id;
			}
			if (m.getHostUserId() == uid) {
				return "redirect:/battle/pvp/room/" + id;
			}
			ra.addFlashAttribute("error", "対戦がまだ始まっていません。招待を承諾してください。");
			return "redirect:/battle/pvp";
		}
		model.addAttribute("matchId", id);
		model.addAttribute("cardBack", GameConstants.cardBackUrl());
		model.addAttribute("cardPlateUrl", GameConstants.CARD_LAYER_BASE);
		model.addAttribute("cardDataUrl", GameConstants.CARD_LAYER_DATA);
		var st = m.getState();
		Long myBattleDeckId = null;
		if (st != null) {
			myBattleDeckId = m.getHostUserId() == uid ? st.getHumanSlotDeckId() : st.getCpuSlotDeckId();
		}
		model.addAttribute("myBattleDeckId", myBattleDeckId);
		boolean iAmHost = m.getHostUserId() == uid;
		String hostName = "ホスト";
		String guestName = "ゲスト";
		if (m.getGuestUserId() != null) {
			AppUser hu = appUserMapper.findById(m.getHostUserId());
			AppUser gu = appUserMapper.findById(m.getGuestUserId());
			if (hu != null) {
				hostName = UserDisplayNames.effectiveDisplayName(hu);
			}
			if (gu != null) {
				guestName = UserDisplayNames.effectiveDisplayName(gu);
			}
		}
		model.addAttribute("battleIntroMyName", iAmHost ? hostName : guestName);
		model.addAttribute("battleIntroOppName", iAmHost ? guestName : hostName);
		long myUid = iAmHost ? m.getHostUserId() : m.getGuestUserId();
		Long oppUid = iAmHost ? m.getGuestUserId() : m.getHostUserId();
		var myEp = nicknameEpithetService.resolveDisplay(myUid);
		var oppEp = oppUid != null ? nicknameEpithetService.resolveDisplay(oppUid) : NicknameEpithetService.EpithetDisplay.empty();
		model.addAttribute("battleIntroMyEpithetUpper", myEp.upperText());
		model.addAttribute("battleIntroMyEpithetLower", myEp.lowerText());
		model.addAttribute("battleIntroOppEpithetUpper", oppEp.upperText());
		model.addAttribute("battleIntroOppEpithetLower", oppEp.lowerText());
		boolean iAmFirst = false;
		if (st != null) {
			boolean humanFirst = st.isHumanGoesFirst();
			iAmFirst = iAmHost ? humanFirst : !humanFirst;
		}
		model.addAttribute("battleIntroIAmFirst", iAmFirst);
		if (oppUid != null) {
			model.addAttribute("pvpOpponentUserId", oppUid);
		}
		return "pvp-play";
	}

	private String resolveDisplayName(long userId) {
		AppUser u = appUserMapper.findById(userId);
		return UserDisplayNames.effectiveDisplayName(u);
	}

	@GetMapping("/api/{id}/ready")
	@ResponseBody
	public Map<String, Object> ready(@PathVariable String id) {
		pvpFriendInviteService.expireStalePendingInvites();
		long uid = CurrentUser.require().getId();
		var m = pvpBattleService.get(id);
		if (m == null) {
			return Map.of("started", false, "ok", false, "gone", true);
		}
		try {
			pvpBattleService.requireParticipant(m, uid);
		} catch (Exception e) {
			return Map.of("started", false, "ok", false);
		}
		boolean guestJoined = pvpBattleService.isGuestJoined(id);
		boolean started = pvpBattleService.isStarted(id);
		boolean needLeagueSlotPick = m.getFormat() == PvpMatch.Format.LEAGUE && guestJoined && !started;
		Map<String, Object> out = new HashMap<>();
		out.put("started", started);
		out.put("guestJoined", guestJoined);
		out.put("needLeagueSlotPick", needLeagueSlotPick);
		out.put("ok", true);
		out.put("gone", false);
		return out;
	}

	@PostMapping(value = "/api/{id}/league-first-slot", produces = "application/json")
	@ResponseBody
	public ResponseEntity<?> apiLeagueFirstSlot(@PathVariable String id, @RequestParam int slot) {
		try {
			pvpBattleService.submitLeagueFirstGameSlot(id, CurrentUser.require().getId(), slot);
			return ResponseEntity.ok(Map.of("ok", true));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping(value = "/api/{id}/league-next", produces = "application/json")
	@ResponseBody
	public ResponseEntity<?> apiLeagueNext(@PathVariable String id) {
		try {
			var dto = pvpBattleService.leagueNextGame(id, CurrentUser.require().getId());
			if (dto == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(dto);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping(value = "/api/{id}/state", produces = "application/json")
	@ResponseBody
	public ResponseEntity<?> apiState(@PathVariable String id) {
		try {
			var dto = pvpBattleService.stateForUser(id, CurrentUser.require().getId());
			if (dto == null) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(dto);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping(value = "/api/{id}/commit", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public ResponseEntity<?> apiCommit(@PathVariable String id, @RequestBody CpuBattleCommitRequest req) {
		try {
			var dto = pvpBattleService.commit(id, CurrentUser.require().getId(), req);
			if (dto == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(dto);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping(value = "/api/{id}/resolve", produces = "application/json")
	@ResponseBody
	public ResponseEntity<?> apiResolve(@PathVariable String id) {
		try {
			var dto = pvpBattleService.resolve(id, CurrentUser.require().getId());
			if (dto == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(dto);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping(value = "/api/{id}/choice", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public ResponseEntity<?> apiChoice(@PathVariable String id, @RequestBody CpuBattleChoiceRequest req) {
		try {
			var dto = pvpBattleService.choice(id, CurrentUser.require().getId(), req);
			if (dto == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(dto);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping(value = "/api/{id}/timeout", produces = "application/json")
	@ResponseBody
	public ResponseEntity<?> apiTimeout(@PathVariable String id) {
		try {
			var dto = pvpBattleService.timeoutTick(id, CurrentUser.require().getId());
			if (dto == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(dto);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/api/{id}/surrender")
	public String apiSurrender(@PathVariable String id, RedirectAttributes ra) {
		try {
			pvpBattleService.surrender(id, CurrentUser.require().getId());
			var m = pvpBattleService.get(id);
			if (m != null && m.getFormat() == PvpMatch.Format.LEAGUE) {
				return "redirect:/battle/pvp/play/" + id;
			}
			return "redirect:/home";
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/battle/pvp/play/" + id;
		}
	}
}
