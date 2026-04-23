package com.example.nineuniverse.web;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.service.AnnouncementRewardService;
import com.example.nineuniverse.service.AnnouncementRewardService.BulkGemClaimResult;
import com.example.nineuniverse.service.AnnouncementRewardService.ClaimOutcome;
import com.example.nineuniverse.service.FriendService;
import com.example.nineuniverse.service.MissionService;
import com.example.nineuniverse.service.PackService;
import com.example.nineuniverse.service.PackService.PackType;
import com.example.nineuniverse.service.PvpFriendInviteService;
import com.example.nineuniverse.service.TimePackGaugeService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final AppUserMapper appUserMapper;
	private final MissionService missionService;
	private final AnnouncementRewardService announcementRewardService;
	private final TimePackGaugeService timePackGaugeService;
	private final PackService packService;
	private final FriendService friendService;
	private final PvpFriendInviteService pvpFriendInviteService;

	@GetMapping({"/", "/home"})
	public String home(Model model, HttpSession session) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var userForAnnouncements = appUserMapper.findById(uid);
		boolean listPerfLight = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_PERF_LIGHT_START);
		boolean listTimePack = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_TIME_PACK_START);
		boolean listBalanceUi = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_START);
		boolean listPackRates = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_PACK_RATES_START);
		boolean listPackResultDrawAgain = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_START);
		boolean listCaptainText = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_START);
		boolean listMissionFix = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_MISSION_FIX_START);
		boolean listCardTextFix = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_START);
		boolean listSamuraiFix = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_SAMURAI_FIX_START);
		boolean listPackMissionBonusFix = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_START);
		boolean list30Users = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_30_USERS_START);
		boolean listKaenryuStatus = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_START);
		boolean listSamuraiStatus = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_START);
		boolean listMajorUpdate = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_START);
		boolean listDenzirionFix = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_DENZIRION_FIX_START);
		boolean listNinjaDarkDragonFix = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_START);
		boolean listWeaponDepotDenzirionFix = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_START);
		boolean listFieldDisplaySettingsBonus = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_START);
		boolean listDenzirionGarakutaFusionFix = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_START);
		boolean listPlatformApr2026 = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_START);
		boolean listFriendPvpUpdate2026 = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_START);
		boolean listApr23Bundle2026 = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_APR_23_2026_BUNDLE_START);
		boolean listMechanicPvpPreviewFix2026 = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_START);
		boolean listKentoshiFix2026 = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_KENTOSHI_FIX_2026_START);
		boolean listKusuriFix2026 = GameConstants.shouldListAnnouncementForUser(
				today, userForAnnouncements != null ? userForAnnouncements.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_KUSURI_FIX_2026_START);
		model.addAttribute("announcementListPerfLight", listPerfLight);
		model.addAttribute("announcementListTimePack", listTimePack);
		model.addAttribute("announcementListBalanceUiMission", listBalanceUi);
		model.addAttribute("announcementListPackRates", listPackRates);
		model.addAttribute("announcementListPackResultDrawAgain", listPackResultDrawAgain);
		model.addAttribute("announcementListCaptainText", listCaptainText);
		model.addAttribute("announcementListMissionFix", listMissionFix);
		model.addAttribute("announcementListCardTextFix", listCardTextFix);
		model.addAttribute("announcementListSamuraiFix", listSamuraiFix);
		model.addAttribute("announcementListPackMissionBonusFix", listPackMissionBonusFix);
		model.addAttribute("announcementList30Users", list30Users);
		model.addAttribute("announcementListKaenryuStatus", listKaenryuStatus);
		model.addAttribute("announcementListSamuraiStatus", listSamuraiStatus);
		model.addAttribute("announcementListMajorUpdate", listMajorUpdate);
		model.addAttribute("announcementListDenzirionFix", listDenzirionFix);
		model.addAttribute("announcementListNinjaDarkDragonFix", listNinjaDarkDragonFix);
		model.addAttribute("announcementListWeaponDepotDenzirionFix", listWeaponDepotDenzirionFix);
		model.addAttribute("announcementListFieldDisplaySettingsBonus", listFieldDisplaySettingsBonus);
		model.addAttribute("announcementListDenzirionGarakutaFusionFix", listDenzirionGarakutaFusionFix);
		model.addAttribute("announcementListPlatformApr2026", listPlatformApr2026);
		model.addAttribute("announcementListFriendPvpUpdate2026", listFriendPvpUpdate2026);
		model.addAttribute("announcementListApr23Bundle2026", listApr23Bundle2026);
		model.addAttribute("announcementListMechanicPvpPreviewFix2026", listMechanicPvpPreviewFix2026);
		model.addAttribute("announcementListKentoshiFix2026", listKentoshiFix2026);
		model.addAttribute("announcementListKusuriFix2026", listKusuriFix2026);

		Set<String> claimedKeys = announcementRewardService.findClaimedKeys(uid);

		boolean perfClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_PERF_LIGHT_KEY);
		boolean perfInWindow = announcementRewardService.isWithinPerfLightWindow(today);
		model.addAttribute("perfLightAnnouncementClaimed", perfClaimed);
		model.addAttribute("perfLightAnnouncementClaimable", perfInWindow && !perfClaimed);
		model.addAttribute("perfLightAnnouncementExpiredUnclaimed",
				!perfClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_PERF_LIGHT_LAST_DAY));
		model.addAttribute("perfLightAnnouncementFutureUnclaimed",
				!perfClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_PERF_LIGHT_START));
		model.addAttribute("perfLightAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_PERF_LIGHT_GEMS);

		boolean timeAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_TIME_PACK_KEY);
		boolean timeAnnInWindow = announcementRewardService.isWithinTimePackAnnouncementWindow(today);
		model.addAttribute("timePackAnnouncementClaimed", timeAnnClaimed);
		model.addAttribute("timePackAnnouncementClaimable", timeAnnInWindow && !timeAnnClaimed);
		model.addAttribute("timePackAnnouncementExpiredUnclaimed",
				!timeAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_TIME_PACK_LAST_DAY));
		model.addAttribute("timePackAnnouncementFutureUnclaimed",
				!timeAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_TIME_PACK_START));
		model.addAttribute("timePackAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_TIME_PACK_GEMS);

		boolean balanceAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_KEY);
		boolean balanceAnnInWindow = announcementRewardService.isWithinBalanceUiMissionWindow(today);
		model.addAttribute("balanceUiMissionAnnouncementClaimed", balanceAnnClaimed);
		model.addAttribute("balanceUiMissionAnnouncementClaimable", balanceAnnInWindow && !balanceAnnClaimed);
		model.addAttribute("balanceUiMissionAnnouncementExpiredUnclaimed",
				!balanceAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_LAST_DAY));
		model.addAttribute("balanceUiMissionAnnouncementFutureUnclaimed",
				!balanceAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_START));
		model.addAttribute("balanceUiMissionAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_GEMS);

		boolean packRatesAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_PACK_RATES_KEY);
		boolean packRatesAnnInWindow = announcementRewardService.isWithinPackRatesAnnouncementWindow(today);
		model.addAttribute("packRatesAnnouncementClaimed", packRatesAnnClaimed);
		model.addAttribute("packRatesAnnouncementClaimable", packRatesAnnInWindow && !packRatesAnnClaimed);
		model.addAttribute("packRatesAnnouncementExpiredUnclaimed",
				!packRatesAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_PACK_RATES_LAST_DAY));
		model.addAttribute("packRatesAnnouncementFutureUnclaimed",
				!packRatesAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_PACK_RATES_START));
		model.addAttribute("packRatesAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_PACK_RATES_GEMS);

		boolean packResultDrawAgainAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_KEY);
		boolean packResultDrawAgainAnnInWindow =
				announcementRewardService.isWithinPackResultDrawAgainAnnouncementWindow(today);
		model.addAttribute("packResultDrawAgainAnnouncementClaimed", packResultDrawAgainAnnClaimed);
		model.addAttribute("packResultDrawAgainAnnouncementClaimable",
				packResultDrawAgainAnnInWindow && !packResultDrawAgainAnnClaimed);
		model.addAttribute("packResultDrawAgainAnnouncementExpiredUnclaimed",
				!packResultDrawAgainAnnClaimed
						&& today.isAfter(GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_LAST_DAY));
		model.addAttribute("packResultDrawAgainAnnouncementFutureUnclaimed",
				!packResultDrawAgainAnnClaimed
						&& today.isBefore(GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_START));
		model.addAttribute("packResultDrawAgainAnnouncementGemAmount",
				GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_GEMS);

		boolean captainTextAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_KEY);
		boolean captainTextAnnInWindow = announcementRewardService.isWithinCaptainTextAnnouncementWindow(today);
		model.addAttribute("captainTextAnnouncementClaimed", captainTextAnnClaimed);
		model.addAttribute("captainTextAnnouncementClaimable", captainTextAnnInWindow && !captainTextAnnClaimed);
		model.addAttribute("captainTextAnnouncementExpiredUnclaimed",
				!captainTextAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_LAST_DAY));
		model.addAttribute("captainTextAnnouncementFutureUnclaimed",
				!captainTextAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_START));
		model.addAttribute("captainTextAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_GEMS);

		boolean missionFixAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_MISSION_FIX_KEY);
		boolean missionFixAnnInWindow = announcementRewardService.isWithinMissionFixAnnouncementWindow(today);
		model.addAttribute("missionFixAnnouncementClaimed", missionFixAnnClaimed);
		model.addAttribute("missionFixAnnouncementClaimable", missionFixAnnInWindow && !missionFixAnnClaimed);
		model.addAttribute("missionFixAnnouncementExpiredUnclaimed",
				!missionFixAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_MISSION_FIX_LAST_DAY));
		model.addAttribute("missionFixAnnouncementFutureUnclaimed",
				!missionFixAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_MISSION_FIX_START));
		model.addAttribute("missionFixAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_MISSION_FIX_GEMS);

		boolean cardTextFixAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_KEY);
		boolean cardTextFixAnnInWindow = announcementRewardService.isWithinCardTextFixAnnouncementWindow(today);
		model.addAttribute("cardTextFixAnnouncementClaimed", cardTextFixAnnClaimed);
		model.addAttribute("cardTextFixAnnouncementClaimable", cardTextFixAnnInWindow && !cardTextFixAnnClaimed);
		model.addAttribute("cardTextFixAnnouncementExpiredUnclaimed",
				!cardTextFixAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_LAST_DAY));
		model.addAttribute("cardTextFixAnnouncementFutureUnclaimed",
				!cardTextFixAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_START));
		model.addAttribute("cardTextFixAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_GEMS);

		boolean samuraiFixAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_SAMURAI_FIX_KEY);
		boolean samuraiFixAnnInWindow = announcementRewardService.isWithinSamuraiFixAnnouncementWindow(today);
		model.addAttribute("samuraiFixAnnouncementClaimed", samuraiFixAnnClaimed);
		model.addAttribute("samuraiFixAnnouncementClaimable", samuraiFixAnnInWindow && !samuraiFixAnnClaimed);
		model.addAttribute("samuraiFixAnnouncementExpiredUnclaimed",
				!samuraiFixAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_SAMURAI_FIX_LAST_DAY));
		model.addAttribute("samuraiFixAnnouncementFutureUnclaimed",
				!samuraiFixAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_SAMURAI_FIX_START));
		model.addAttribute("samuraiFixAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_SAMURAI_FIX_GEMS);

		boolean packMissionBonusFixAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_KEY);
		boolean packMissionBonusFixAnnInWindow =
				announcementRewardService.isWithinPackMissionBonusFixAnnouncementWindow(today);
		model.addAttribute("packMissionBonusFixAnnouncementClaimed", packMissionBonusFixAnnClaimed);
		model.addAttribute("packMissionBonusFixAnnouncementClaimable",
				packMissionBonusFixAnnInWindow && !packMissionBonusFixAnnClaimed);
		model.addAttribute("packMissionBonusFixAnnouncementExpiredUnclaimed",
				!packMissionBonusFixAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_LAST_DAY));
		model.addAttribute("packMissionBonusFixAnnouncementFutureUnclaimed",
				!packMissionBonusFixAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_START));
		model.addAttribute("packMissionBonusFixAnnouncementGemAmount",
				GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_GEMS);

		boolean celebrate30Claimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_30_USERS_KEY);
		boolean celebrate30InWindow = announcementRewardService.isWithin30UsersAnnouncementWindow(today);
		model.addAttribute("celebrate30UsersAnnouncementClaimed", celebrate30Claimed);
		model.addAttribute("celebrate30UsersAnnouncementClaimable", celebrate30InWindow && !celebrate30Claimed);
		model.addAttribute("celebrate30UsersAnnouncementExpiredUnclaimed",
				!celebrate30Claimed && today.isAfter(GameConstants.ANNOUNCEMENT_30_USERS_LAST_DAY));
		model.addAttribute("celebrate30UsersAnnouncementFutureUnclaimed",
				!celebrate30Claimed && today.isBefore(GameConstants.ANNOUNCEMENT_30_USERS_START));
		model.addAttribute("celebrate30UsersAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_30_USERS_GEMS);

		boolean kaenryuStatusAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_KEY);
		boolean kaenryuStatusAnnInWindow = announcementRewardService.isWithinKaenryuStatusAnnouncementWindow(today);
		model.addAttribute("kaenryuStatusAnnouncementClaimed", kaenryuStatusAnnClaimed);
		model.addAttribute("kaenryuStatusAnnouncementClaimable", kaenryuStatusAnnInWindow && !kaenryuStatusAnnClaimed);
		model.addAttribute("kaenryuStatusAnnouncementExpiredUnclaimed",
				!kaenryuStatusAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_LAST_DAY));
		model.addAttribute("kaenryuStatusAnnouncementFutureUnclaimed",
				!kaenryuStatusAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_START));
		model.addAttribute("kaenryuStatusAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_GEMS);

		boolean samuraiStatusAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_KEY);
		boolean samuraiStatusAnnInWindow = announcementRewardService.isWithinSamuraiStatusAnnouncementWindow(today);
		model.addAttribute("samuraiStatusAnnouncementClaimed", samuraiStatusAnnClaimed);
		model.addAttribute("samuraiStatusAnnouncementClaimable", samuraiStatusAnnInWindow && !samuraiStatusAnnClaimed);
		model.addAttribute("samuraiStatusAnnouncementExpiredUnclaimed",
				!samuraiStatusAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_LAST_DAY));
		model.addAttribute("samuraiStatusAnnouncementFutureUnclaimed",
				!samuraiStatusAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_START));
		model.addAttribute("samuraiStatusAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_GEMS);

		boolean majorUpdateAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_KEY);
		boolean majorUpdateAnnInWindow = announcementRewardService.isWithinMajorUpdateAnnouncementWindow(today);
		boolean majorUpdatePopupSuppress = announcementRewardService.hasSuppressedMajorUpdatePopup(uid);
		model.addAttribute("majorUpdateAnnouncementClaimed", majorUpdateAnnClaimed);
		model.addAttribute("majorUpdateAnnouncementClaimable", majorUpdateAnnInWindow && !majorUpdateAnnClaimed);
		model.addAttribute("majorUpdateAnnouncementExpiredUnclaimed",
				!majorUpdateAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_LAST_DAY));
		model.addAttribute("majorUpdateAnnouncementFutureUnclaimed",
				!majorUpdateAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_START));
		model.addAttribute("majorUpdateAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_GEMS);
		model.addAttribute("majorUpdateLoginPopupShow",
				listMajorUpdate && majorUpdateAnnInWindow && !majorUpdatePopupSuppress);

		boolean denzirionFixAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_DENZIRION_FIX_KEY);
		boolean denzirionFixAnnInWindow = announcementRewardService.isWithinDenzirionFixAnnouncementWindow(today);
		model.addAttribute("denzirionFixAnnouncementClaimed", denzirionFixAnnClaimed);
		model.addAttribute("denzirionFixAnnouncementClaimable", denzirionFixAnnInWindow && !denzirionFixAnnClaimed);
		model.addAttribute("denzirionFixAnnouncementExpiredUnclaimed",
				!denzirionFixAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_DENZIRION_FIX_LAST_DAY));
		model.addAttribute("denzirionFixAnnouncementFutureUnclaimed",
				!denzirionFixAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_DENZIRION_FIX_START));
		model.addAttribute("denzirionFixAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_DENZIRION_FIX_GEMS);

		boolean ninjaDarkDragonFixAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_KEY);
		boolean ninjaDarkDragonFixAnnInWindow = announcementRewardService.isWithinNinjaDarkDragonFixAnnouncementWindow(today);
		model.addAttribute("ninjaDarkDragonFixAnnouncementClaimed", ninjaDarkDragonFixAnnClaimed);
		model.addAttribute("ninjaDarkDragonFixAnnouncementClaimable", ninjaDarkDragonFixAnnInWindow && !ninjaDarkDragonFixAnnClaimed);
		model.addAttribute("ninjaDarkDragonFixAnnouncementExpiredUnclaimed",
				!ninjaDarkDragonFixAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_LAST_DAY));
		model.addAttribute("ninjaDarkDragonFixAnnouncementFutureUnclaimed",
				!ninjaDarkDragonFixAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_START));
		model.addAttribute("ninjaDarkDragonFixAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_GEMS);

		boolean weaponDepotDenzirionFixAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_KEY);
		boolean weaponDepotDenzirionFixAnnInWindow =
				announcementRewardService.isWithinWeaponDepotDenzirionFixAnnouncementWindow(today);
		model.addAttribute("weaponDepotDenzirionFixAnnouncementClaimed", weaponDepotDenzirionFixAnnClaimed);
		model.addAttribute("weaponDepotDenzirionFixAnnouncementClaimable",
				weaponDepotDenzirionFixAnnInWindow && !weaponDepotDenzirionFixAnnClaimed);
		model.addAttribute("weaponDepotDenzirionFixAnnouncementExpiredUnclaimed",
				!weaponDepotDenzirionFixAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_LAST_DAY));
		model.addAttribute("weaponDepotDenzirionFixAnnouncementFutureUnclaimed",
				!weaponDepotDenzirionFixAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_START));
		model.addAttribute("weaponDepotDenzirionFixAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_GEMS);

		boolean fieldDisplaySettingsBonusAnnClaimed =
				claimedKeys.contains(GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_KEY);
		boolean fieldDisplaySettingsBonusAnnInWindow =
				announcementRewardService.isWithinFieldDisplaySettingsBonusAnnouncementWindow(today);
		model.addAttribute("fieldDisplaySettingsBonusAnnouncementClaimed", fieldDisplaySettingsBonusAnnClaimed);
		model.addAttribute("fieldDisplaySettingsBonusAnnouncementClaimable",
				fieldDisplaySettingsBonusAnnInWindow && !fieldDisplaySettingsBonusAnnClaimed);
		model.addAttribute("fieldDisplaySettingsBonusAnnouncementExpiredUnclaimed",
				!fieldDisplaySettingsBonusAnnClaimed
						&& today.isAfter(GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_LAST_DAY));
		model.addAttribute("fieldDisplaySettingsBonusAnnouncementFutureUnclaimed",
				!fieldDisplaySettingsBonusAnnClaimed
						&& today.isBefore(GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_START));
		model.addAttribute("fieldDisplaySettingsBonusAnnouncementGemAmount",
				GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_GEMS);

		boolean denzirionGarakutaFusionFixAnnClaimed =
				claimedKeys.contains(GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_KEY);
		boolean denzirionGarakutaFusionFixAnnInWindow =
				announcementRewardService.isWithinDenzirionGarakutaFusionFixAnnouncementWindow(today);
		model.addAttribute("denzirionGarakutaFusionFixAnnouncementClaimed", denzirionGarakutaFusionFixAnnClaimed);
		model.addAttribute("denzirionGarakutaFusionFixAnnouncementClaimable",
				denzirionGarakutaFusionFixAnnInWindow && !denzirionGarakutaFusionFixAnnClaimed);
		model.addAttribute("denzirionGarakutaFusionFixAnnouncementExpiredUnclaimed",
				!denzirionGarakutaFusionFixAnnClaimed
						&& today.isAfter(GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_LAST_DAY));
		model.addAttribute("denzirionGarakutaFusionFixAnnouncementFutureUnclaimed",
				!denzirionGarakutaFusionFixAnnClaimed
						&& today.isBefore(GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_START));
		model.addAttribute("denzirionGarakutaFusionFixAnnouncementGemAmount",
				GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_GEMS);

		boolean platformApr2026AnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_KEY);
		boolean platformApr2026AnnInWindow = announcementRewardService.isWithinPlatformApr2026AnnouncementWindow(today);
		model.addAttribute("platformApr2026AnnouncementClaimed", platformApr2026AnnClaimed);
		model.addAttribute("platformApr2026AnnouncementClaimable",
				platformApr2026AnnInWindow && !platformApr2026AnnClaimed);
		model.addAttribute("platformApr2026AnnouncementExpiredUnclaimed",
				!platformApr2026AnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_LAST_DAY));
		model.addAttribute("platformApr2026AnnouncementFutureUnclaimed",
				!platformApr2026AnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_START));
		model.addAttribute("platformApr2026AnnouncementGemAmount", GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_GEMS);

		boolean friendPvpUpdateAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_KEY);
		boolean friendPvpUpdateAnnInWindow = announcementRewardService.isWithinFriendPvpUpdateAnnouncementWindow(today);
		model.addAttribute("friendPvpUpdateAnnouncementClaimed", friendPvpUpdateAnnClaimed);
		model.addAttribute("friendPvpUpdateAnnouncementClaimable",
				friendPvpUpdateAnnInWindow && !friendPvpUpdateAnnClaimed);
		model.addAttribute("friendPvpUpdateAnnouncementExpiredUnclaimed",
				!friendPvpUpdateAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_LAST_DAY));
		model.addAttribute("friendPvpUpdateAnnouncementFutureUnclaimed",
				!friendPvpUpdateAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_START));
		model.addAttribute("friendPvpUpdateAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_GEMS);

		boolean apr23BundleAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_APR_23_2026_BUNDLE_KEY);
		boolean apr23BundleAnnInWindow = announcementRewardService.isWithinApr232026BundleAnnouncementWindow(today);
		model.addAttribute("apr23BundleAnnouncementClaimed", apr23BundleAnnClaimed);
		model.addAttribute("apr23BundleAnnouncementClaimable",
				apr23BundleAnnInWindow && !apr23BundleAnnClaimed);
		model.addAttribute("apr23BundleAnnouncementExpiredUnclaimed",
				!apr23BundleAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_APR_23_2026_BUNDLE_LAST_DAY));
		model.addAttribute("apr23BundleAnnouncementFutureUnclaimed",
				!apr23BundleAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_APR_23_2026_BUNDLE_START));
		model.addAttribute("apr23BundleAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_APR_23_2026_BUNDLE_GEMS);

		boolean mechanicPvpPreviewFixAnnClaimed =
				claimedKeys.contains(GameConstants.ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_KEY);
		boolean mechanicPvpPreviewFixAnnInWindow =
				announcementRewardService.isWithinMechanicPvpPreviewFixAnnouncementWindow(today);
		model.addAttribute("mechanicPvpPreviewFixAnnouncementClaimed", mechanicPvpPreviewFixAnnClaimed);
		model.addAttribute("mechanicPvpPreviewFixAnnouncementClaimable",
				mechanicPvpPreviewFixAnnInWindow && !mechanicPvpPreviewFixAnnClaimed);
		model.addAttribute("mechanicPvpPreviewFixAnnouncementExpiredUnclaimed",
				!mechanicPvpPreviewFixAnnClaimed
						&& today.isAfter(GameConstants.ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_LAST_DAY));
		model.addAttribute("mechanicPvpPreviewFixAnnouncementFutureUnclaimed",
				!mechanicPvpPreviewFixAnnClaimed
						&& today.isBefore(GameConstants.ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_START));
		model.addAttribute("mechanicPvpPreviewFixAnnouncementGemAmount",
				GameConstants.ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_GEMS);

		boolean kentoshiFixAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_KENTOSHI_FIX_2026_KEY);
		boolean kentoshiFixAnnInWindow = announcementRewardService.isWithinKentoshiFixAnnouncementWindow(today);
		model.addAttribute("kentoshiFixAnnouncementClaimed", kentoshiFixAnnClaimed);
		model.addAttribute("kentoshiFixAnnouncementClaimable", kentoshiFixAnnInWindow && !kentoshiFixAnnClaimed);
		model.addAttribute("kentoshiFixAnnouncementExpiredUnclaimed",
				!kentoshiFixAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_KENTOSHI_FIX_2026_LAST_DAY));
		model.addAttribute("kentoshiFixAnnouncementFutureUnclaimed",
				!kentoshiFixAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_KENTOSHI_FIX_2026_START));
		model.addAttribute("kentoshiFixAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_KENTOSHI_FIX_2026_GEMS);

		boolean kusuriFixAnnClaimed = claimedKeys.contains(GameConstants.ANNOUNCEMENT_KUSURI_FIX_2026_KEY);
		boolean kusuriFixAnnInWindow = announcementRewardService.isWithinKusuriFixAnnouncementWindow(today);
		model.addAttribute("kusuriFixAnnouncementClaimed", kusuriFixAnnClaimed);
		model.addAttribute("kusuriFixAnnouncementClaimable", kusuriFixAnnInWindow && !kusuriFixAnnClaimed);
		model.addAttribute("kusuriFixAnnouncementExpiredUnclaimed",
				!kusuriFixAnnClaimed && today.isAfter(GameConstants.ANNOUNCEMENT_KUSURI_FIX_2026_LAST_DAY));
		model.addAttribute("kusuriFixAnnouncementFutureUnclaimed",
				!kusuriFixAnnClaimed && today.isBefore(GameConstants.ANNOUNCEMENT_KUSURI_FIX_2026_START));
		model.addAttribute("kusuriFixAnnouncementGemAmount", GameConstants.ANNOUNCEMENT_KUSURI_FIX_2026_GEMS);

		int announcementBulkClaimableGemTotal = 0;
		if (listPerfLight && perfInWindow && !perfClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_PERF_LIGHT_GEMS;
		}
		if (listTimePack && timeAnnInWindow && !timeAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_TIME_PACK_GEMS;
		}
		if (listBalanceUi && balanceAnnInWindow && !balanceAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_GEMS;
		}
		if (listPackRates && packRatesAnnInWindow && !packRatesAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_PACK_RATES_GEMS;
		}
		if (listPackResultDrawAgain && packResultDrawAgainAnnInWindow && !packResultDrawAgainAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_GEMS;
		}
		if (listCaptainText && captainTextAnnInWindow && !captainTextAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_GEMS;
		}
		if (listMissionFix && missionFixAnnInWindow && !missionFixAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_MISSION_FIX_GEMS;
		}
		if (listCardTextFix && cardTextFixAnnInWindow && !cardTextFixAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_GEMS;
		}
		if (listSamuraiFix && samuraiFixAnnInWindow && !samuraiFixAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_SAMURAI_FIX_GEMS;
		}
		if (listPackMissionBonusFix && packMissionBonusFixAnnInWindow && !packMissionBonusFixAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_GEMS;
		}
		if (list30Users && celebrate30InWindow && !celebrate30Claimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_30_USERS_GEMS;
		}
		if (listKaenryuStatus && kaenryuStatusAnnInWindow && !kaenryuStatusAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_GEMS;
		}
		if (listSamuraiStatus && samuraiStatusAnnInWindow && !samuraiStatusAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_GEMS;
		}
		if (listMajorUpdate && majorUpdateAnnInWindow && !majorUpdateAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_GEMS;
		}
		if (listDenzirionFix && denzirionFixAnnInWindow && !denzirionFixAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_DENZIRION_FIX_GEMS;
		}
		if (listNinjaDarkDragonFix && ninjaDarkDragonFixAnnInWindow && !ninjaDarkDragonFixAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_GEMS;
		}
		if (listWeaponDepotDenzirionFix && weaponDepotDenzirionFixAnnInWindow && !weaponDepotDenzirionFixAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_GEMS;
		}
		if (listFieldDisplaySettingsBonus && fieldDisplaySettingsBonusAnnInWindow && !fieldDisplaySettingsBonusAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_GEMS;
		}
		if (listDenzirionGarakutaFusionFix && denzirionGarakutaFusionFixAnnInWindow && !denzirionGarakutaFusionFixAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_GEMS;
		}
		if (listPlatformApr2026 && platformApr2026AnnInWindow && !platformApr2026AnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_GEMS;
		}
		if (listFriendPvpUpdate2026 && friendPvpUpdateAnnInWindow && !friendPvpUpdateAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_GEMS;
		}
		if (listApr23Bundle2026 && apr23BundleAnnInWindow && !apr23BundleAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_APR_23_2026_BUNDLE_GEMS;
		}
		if (listMechanicPvpPreviewFix2026 && mechanicPvpPreviewFixAnnInWindow && !mechanicPvpPreviewFixAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_GEMS;
		}
		if (listKentoshiFix2026 && kentoshiFixAnnInWindow && !kentoshiFixAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_KENTOSHI_FIX_2026_GEMS;
		}
		if (listKusuriFix2026 && kusuriFixAnnInWindow && !kusuriFixAnnClaimed) {
			announcementBulkClaimableGemTotal += GameConstants.ANNOUNCEMENT_KUSURI_FIX_2026_GEMS;
		}
		model.addAttribute("announcementBulkClaimableGemTotal", announcementBulkClaimableGemTotal);
		model.addAttribute("announcementAnyGemClaimable", announcementBulkClaimableGemTotal > 0);

		var gauge = timePackGaugeService.snapshotForUser(uid);
		int bonusBank = userForAnnouncements != null && userForAnnouncements.getTimePackBonusBank() != null
				? userForAnnouncements.getTimePackBonusBank() : 0;
		model.addAttribute("timePackFillPercent", gauge.fillPercent());
		model.addAttribute("timePackAvailablePacks", gauge.availablePacks() + bonusBank);
		model.addAttribute("timePackBonusBank", bonusBank);
		model.addAttribute("timePackCycleStartEpochMs", gauge.cycleStartEpochMilli());
		model.addAttribute("timePackDurationMs", gauge.durationMs());
		model.addAttribute("packArtCacheKey", PackController.getPackArtCacheKey());
		model.addAttribute("standardPackImage", GameConstants.packArtImageUrl("スタンダードパック1.PNG"));
		model.addAttribute("standard2PackImage", GameConstants.packArtImageUrl("スタンダードパック2.PNG"));
		model.addAttribute("packRarityRates", PackController.getPackRarityRatesForView());
		model.addAttribute("standardPackPreview", PackController.buildPackPreviewLines(packService, PackType.STANDARD));
		model.addAttribute("standard2PackPreview", PackController.buildPackPreviewLines(packService, PackType.STANDARD_2));
		model.addAttribute("announcementUiEpoch", GameConstants.ANNOUNCEMENT_UI_EPOCH);

		int granted = appUserMapper.grantWelcomeHomeBonusIfPending(uid, GameConstants.WELCOME_HOME_BONUS_GEMS);
		if (granted > 0) {
			model.addAttribute("welcomeHomeBonusShown", true);
			model.addAttribute("welcomeHomeBonusAmount", GameConstants.WELCOME_HOME_BONUS_GEMS);
		}
		missionService.ensureDailyMissions(uid);
		missionService.ensureWeeklyMissions(uid);
		var fresh = appUserMapper.findById(uid);
		model.addAttribute("user", fresh);
		model.addAttribute("missionHasUnclaimedReward", missionService.hasUnclaimedMissionRewards(uid));
		model.addAttribute("friendHubPendingInbound", friendService.countPendingInbound(uid) > 0);
		model.addAttribute("pvpHubPendingInbound", pvpFriendInviteService.countPendingInbound(uid) > 0);

		Object epU = session.getAttribute(PackController.SESSION_HOME_BONUS_EPITHET_UPPER);
		Object epL = session.getAttribute(PackController.SESSION_HOME_BONUS_EPITHET_LOWER);
		if (epU instanceof String || epL instanceof String) {
			session.removeAttribute(PackController.SESSION_HOME_BONUS_EPITHET_UPPER);
			session.removeAttribute(PackController.SESSION_HOME_BONUS_EPITHET_LOWER);
			model.addAttribute("homeBonusEpithetReveal", true);
			model.addAttribute("homeBonusEpithetUpper", epU instanceof String ? (String) epU : "");
			model.addAttribute("homeBonusEpithetLower", epL instanceof String ? (String) epL : "");
		} else {
			model.addAttribute("homeBonusEpithetReveal", false);
			model.addAttribute("homeBonusEpithetUpper", "");
			model.addAttribute("homeBonusEpithetLower", "");
		}

		return "home";
	}

	@PostMapping("/home/announcements/perf-light/claim")
	public String claimPerfLightAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_PERF_LIGHT_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimPerfLightBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_PERF_LIGHT_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/time-pack/claim")
	public String claimTimePackAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_TIME_PACK_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimTimePackAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_TIME_PACK_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/balance-ui-mission/claim")
	public String claimBalanceUiMissionAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimBalanceUiMissionBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/pack-rates/claim")
	public String claimPackRatesAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_PACK_RATES_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimPackRatesAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_PACK_RATES_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/pack-result-draw-again/claim")
	public String claimPackResultDrawAgainAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimPackResultDrawAgainAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/captain-text/claim")
	public String claimCaptainTextAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimCaptainTextAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/mission-fix/claim")
	public String claimMissionFixAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_MISSION_FIX_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimMissionFixAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_MISSION_FIX_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/card-text-fix/claim")
	public String claimCardTextFixAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimCardTextFixAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/samurai-fix/claim")
	public String claimSamuraiFixAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_SAMURAI_FIX_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimSamuraiFixAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_SAMURAI_FIX_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/pack-mission-bonus-fix/claim")
	public String claimPackMissionBonusFixAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimPackMissionBonusFixAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/celebrate-30-users/claim")
	public String claimCelebrate30UsersAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_30_USERS_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claim30UsersAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_30_USERS_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/kaenryu-status/claim")
	public String claimKaenryuStatusAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimKaenryuStatusAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/samurai-status/claim")
	public String claimSamuraiStatusAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimSamuraiStatusAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/denzirion-fix/claim")
	public String claimDenzirionFixAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_DENZIRION_FIX_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimDenzirionFixAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_DENZIRION_FIX_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/ninja-dark-dragon-fix/claim")
	public String claimNinjaDarkDragonFixAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimNinjaDarkDragonFixAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/weapon-depot-denzirion-fix/claim")
	public String claimWeaponDepotDenzirionFixAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimWeaponDepotDenzirionFixAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/field-display-settings-bonus/claim")
	public String claimFieldDisplaySettingsBonusAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimFieldDisplaySettingsBonusAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/denzirion-garakuta-fusion-fix/claim")
	public String claimDenzirionGarakutaFusionFixAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimDenzirionGarakutaFusionFixAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/platform-apr-2026/claim")
	public String claimPlatformApr2026Announcement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimPlatformApr2026AnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/friend-pvp-update/claim")
	public String claimFriendPvpUpdateAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimFriendPvpUpdateAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/apr-23-2026-bundle/claim")
	public String claimApr232026BundleAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_APR_23_2026_BUNDLE_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimApr232026BundleAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_APR_23_2026_BUNDLE_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/mechanic-pvp-preview-fix/claim")
	public String claimMechanicPvpPreviewFixAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone,
				GameConstants.ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimMechanicPvpPreviewFixAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/kentoshi-fix/claim")
	public String claimKentoshiFixAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_KENTOSHI_FIX_2026_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimKentoshiFixAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_KENTOSHI_FIX_2026_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/kusuri-fix/claim")
	public String claimKusuriFixAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_KUSURI_FIX_2026_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimKusuriFixAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_KUSURI_FIX_2026_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/major-update/claim")
	public String claimMajorUpdateAnnouncement(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		if (!GameConstants.shouldListAnnouncementForUser(
				today, u != null ? u.getCreatedAt() : null, zone, GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_START)) {
			ra.addFlashAttribute("flashAnnouncementError", "このおしらせは受け取り対象外です。");
			return "redirect:/home";
		}
		ClaimOutcome outcome = announcementRewardService.claimMajorUpdateAnnouncementBonus(uid);
		switch (outcome) {
			case SUCCESS -> ra.addFlashAttribute("flashAnnouncementSuccess",
					GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_GEMS + "ジェムを受け取りました。");
			case ALREADY_CLAIMED -> ra.addFlashAttribute("flashAnnouncementError", "既に受け取り済みです。");
			case NOT_YET_STARTED, EXPIRED -> ra.addFlashAttribute("flashAnnouncementError", "受け取り期限外です。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/major-update/suppress-popup")
	public String suppressMajorUpdateLoginPopup(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		announcementRewardService.suppressMajorUpdateLoginPopup(uid);
		return "redirect:/home";
	}

	@PostMapping("/home/announcements/claim-all-gems")
	public String claimAllAnnouncementGems(RedirectAttributes ra) {
		long uid = CurrentUser.require().getId();
		ZoneId zone = ZoneId.systemDefault();
		LocalDate today = LocalDate.now(zone);
		var u = appUserMapper.findById(uid);
		BulkGemClaimResult result = announcementRewardService.claimAllEligibleAnnouncementGems(
				uid, today, zone, u != null ? u.getCreatedAt() : null);
		if (result.claimedCount() > 0) {
			ra.addFlashAttribute("flashAnnouncementSuccess",
					"おしらせのジェムを一括で受け取りました（合計 " + result.totalGems() + "ジェム、" + result.claimedCount() + "件）。");
		} else {
			ra.addFlashAttribute("flashAnnouncementSuccess", "いま受け取れるジェムのおしらせはありませんでした。");
		}
		return "redirect:/home";
	}

	@PostMapping("/home/time-pack/open")
	public String openTimePack(HttpSession session, RedirectAttributes ra,
			@RequestParam(name = "pack0", required = false) String pack0) {
		long uid = CurrentUser.require().getId();
		try {
			PackType choice = parseBonusPackChoiceParam(pack0);
			var outcome = timePackGaugeService.claimOneBonusPackFromGauge(uid, choice);
			var fresh = appUserMapper.findById(uid);
			int bank = fresh != null && fresh.getTimePackBonusBank() != null ? Math.max(0, fresh.getTimePackBonusBank()) : 0;
			var gauge = timePackGaugeService.snapshotForUser(uid);
			boolean canOpenAnother = gauge.availablePacks() + bank > 0;

			if (choice == PackType.BONUS_EPITHET_GACHA) {
				session.removeAttribute(PackController.SESSION_PACK_OPENING_SLOTS);
				session.setAttribute("pack_last_pulled_ids", List.of());
				session.removeAttribute(PackController.SESSION_PACK_LAST_EPITHET_RESULTS);
				session.removeAttribute(PackController.SESSION_PACK_RESULT_FROM_BONUS_PACK);
				session.removeAttribute(PackController.SESSION_PACK_AFTER_OPEN_REDIRECT);
				var epithets = outcome.epithetResults();
				if (!epithets.isEmpty()) {
					var r = epithets.get(0);
					session.setAttribute(PackController.SESSION_HOME_BONUS_EPITHET_UPPER, r.upperGained());
					session.setAttribute(PackController.SESSION_HOME_BONUS_EPITHET_LOWER, r.lowerGained());
				}
				StringBuilder to = new StringBuilder("redirect:/home?showBonusEpithet=1");
				if (canOpenAnother) {
					to.append("&openTimePackChoice=1");
				}
				return to.toString();
			}

			session.setAttribute("pack_last_pulled_ids", outcome.flatCardIds());
			session.setAttribute(PackController.SESSION_PACK_OPENING_SLOTS, outcome.openingSlots());
			session.setAttribute(PackController.SESSION_PACK_LAST_EPITHET_RESULTS, outcome.epithetResults());
			session.setAttribute("pack_last_type", choice.name());
			session.setAttribute(PackController.SESSION_PACK_RESULT_FROM_BONUS_PACK, Boolean.TRUE);
			if (canOpenAnother) {
				session.setAttribute(PackController.SESSION_PACK_AFTER_OPEN_REDIRECT, "/home?openTimePackChoice=1");
			} else {
				session.removeAttribute(PackController.SESSION_PACK_AFTER_OPEN_REDIRECT);
			}
			return "redirect:/pack/opening";
		} catch (IllegalStateException | IllegalArgumentException e) {
			ra.addFlashAttribute("flashTimePackError", e.getMessage());
			return "redirect:/home";
		}
	}

	private static PackType parseBonusPackChoiceParam(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new IllegalArgumentException("開封するパックを選んでください。");
		}
		String s = raw.trim().toUpperCase();
		return switch (s) {
			case "STANDARD" -> PackType.STANDARD;
			case "STANDARD_2" -> PackType.STANDARD_2;
			case "BONUS_EPITHET_GACHA" -> PackType.BONUS_EPITHET_GACHA;
			default -> throw new IllegalArgumentException("開封できるのはスタンダードパック1・2、または二つ名ガチャです。");
		};
	}
}
