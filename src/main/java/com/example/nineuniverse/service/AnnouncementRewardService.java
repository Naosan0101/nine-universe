package com.example.nineuniverse.service;

import com.example.nineuniverse.GameConstants;
import com.example.nineuniverse.repository.AppUserMapper;
import com.example.nineuniverse.repository.UserAnnouncementClaimMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnnouncementRewardService {

	private final UserAnnouncementClaimMapper userAnnouncementClaimMapper;
	private final AppUserMapper appUserMapper;

	/**
	 * ホーム画面などで「複数のお知らせの受け取り済み判定」をまとめて行う用途。
	 * （個別 exists の多重発行を避ける）
	 */
	public Set<String> findClaimedKeys(long userId) {
		return new HashSet<>(userAnnouncementClaimMapper.findClaimedKeys(userId));
	}

	public boolean hasClaimedPerfLight(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_PERF_LIGHT_KEY);
	}

	public boolean hasClaimedTimePackAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_TIME_PACK_KEY);
	}

	public boolean hasClaimedBalanceUiMission(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_KEY);
	}

	public boolean hasClaimedPackRatesAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_PACK_RATES_KEY);
	}

	public boolean hasClaimedPackResultDrawAgainAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_KEY);
	}

	public boolean hasClaimedCaptainTextAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_KEY);
	}

	public boolean hasClaimedMissionFixAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_MISSION_FIX_KEY);
	}

	public boolean hasClaimedCardTextFixAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_KEY);
	}

	public boolean hasClaimedSamuraiFixAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_SAMURAI_FIX_KEY);
	}

	public boolean hasClaimedPackMissionBonusFixAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_KEY);
	}

	public boolean hasClaimed30UsersAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_30_USERS_KEY);
	}

	public boolean hasClaimedKaenryuStatusAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_KEY);
	}

	public boolean hasClaimedSamuraiStatusAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_KEY);
	}

	public boolean hasClaimedMajorUpdateAnnouncement(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_KEY);
	}

	public boolean hasSuppressedMajorUpdatePopup(long userId) {
		return userAnnouncementClaimMapper.exists(userId, GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_POPUP_SUPPRESS_KEY);
	}

	/** 受け取り可能期間内（開始日〜終了日を含む）か。 */
	public boolean isWithinPerfLightWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_PERF_LIGHT_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_PERF_LIGHT_LAST_DAY);
	}

	public boolean isWithinPerfLightWindow() {
		return isWithinPerfLightWindow(LocalDate.now(ZoneId.systemDefault()));
	}

	/** 時間パックお知らせの受け取り可能期間（開始日〜終了日を含む）。 */
	public boolean isWithinTimePackAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_TIME_PACK_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_TIME_PACK_LAST_DAY);
	}

	public boolean isWithinBalanceUiMissionWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_LAST_DAY);
	}

	public boolean isWithinPackRatesAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_PACK_RATES_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_PACK_RATES_LAST_DAY);
	}

	public boolean isWithinPackResultDrawAgainAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_LAST_DAY);
	}

	public boolean isWithinCaptainTextAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_LAST_DAY);
	}

	public boolean isWithinMissionFixAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_MISSION_FIX_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_MISSION_FIX_LAST_DAY);
	}

	public boolean isWithinCardTextFixAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_LAST_DAY);
	}

	public boolean isWithinSamuraiFixAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_SAMURAI_FIX_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_SAMURAI_FIX_LAST_DAY);
	}

	public boolean isWithinPackMissionBonusFixAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_LAST_DAY);
	}

	public boolean isWithin30UsersAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_30_USERS_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_30_USERS_LAST_DAY);
	}

	public boolean isWithinKaenryuStatusAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_LAST_DAY);
	}

	public boolean isWithinSamuraiStatusAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_LAST_DAY);
	}

	public boolean isWithinMajorUpdateAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_LAST_DAY);
	}

	public boolean isWithinDenzirionFixAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_DENZIRION_FIX_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_DENZIRION_FIX_LAST_DAY);
	}

	public boolean isWithinNinjaDarkDragonFixAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_LAST_DAY);
	}

	public boolean isWithinWeaponDepotDenzirionFixAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_LAST_DAY);
	}

	public boolean isWithinFieldDisplaySettingsBonusAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_LAST_DAY);
	}

	public boolean isWithinDenzirionGarakutaFusionFixAnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_LAST_DAY);
	}

	public boolean isWithinPlatformApr2026AnnouncementWindow(LocalDate today) {
		if (today.isBefore(GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_START)) {
			return false;
		}
		return !today.isAfter(GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_LAST_DAY);
	}

	public enum ClaimOutcome {
		SUCCESS,
		ALREADY_CLAIMED,
		NOT_YET_STARTED,
		EXPIRED
	}

	/**
	 * 個別の「お知らせ」受け取りと同条件で、ジェム配布のあるお知らせをまとめて受け取る。
	 * 対象外のお知らせはスキップし、SUCCESS だった件数と合計ジェムのみを返す。
	 */
	@Transactional
	public BulkGemClaimResult claimAllEligibleAnnouncementGems(
			long userId, LocalDate today, ZoneId zone, LocalDateTime userCreatedAt) {
		int totalGems = 0;
		int claimed = 0;

		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_PERF_LIGHT_START)) {
			if (claimPerfLightBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_PERF_LIGHT_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_TIME_PACK_START)) {
			if (claimTimePackAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_TIME_PACK_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_START)) {
			if (claimBalanceUiMissionBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_PACK_RATES_START)) {
			if (claimPackRatesAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_PACK_RATES_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_START)) {
			if (claimPackResultDrawAgainAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_MISSION_FIX_START)) {
			if (claimMissionFixAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_MISSION_FIX_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_START)) {
			if (claimCardTextFixAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_SAMURAI_FIX_START)) {
			if (claimSamuraiFixAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_SAMURAI_FIX_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_START)) {
			if (claimPackMissionBonusFixAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_30_USERS_START)) {
			if (claim30UsersAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_30_USERS_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_START)) {
			if (claimCaptainTextAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_START)) {
			if (claimKaenryuStatusAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_START)) {
			if (claimSamuraiStatusAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_START)) {
			if (claimMajorUpdateAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_DENZIRION_FIX_START)) {
			if (claimDenzirionFixAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_DENZIRION_FIX_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_START)) {
			if (claimNinjaDarkDragonFixAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_START)) {
			if (claimWeaponDepotDenzirionFixAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_START)) {
			if (claimFieldDisplaySettingsBonusAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_START)) {
			if (claimDenzirionGarakutaFusionFixAnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_GEMS;
				claimed++;
			}
		}
		if (GameConstants.shouldListAnnouncementForUser(
				today, userCreatedAt, zone, GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_START)) {
			if (claimPlatformApr2026AnnouncementBonus(userId) == ClaimOutcome.SUCCESS) {
				totalGems += GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_GEMS;
				claimed++;
			}
		}

		return new BulkGemClaimResult(totalGems, claimed);
	}

	public record BulkGemClaimResult(int totalGems, int claimedCount) {}

	@Transactional
	public ClaimOutcome claimPerfLightBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_PERF_LIGHT_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_PERF_LIGHT_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_PERF_LIGHT_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_PERF_LIGHT_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimTimePackAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_TIME_PACK_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_TIME_PACK_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_TIME_PACK_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_TIME_PACK_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimBalanceUiMissionBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_BALANCE_UI_MISSION_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimPackRatesAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_PACK_RATES_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_PACK_RATES_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_PACK_RATES_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_PACK_RATES_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimPackResultDrawAgainAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimCaptainTextAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_CAPTAIN_TEXT_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimMissionFixAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_MISSION_FIX_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_MISSION_FIX_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_MISSION_FIX_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_MISSION_FIX_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimCardTextFixAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_CARD_TEXT_FIX_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimSamuraiFixAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_SAMURAI_FIX_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_SAMURAI_FIX_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_SAMURAI_FIX_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_SAMURAI_FIX_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimPackMissionBonusFixAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(
				userId, GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claim30UsersAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_30_USERS_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_30_USERS_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_30_USERS_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_30_USERS_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimKaenryuStatusAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_KAENRYU_STATUS_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimSamuraiStatusAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_SAMURAI_STATUS_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimMajorUpdateAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimDenzirionFixAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_DENZIRION_FIX_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_DENZIRION_FIX_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_DENZIRION_FIX_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_DENZIRION_FIX_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimNinjaDarkDragonFixAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimWeaponDepotDenzirionFixAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimFieldDisplaySettingsBonusAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimDenzirionGarakutaFusionFixAnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	@Transactional
	public ClaimOutcome claimPlatformApr2026AnnouncementBonus(long userId) {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		if (today.isBefore(GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_START)) {
			return ClaimOutcome.NOT_YET_STARTED;
		}
		if (today.isAfter(GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_LAST_DAY)) {
			return ClaimOutcome.EXPIRED;
		}
		int inserted = userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_KEY);
		if (inserted == 0) {
			return ClaimOutcome.ALREADY_CLAIMED;
		}
		appUserMapper.addCoinsDelta(userId, GameConstants.ANNOUNCEMENT_PLATFORM_APR_2026_GEMS);
		return ClaimOutcome.SUCCESS;
	}

	/** 「もう表示しない」: ポップアップ抑止のみ（ジェムは別途受け取り）。 */
	@Transactional
	public void suppressMajorUpdateLoginPopup(long userId) {
		userAnnouncementClaimMapper.insertIfAbsent(userId, GameConstants.ANNOUNCEMENT_MAJOR_UPDATE_POPUP_SUPPRESS_KEY);
	}
}
