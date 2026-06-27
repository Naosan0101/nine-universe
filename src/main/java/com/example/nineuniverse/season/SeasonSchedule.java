package com.example.nineuniverse.season;

import com.example.nineuniverse.service.PackService.PackType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 6か月区切りのシーズンと、区切り内のパック段階アンロック（各2か月）を計算する。
 * 第1区切りは 2026-06-01 ～ 2026-11-30。以降も 6/1 と 12/1 を境に交互に続く。
 */
public final class SeasonSchedule {

	public static final LocalDate FIRST_PERIOD_START = LocalDate.of(2026, 6, 1);

	private static final DateTimeFormatter UNLOCK_LABEL =
			DateTimeFormatter.ofPattern("M月d日", Locale.JAPAN);

	private SeasonSchedule() {
	}

	public record SeasonPeriod(LocalDate start, LocalDate endInclusive) {
	}

	public record PackUnlockAnnouncement(
			String key,
			LocalDate start,
			LocalDate lastDay,
			int gems,
			String bodyText) {
	}

	/** シーズン制（区切り・アンロック）が有効な日付か。 */
	public static boolean isSeasonActive(LocalDate today) {
		return today != null && !today.isBefore(FIRST_PERIOD_START);
	}

	/** 現在の6か月区切り。シーズン開始前は empty。 */
	public static SeasonPeriod currentPeriod(LocalDate today) {
		if (!isSeasonActive(today)) {
			return null;
		}
		LocalDate start = periodStartContaining(today);
		return new SeasonPeriod(start, start.plusMonths(6).minusDays(1));
	}

	/** 区切りの開始日（6/1 または 12/1）。 */
	public static LocalDate periodStartContaining(LocalDate day) {
		int year = day.getYear();
		LocalDate juneStart = LocalDate.of(year, 6, 1);
		LocalDate decStart = LocalDate.of(year, 12, 1);
		if (!day.isBefore(juneStart) && day.isBefore(decStart)) {
			return juneStart;
		}
		if (!day.isBefore(decStart)) {
			return decStart;
		}
		return LocalDate.of(year - 1, 12, 1);
	}

	/** 区切り内の段階（1=最初の2か月, 2=次の2か月, 3=最後の2か月）。シーズン前は 3（全パック解放扱い）。 */
	public static int unlockTier(LocalDate today) {
		if (SeasonUnlockContext.isFullUnlock()) {
			return 3;
		}
		SeasonPeriod period = currentPeriod(today);
		if (period == null) {
			return 3;
		}
		long months = ChronoUnit.MONTHS.between(period.start(), today);
		if (months < 2) {
			return 1;
		}
		if (months < 4) {
			return 2;
		}
		return 3;
	}

	public static LocalDate tierUnlockDate(SeasonPeriod period, int tier) {
		if (period == null || tier < 2) {
			return null;
		}
		int monthsToAdd = tier == 2 ? 2 : 4;
		return period.start().plusMonths(monthsToAdd);
	}

	public static String unlockLabelJa(LocalDate unlockDate) {
		if (unlockDate == null) {
			return "";
		}
		return unlockDate.format(UNLOCK_LABEL) + "アンロック";
	}

	public static int requiredTier(PackType type) {
		if (type == null) {
			return 1;
		}
		return switch (type) {
			case STANDARD, WINDY_HILL, EVIL_THREAT -> 1;
			case STANDARD_2, JEWEL_UTOPIA, IRON_FLEET -> 2;
			case STANDARD_3, OCEAN_TIDE, CREATION_SANCTUM -> 3;
			case BONUS_EPITHET_GACHA -> 1;
		};
	}

	public static boolean isPackUnlocked(PackType type, LocalDate today) {
		if (!isSeasonActive(today)) {
			return true;
		}
		return unlockTier(today) >= requiredTier(type);
	}

	public static LocalDate unlockDateFor(PackType type, LocalDate today) {
		SeasonPeriod period = currentPeriod(today);
		if (period == null) {
			return null;
		}
		int need = requiredTier(type);
		if (need <= 1) {
			return period.start();
		}
		return tierUnlockDate(period, need);
	}

	/** ライブラリに表示する pack_initial 集合。 */
	public static Set<String> visiblePackInitials(LocalDate today) {
		int tier = unlockTier(today);
		LinkedHashSet<String> initials = new LinkedHashSet<>();
		initials.add("STD");
		initials.add("WH");
		initials.add("ET");
		if (tier >= 2) {
			initials.add("JU");
			initials.add("IF");
		}
		if (tier >= 3) {
			initials.add("OT");
			initials.add("CS");
		}
		return initials;
	}

	public static boolean isPackInitialVisible(String packInitial, LocalDate today) {
		if (!isSeasonActive(today)) {
			return true;
		}
		String pi = packInitial != null && !packInitial.isBlank() ? packInitial.trim() : "STD";
		return visiblePackInitials(today).contains(pi);
	}

	/** リーグ対戦（最後の2か月＝段階3）。シーズン前は解放済み。 */
	public static boolean isLeagueBattleUnlocked(LocalDate today) {
		if (!isSeasonActive(today)) {
			return true;
		}
		return unlockTier(today) >= 3;
	}

	/** CPU（アドバンスド）（最初の2か月経過後＝段階2以上）。シーズン前は解放済み。 */
	public static boolean isCpuAdvancedUnlocked(LocalDate today) {
		if (!isSeasonActive(today)) {
			return true;
		}
		return unlockTier(today) >= 2;
	}

	/** 遊び方の〈フィールド〉カード説明（最初の2か月経過後＝段階2以上）。シーズン前は表示。 */
	public static boolean isHowToPlayFieldSectionVisible(LocalDate today) {
		return isCpuAdvancedUnlocked(today);
	}

	public static LocalDate leagueBattleUnlockDate(LocalDate today) {
		SeasonPeriod period = currentPeriod(today);
		if (period == null) {
			return null;
		}
		return tierUnlockDate(period, 3);
	}

	public static LocalDate cpuAdvancedUnlockDate(LocalDate today) {
		SeasonPeriod period = currentPeriod(today);
		if (period == null) {
			return null;
		}
		return tierUnlockDate(period, 2);
	}

	public static String leagueBattleUnlockHint(LocalDate today) {
		return unlockLabelJa(leagueBattleUnlockDate(today));
	}

	public static String cpuAdvancedUnlockHint(LocalDate today) {
		return unlockLabelJa(cpuAdvancedUnlockDate(today));
	}

	public static void requireLeagueBattleUnlocked(LocalDate today) {
		if (!isLeagueBattleUnlocked(today)) {
			String hint = leagueBattleUnlockHint(today);
			throw new IllegalArgumentException(
					"リーグ対戦はまだ利用できません。" + (hint.isBlank() ? "" : "（" + hint + "）"));
		}
	}

	/** リーグデッキの作成（最後の2か月＝段階3）。シーズン前は解放済み。 */
	public static boolean isLeagueDeckCreationUnlocked(LocalDate today) {
		return isLeagueBattleUnlocked(today);
	}

	public static LocalDate leagueDeckCreationUnlockDate(LocalDate today) {
		return leagueBattleUnlockDate(today);
	}

	public static String leagueDeckCreationUnlockHint(LocalDate today) {
		return unlockLabelJa(leagueDeckCreationUnlockDate(today));
	}

	public static void requireLeagueDeckCreationUnlocked(LocalDate today) {
		if (!isLeagueDeckCreationUnlocked(today)) {
			String hint = leagueDeckCreationUnlockHint(today);
			throw new IllegalArgumentException(
					"リーグデッキの作成はまだ利用できません。" + (hint.isBlank() ? "" : "（" + hint + "）"));
		}
	}

	public static void requireCpuAdvancedUnlocked(LocalDate today) {
		if (!isCpuAdvancedUnlocked(today)) {
			String hint = cpuAdvancedUnlockHint(today);
			throw new IllegalArgumentException(
					"CPU（アドバンスド）はまだ利用できません。" + (hint.isBlank() ? "" : "（" + hint + "）"));
		}
	}

	public static final int SEASON_PACK_UNLOCK_GEMS = 40;
	public static final int SEASON_PACK_UNLOCK_CLAIM_DAYS = 30;

	public static PackUnlockAnnouncement tier2Announcement(LocalDate today) {
		SeasonPeriod period = currentPeriod(today);
		if (period == null) {
			return null;
		}
		LocalDate start = tierUnlockDate(period, 2);
		if (today.isBefore(start)) {
			return null;
		}
		LocalDate last = start.plusDays(SEASON_PACK_UNLOCK_CLAIM_DAYS - 1);
		String key = "season_unlock_tier2_" + period.start();
		String body = "・「スタンダードパック2」「宝石の秘境パック」「鉄面の艦隊パック」を追加しました。";
		return new PackUnlockAnnouncement(key, start, last, SEASON_PACK_UNLOCK_GEMS, body);
	}

	public static PackUnlockAnnouncement tier3Announcement(LocalDate today) {
		SeasonPeriod period = currentPeriod(today);
		if (period == null) {
			return null;
		}
		LocalDate start = tierUnlockDate(period, 3);
		if (today.isBefore(start)) {
			return null;
		}
		LocalDate last = start.plusDays(SEASON_PACK_UNLOCK_CLAIM_DAYS - 1);
		String key = "season_unlock_tier3_" + period.start();
		String body = "・「スタンダードパック3」「海底の潮流パック」「創世の神域パック」を追加しました。";
		return new PackUnlockAnnouncement(key, start, last, SEASON_PACK_UNLOCK_GEMS, body);
	}

	/** ログイン時ポップアップ「もう表示しない」用（ジェム配布キーとは別行）。 */
	public static String seasonPackPopupSuppressKey(PackUnlockAnnouncement ann) {
		if (ann == null) {
			return "";
		}
		return ann.key() + "_popup_suppress";
	}
}
