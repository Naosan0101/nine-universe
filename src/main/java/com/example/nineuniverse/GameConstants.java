package com.example.nineuniverse;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;
import org.springframework.web.util.UriUtils;

/**
 * 静的画像 URL（{@code /images/cards/…}）。参照時は NFD に正規化しリソース名と一致させる。拡張子は {@code .PNG} / {@code .JPEG} にそろえる。
 */
public final class GameConstants {
	public static final String CARD_ASSET_DIR = "/images/cards/";

	/**
	 * イラストファイル名が「カード名.PNG」（スペース）と異なる（アンダースコア名など）場合、{@code image_file} をそのまま参照する。
	 * 他カードは従来どおり「カード名.PNG」優先のまま。
	 */
	private static final Set<Short> PORTRAIT_USE_DB_IMAGE_FILE_IDS = Set.of((short) 41, (short) 42, (short) 65, (short) 68);

	private static String normalizeImageExtension(String filename) {
		if (filename == null || filename.isBlank()) {
			return filename;
		}
		int dot = filename.lastIndexOf('.');
		if (dot < 0) {
			return filename;
		}
		String base = filename.substring(0, dot);
		String ext = filename.substring(dot).toLowerCase(Locale.ROOT);
		if (ext.equals(".png")) {
			return base + ".PNG";
		}
		if (ext.equals(".jpg") || ext.equals(".jpeg")) {
			return base + ".JPEG";
		}
		return base + filename.substring(dot).toUpperCase(Locale.ROOT);
	}

	/**
	 * クラスパス上の実ファイル名（Git / macOS 由来は NFD になりやすい）と一致させる。
	 * NFC のまま URL 化すると、エントリ名が NFD の JAR 内リソースと一致せず 404 になる。
	 */
	private static String encCardFile(String filename) {
		if (filename == null || filename.isBlank()) {
			return "";
		}
		String trimmed = filename.trim();
		String nfd = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
		String normalized = normalizeImageExtension(nfd);
		return CARD_ASSET_DIR + UriUtils.encodePathSegment(normalized, StandardCharsets.UTF_8);
	}

	/**
	 * リポジトリ内の固定 PNG（カードうら・パック絵など）用。Windows ではファイル名が NFC のことが多く、{@link #encCardFile} だと 404 になる。
	 */
	private static String encCardFileNfc(String filename) {
		if (filename == null || filename.isBlank()) {
			return "";
		}
		String normalized = normalizeImageExtension(filename.trim());
		return CARD_ASSET_DIR + UriUtils.encodePathSegment(normalized, StandardCharsets.UTF_8);
	}

	/** ①カード基盤 */
	public static final String CARD_LAYER_BASE = encCardFile("カード基盤.PNG");

	/** ③カード基礎データ（装飾・枠・最前面の画像レイヤー） */
	public static final String CARD_LAYER_DATA = encCardFile("カード基礎データ.PNG");

	/** ③カード基礎データ（フィールド用） */
	public static final String CARD_LAYER_DATA_FIELD = encCardFile("カード基礎データ_フィールド.PNG");

	/** ②種族バー（ファイル名は NFC 想定で {@link #encCardFileNfc}） */
	public static String cardLayerBarPath(String attribute) {
		String file = switch (attribute == null || attribute.isBlank() ? "HUMAN" : attribute.toUpperCase(Locale.ROOT)) {
			case "HUMAN" -> "人間バー.PNG";
			case "ELF" -> "エルフバー.PNG";
			case "UNDEAD" -> "アンデッドバー.PNG";
			case "DRAGON" -> "ドラゴンバー.PNG";
			case "ELF_UNDEAD" -> "エルフアンデッドバー.PNG";
			case "HUMAN_UNDEAD" -> "人間アンデッドバー.PNG";
			case "HUMAN_ELF" -> "人間エルフバー.PNG";
			case "MACHINE" -> "マシンバー.PNG";
			case "CARBUNCLE" -> "カーバンクルバー.PNG";
			default -> "人間バー.PNG";
		};
		return encCardFileNfc(file);
	}

	/**
	 * キャライラスト（DB {@code image_file} または「カード名.PNG」）。
	 * クラスパス上の実ファイル名に合わせ {@link #encCardFile}（NFD URL）を使う。
	 * NFC 名のみのファイルは読めないため、素材はリポジトリで NFD に揃えるかファイル名を DB と揃える。
	 */
	public static String cardPortraitPath(String imageFile) {
		if (imageFile == null || imageFile.isBlank()) {
			return "";
		}
		return encCardFile(imageFile);
	}

	/**
	 * カード面のイラスト層（①基盤と種族バーの間）。素材は {@code カード名.PNG}（または DB の image_file）。
	 * 種族: 人間・エルフ・アンデッド・ドラゴン・エルフアンデッド・カーバンクル（カード名とファイル名を一致させる）。
	 */
	public static String namedTribePortraitLayerPath(String attribute, String cardName) {
		if (attribute == null || cardName == null) {
			return "";
		}
		String attr = attribute.trim().toUpperCase(Locale.ROOT);
		if (!attr.equals("HUMAN")
				&& !attr.equals("ELF")
				&& !attr.equals("UNDEAD")
				&& !attr.equals("ELF_UNDEAD")
				&& !attr.equals("DRAGON")
				&& !attr.equals("CARBUNCLE")) {
			return "";
		}
		String n = cardName.trim();
		if (n.isEmpty()) {
			return "";
		}
		return encCardFile(n + ".PNG");
	}

	/**
	 * カード面のイラスト層 URL（カード ID 不明時は {@link #cardFacePortraitLayerPath(String, String, String, Short)} に {@code null} を渡すのと同じ）。
	 */
	public static String cardFacePortraitLayerPath(String attribute, String cardName, String imageFile) {
		return cardFacePortraitLayerPath(attribute, cardName, imageFile, null);
	}

	/**
	 * カード面のイラスト層 URL。
	 * <p>単一種族は通常 {@link #namedTribePortraitLayerPath}（カード名.PNG）を優先。
	 * {@link #PORTRAIT_USE_DB_IMAGE_FILE_IDS} のみ {@code image_file} を優先（静的ファイル名がカード名と異なる場合）。
	 * その URL は {@link #encCardFileNfc}（NFC）で組む。Windows 資産のファイル名が NFC のとき {@link #cardPortraitPath}（NFD）だと 404 になるため。
	 */
	public static String cardFacePortraitLayerPath(String attribute, String cardName, String imageFile, Short cardId) {
		String img = imageFile != null ? imageFile.trim() : "";
		if (cardId != null
				&& PORTRAIT_USE_DB_IMAGE_FILE_IDS.contains(cardId)
				&& !img.isEmpty()
				&& !img.equalsIgnoreCase("__missing__.PNG")) {
			return encCardFileNfc(imageFile);
		}
		String named = namedTribePortraitLayerPath(attribute, cardName);
		if (named != null && !named.isBlank()) {
			return named;
		}
		if (!img.isEmpty() && !img.equalsIgnoreCase("__missing__.PNG")) {
			return cardPortraitPath(imageFile);
		}
		return cardPortraitPath(imageFile);
	}

	/** ASCII 名に統一（Git が NFD の「カードうら.PNG」だと URL 解決が環境で不一致になりやすい）。 */
	public static final String CARD_BACK_FILE = "card-back.PNG";

	public static String cardBackUrl() {
		return encCardFileNfc(CARD_BACK_FILE);
	}

	public static final String CARD_PACK_FILE = "カードパック_イラスト.JPEG";

	public static String packImageUrl() {
		return encCardFile(CARD_PACK_FILE);
	}

	/**
	 * 購入一覧など、{@code /images/cards/} 配下のパックアート用。
	 * リポジトリ上のファイル名（Git 由来は NFD になりやすい）と一致させるため {@link #encCardFile} を使う。
	 */
	public static String packArtImageUrl(String filename) {
		return encCardFile(filename);
	}

	/** 新規登録直後の所持ジェム（初回ホームでウェルカムボーナス） */
	public static final int STARTING_COINS = 0;

	/** 新規登録時に付与する「スタンダードパック1」のストック数（1パックずつ開封） */
	public static final int STARTER_GIFT_STANDARD1_PACK_COUNT = 10;

	/** 初めてホームを開いたときに一度だけ付与 */
	public static final int WELCOME_HOME_BONUS_GEMS = 30;
	public static final int PACK_COST = 3;
	public static final int PACK_CARD_COUNT = 4;
	public static final int MISSION_REWARD_COINS = 3;

	/** ウィークリーミッション1件あたり（デイリーの約2倍） */
	public static final int MISSION_WEEKLY_REWARD_COINS = 6;

	/** おしらせ配布（処理軽量化リリース記念）の識別子。 */
	public static final String ANNOUNCEMENT_PERF_LIGHT_KEY = "perf_light_2026_04";

	public static final int ANNOUNCEMENT_PERF_LIGHT_GEMS = 10;

	/** 受け取り開始日（この日を含む）。 */
	public static final LocalDate ANNOUNCEMENT_PERF_LIGHT_START = LocalDate.of(2026, 4, 14);

	/**
	 * 受け取り終了日（この日を含む）。開始日から 30 日間。
	 */
	public static final LocalDate ANNOUNCEMENT_PERF_LIGHT_LAST_DAY =
			ANNOUNCEMENT_PERF_LIGHT_START.plusDays(30 - 1);

	/** おしらせ配布（時間パックゲージ実装のおしらせ） */
	public static final String ANNOUNCEMENT_TIME_PACK_KEY = "time_pack_gauge_2026_04";

	public static final int ANNOUNCEMENT_TIME_PACK_GEMS = 10;

	public static final LocalDate ANNOUNCEMENT_TIME_PACK_START = LocalDate.of(2026, 4, 14);

	public static final LocalDate ANNOUNCEMENT_TIME_PACK_LAST_DAY =
			ANNOUNCEMENT_TIME_PACK_START.plusDays(30 - 1);

	/** おしらせ配布（UI・ミッション見直し＆カードバランス調整） */
	public static final String ANNOUNCEMENT_BALANCE_UI_MISSION_KEY = "balance_ui_mission_2026_04";

	public static final int ANNOUNCEMENT_BALANCE_UI_MISSION_GEMS = 10;

	public static final LocalDate ANNOUNCEMENT_BALANCE_UI_MISSION_START = LocalDate.of(2026, 4, 14);

	public static final LocalDate ANNOUNCEMENT_BALANCE_UI_MISSION_LAST_DAY =
			ANNOUNCEMENT_BALANCE_UI_MISSION_START.plusDays(30 - 1);

	/** おしらせ配布（カードパック排出率調整） */
	public static final String ANNOUNCEMENT_PACK_RATES_KEY = "pack_rates_2026_04";

	public static final int ANNOUNCEMENT_PACK_RATES_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_PACK_RATES_START = LocalDate.of(2026, 4, 14);

	public static final LocalDate ANNOUNCEMENT_PACK_RATES_LAST_DAY =
			ANNOUNCEMENT_PACK_RATES_START.plusDays(30 - 1);

	/** おしらせ配布（パック結果「もう一度引く」ボタン追加） */
	public static final String ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_KEY = "pack_result_draw_again_2026_04";

	public static final int ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_GEMS = 5;

	public static final LocalDate ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_START = LocalDate.of(2026, 4, 14);

	public static final LocalDate ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_LAST_DAY =
			ANNOUNCEMENT_PACK_RESULT_DRAW_AGAIN_START.plusDays(30 - 1);

	/** おしらせ配布（「隊長」カードのテキスト修正） */
	public static final String ANNOUNCEMENT_CAPTAIN_TEXT_KEY = "captain_text_2026_04";

	public static final int ANNOUNCEMENT_CAPTAIN_TEXT_GEMS = 5;

	public static final LocalDate ANNOUNCEMENT_CAPTAIN_TEXT_START = LocalDate.of(2026, 4, 14);

	public static final LocalDate ANNOUNCEMENT_CAPTAIN_TEXT_LAST_DAY =
			ANNOUNCEMENT_CAPTAIN_TEXT_START.plusDays(30 - 1);

	/** おしらせ配布（ミッション達成不具合の修正） */
	public static final String ANNOUNCEMENT_MISSION_FIX_KEY = "mission_fix_2026_04";

	public static final int ANNOUNCEMENT_MISSION_FIX_GEMS = 5;

	public static final LocalDate ANNOUNCEMENT_MISSION_FIX_START = LocalDate.of(2026, 4, 14);

	public static final LocalDate ANNOUNCEMENT_MISSION_FIX_LAST_DAY =
			ANNOUNCEMENT_MISSION_FIX_START.plusDays(30 - 1);

	/** おしらせ配布（カード効果テキスト修正: ダークドラゴン/エルフの巫女/ウッドエルフ） */
	public static final String ANNOUNCEMENT_CARD_TEXT_FIX_KEY = "card_text_fix_2026_04";

	public static final int ANNOUNCEMENT_CARD_TEXT_FIX_GEMS = 5;

	public static final LocalDate ANNOUNCEMENT_CARD_TEXT_FIX_START = LocalDate.of(2026, 4, 14);

	public static final LocalDate ANNOUNCEMENT_CARD_TEXT_FIX_LAST_DAY =
			ANNOUNCEMENT_CARD_TEXT_FIX_START.plusDays(30 - 1);

	/** おしらせ配布（サムライ効果不具合の修正） */
	public static final String ANNOUNCEMENT_SAMURAI_FIX_KEY = "samurai_fix_2026_04";

	public static final int ANNOUNCEMENT_SAMURAI_FIX_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_SAMURAI_FIX_START = LocalDate.of(2026, 4, 15);

	public static final LocalDate ANNOUNCEMENT_SAMURAI_FIX_LAST_DAY =
			ANNOUNCEMENT_SAMURAI_FIX_START.plusDays(30 - 1);

	/** おしらせ配布（「カードパックを引く」ミッションでボーナスパックがカウントされない不具合の修正） */
	public static final String ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_KEY = "pack_mission_bonus_fix_2026_04";

	public static final int ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_GEMS = 5;

	public static final LocalDate ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_START = LocalDate.of(2026, 4, 15);

	public static final LocalDate ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_LAST_DAY =
			ANNOUNCEMENT_PACK_MISSION_BONUS_FIX_START.plusDays(30 - 1);

	/** おしらせ配布（祝！ユーザー登録者数30人突破！） */
	public static final String ANNOUNCEMENT_30_USERS_KEY = "celebrate_30_users_2026_04";

	public static final int ANNOUNCEMENT_30_USERS_GEMS = 20;

	public static final LocalDate ANNOUNCEMENT_30_USERS_START = LocalDate.of(2026, 4, 16);

	public static final LocalDate ANNOUNCEMENT_30_USERS_LAST_DAY =
			ANNOUNCEMENT_30_USERS_START.plusDays(30 - 1);

	/** おしらせ配布（「火炎竜」のステータス修正） */
	public static final String ANNOUNCEMENT_KAENRYU_STATUS_KEY = "kaenryu_status_2026_04";

	public static final int ANNOUNCEMENT_KAENRYU_STATUS_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_KAENRYU_STATUS_START = LocalDate.of(2026, 4, 17);

	public static final LocalDate ANNOUNCEMENT_KAENRYU_STATUS_LAST_DAY =
			ANNOUNCEMENT_KAENRYU_STATUS_START.plusDays(30 - 1);

	/** おしらせ配布（「サムライ」のステータス修正） */
	public static final String ANNOUNCEMENT_SAMURAI_STATUS_KEY = "samurai_status_2026_04";

	public static final int ANNOUNCEMENT_SAMURAI_STATUS_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_SAMURAI_STATUS_START = LocalDate.of(2026, 4, 17);

	public static final LocalDate ANNOUNCEMENT_SAMURAI_STATUS_LAST_DAY =
			ANNOUNCEMENT_SAMURAI_STATUS_START.plusDays(30 - 1);

	/** おしらせ配布（大型アップデート記念・30ジェム） */
	public static final String ANNOUNCEMENT_MAJOR_UPDATE_KEY = "major_update_2026_04";

	public static final int ANNOUNCEMENT_MAJOR_UPDATE_GEMS = 30;

	public static final LocalDate ANNOUNCEMENT_MAJOR_UPDATE_START = LocalDate.of(2026, 4, 20);

	public static final LocalDate ANNOUNCEMENT_MAJOR_UPDATE_LAST_DAY =
			ANNOUNCEMENT_MAJOR_UPDATE_START.plusDays(30 - 1);

	/** おしらせ配布（「磁力合体デンジリオン」効果不具合の修正） */
	public static final String ANNOUNCEMENT_DENZIRION_FIX_KEY = "denzirion_fix_2026_04";

	public static final int ANNOUNCEMENT_DENZIRION_FIX_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_DENZIRION_FIX_START = LocalDate.of(2026, 4, 20);

	public static final LocalDate ANNOUNCEMENT_DENZIRION_FIX_LAST_DAY =
			ANNOUNCEMENT_DENZIRION_FIX_START.plusDays(30 - 1);

	/** おしらせ配布（「忍者」で「ダークドラゴン」と入れ替わったときの進行不能の修正） */
	public static final String ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_KEY = "ninja_dark_dragon_fix_2026_04";

	public static final int ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_START = LocalDate.of(2026, 4, 20);

	public static final LocalDate ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_LAST_DAY =
			ANNOUNCEMENT_NINJA_DARK_DRAGON_FIX_START.plusDays(30 - 1);

	/** おしらせ配布（「武器庫 VV-E4-PON」と「磁力合体デンジリオン」のコスト表示・隊長連動の修正） */
	public static final String ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_KEY = "weapon_depot_denzirion_fix_2026_04";

	public static final int ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_START = LocalDate.of(2026, 4, 20);

	public static final LocalDate ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_LAST_DAY =
			ANNOUNCEMENT_WEAPON_DEPOT_DENZIRION_FIX_START.plusDays(30 - 1);

	/** おしらせ配布（〈フィールド〉無限ループ修正・表示名変更・ボーナスでスタンダードパック2） */
	public static final String ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_KEY = "field_display_settings_bonus_2026_04";

	public static final int ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_GEMS = 10;

	public static final LocalDate ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_START = LocalDate.of(2026, 4, 21);

	public static final LocalDate ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_LAST_DAY =
			ANNOUNCEMENT_FIELD_DISPLAY_SETTINGS_BONUS_START.plusDays(30 - 1);

	/** おしらせ配布（「磁力合体デンジリオン」に「ガラクタレッグ」が正しく合体するよう修正） */
	public static final String ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_KEY = "denzirion_garakuta_fusion_fix_2026_04";

	public static final int ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_START = LocalDate.of(2026, 4, 21);

	public static final LocalDate ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_LAST_DAY =
			ANNOUNCEMENT_DENZIRION_GARAKUTA_FUSION_FIX_START.plusDays(30 - 1);

	/**
	 * おしらせ配布（カードリサイクル・二つ名ガチャ・UI見直し・カード調整・アーサーアート更新・各種不具合修正のまとめ）
	 */
	public static final String ANNOUNCEMENT_PLATFORM_APR_2026_KEY = "platform_apr_2026_04_22";

	public static final int ANNOUNCEMENT_PLATFORM_APR_2026_GEMS = 20;

	public static final LocalDate ANNOUNCEMENT_PLATFORM_APR_2026_START = LocalDate.of(2026, 4, 22);

	public static final LocalDate ANNOUNCEMENT_PLATFORM_APR_2026_LAST_DAY =
			ANNOUNCEMENT_PLATFORM_APR_2026_START.plusDays(30 - 1);

	/** おしらせ配布（「フレンド」追加・「だれかと対戦」更新） */
	public static final String ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_KEY = "friend_pvp_dareka_update_2026_04_22";

	public static final int ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_GEMS = 5;

	public static final LocalDate ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_START = LocalDate.of(2026, 4, 22);

	public static final LocalDate ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_LAST_DAY =
			ANNOUNCEMENT_FRIEND_PVP_UPDATE_2026_START.plusDays(30 - 1);

	/** おしらせ配布（バランス・UI・不具合修正まとめ 2026-04-23） */
	public static final String ANNOUNCEMENT_APR_23_2026_BUNDLE_KEY = "apr_23_2026_balance_ui_bundle";

	public static final int ANNOUNCEMENT_APR_23_2026_BUNDLE_GEMS = 15;

	public static final LocalDate ANNOUNCEMENT_APR_23_2026_BUNDLE_START = LocalDate.of(2026, 4, 23);

	public static final LocalDate ANNOUNCEMENT_APR_23_2026_BUNDLE_LAST_DAY =
			ANNOUNCEMENT_APR_23_2026_BUNDLE_START.plusDays(30 - 1);

	/** おしらせ配布（「メカニック」等の対人バトル表示・プレビュー修正） */
	public static final String ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_KEY = "mechanic_pvp_preview_fix_2026_04_23";

	public static final int ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_START = LocalDate.of(2026, 4, 23);

	public static final LocalDate ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_LAST_DAY =
			ANNOUNCEMENT_MECHANIC_PVP_PREVIEW_FIX_2026_START.plusDays(30 - 1);

	/** おしらせ配布（「剣闘士」〈配置〉をテキスト通りの挙動に修正） */
	public static final String ANNOUNCEMENT_KENTOSHI_FIX_2026_KEY = "kentoshi_deploy_fix_2026_04_24";

	public static final int ANNOUNCEMENT_KENTOSHI_FIX_2026_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_KENTOSHI_FIX_2026_START = LocalDate.of(2026, 4, 24);

	public static final LocalDate ANNOUNCEMENT_KENTOSHI_FIX_2026_LAST_DAY =
			ANNOUNCEMENT_KENTOSHI_FIX_2026_START.plusDays(30 - 1);

	/** おしらせ配布（「薬売り」〈配置〉の効果修正） */
	public static final String ANNOUNCEMENT_KUSURI_FIX_2026_KEY = "kusuri_effect_fix_2026_04_25";

	public static final int ANNOUNCEMENT_KUSURI_FIX_2026_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_KUSURI_FIX_2026_START = LocalDate.of(2026, 4, 25);

	public static final LocalDate ANNOUNCEMENT_KUSURI_FIX_2026_LAST_DAY =
			ANNOUNCEMENT_KUSURI_FIX_2026_START.plusDays(30 - 1);

	/**
	 * ログイン時ポップアップ「もう表示しない」用（ジェム受け取りとは別行）。
	 */
	public static final String ANNOUNCEMENT_MAJOR_UPDATE_POPUP_SUPPRESS_KEY = "major_update_popup_suppress_2026_04";

	/**
	 * おしらせモーダルで「新規ユーザー」に古い項目を出さないための判定。
	 * 登録からこの日数以内を新規とみなし、{@link #announcementVisibleInNewUserWindow} と組み合わせる。
	 */
	public static final int ANNOUNCEMENT_NEW_USER_ACCOUNT_MAX_AGE_DAYS = 14;

	/** 新規ユーザーには、開始日が「今日から遡ってこの日数以内」のおしらせだけを表示する。 */
	public static final int ANNOUNCEMENT_NEW_USER_VISIBLE_LOOKBACK_DAYS = 14;

	/**
	 * 登録から {@link #ANNOUNCEMENT_NEW_USER_ACCOUNT_MAX_AGE_DAYS} 日以内なら、おしらせ一覧を直近分に制限する対象。
	 */
	public static boolean isNewUserForAnnouncementList(LocalDate today, LocalDateTime createdAt, ZoneId zone) {
		if (createdAt == null || zone == null) {
			return false;
		}
		LocalDate reg = createdAt.atZone(zone).toLocalDate();
		return !reg.isBefore(today.minusDays(ANNOUNCEMENT_NEW_USER_ACCOUNT_MAX_AGE_DAYS));
	}

	/**
	 * 新規ユーザー向けおしらせの表示可否（開始日が直近 {@link #ANNOUNCEMENT_NEW_USER_VISIBLE_LOOKBACK_DAYS} 日以内）。
	 */
	public static boolean announcementVisibleInNewUserWindow(LocalDate today, LocalDate announcementStart) {
		if (today == null || announcementStart == null) {
			return false;
		}
		LocalDate cutoff = today.minusDays(ANNOUNCEMENT_NEW_USER_VISIBLE_LOOKBACK_DAYS);
		return !announcementStart.isBefore(cutoff);
	}

	/** おしらせカードを出すか（既存ユーザーは常に候補を見る／新規は直近開始のものだけ）。 */
	public static boolean shouldListAnnouncementForUser(
			LocalDate today, LocalDateTime userCreatedAt, ZoneId zone, LocalDate announcementStart) {
		if (!isNewUserForAnnouncementList(today, userCreatedAt, zone)) {
			return true;
		}
		return announcementVisibleInNewUserWindow(today, announcementStart);
	}

	/** ホームの無料スタンダードパック用ゲージが MAX になるまでの時間（ミリ秒） */
	public static final long TIME_PACK_CYCLE_DURATION_MS = 12L * 60 * 60 * 1000;

	/**
	 * おしらせの未読バッジ用。文言や項目を増やしたら値を変えてクライアントの既読をリセットする。
	 */
	public static final String ANNOUNCEMENT_UI_EPOCH = "2026-04-26";

	/** リサイクル：レア度ごとに得るクリスタル（1枚あたり） */
	public static final int RECYCLE_CRYSTAL_PER_CARD_C = 20;
	public static final int RECYCLE_CRYSTAL_PER_CARD_R = 100;
	public static final int RECYCLE_CRYSTAL_PER_CARD_EP = 500;
	public static final int RECYCLE_CRYSTAL_PER_CARD_REG = 1500;

	/** 300クリスタルで1ジェムに変換 */
	public static final int RECYCLE_CRYSTAL_PER_GEM = 300;

	public static final int RECYCLE_SHOP_LEGENDARY_PICK_CRYSTAL = 10000;
	public static final int RECYCLE_SHOP_LEGENDARY_PACK_CRYSTAL = 5000;
	public static final int RECYCLE_SHOP_EPIC_PLUS_PACK_CRYSTAL = 2000;

	/** 二つ名ガチャ1回 */
	public static final int RECYCLE_SHOP_EPITHET_GACHA_CRYSTAL = 500;

	private GameConstants() {
	}
}
