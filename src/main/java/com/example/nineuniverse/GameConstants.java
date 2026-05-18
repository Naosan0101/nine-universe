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
 * 静的画像 URL（{@code /images/cards/…}）。レイヤー素材（基盤・基礎データなど）は NFD に正規化（Git / macOS 由来 JAR エントリと一致）。
 * キャライラストの主 URL も NFD（{@link #encCardFile}）で、リポジトリ上のファイル名と一致させる。
 * Windows でのみ NFC 名のファイルを置いた場合は {@link #cardFacePortraitLayerPathAltNfc}（主 URL と異なるときだけ）を併用する。
 * 拡張子は {@code .PNG} / {@code .JPEG} にそろえる。
 */
public final class GameConstants {
	public static final String CARD_ASSET_DIR = "/images/cards/";

	/**
	 * 「化石」がバトルからレストへ行く際に場に出るトークン〈フィールド〉。
	 * パック排出・ライブラリ一覧からは除外（{@link #excludedFromPackOpenAndLibraryListing}）。
	 */
	public static final short FOSSIL_FIELD_TRANSFORMS_TOKEN_CARD_ID = 109;

	/** シーサーペント等が生成するトークン。パック・ライブラリ一覧から除外。 */
	public static final short SWORDFISH_TOKEN_CARD_ID = 110;

	public static final short FOSSIL_FIGHTER_CARD_ID = 79;

	public static final short SEASERPENT_CARD_ID = 74;

	/** マーメイド（id=72） */
	public static final short MERMAID_FIGHTER_CARD_ID = 72;

	/** クラーケン（id=71） */
	public static final short KRAKEN_FIGHTER_CARD_ID = 71;

	/** セイレーン（id=69） */
	public static final short SIREN_FIGHTER_CARD_ID = 69;

	/** ポセイドン（id=70） */
	public static final short POSEIDON_FIGHTER_CARD_ID = 70;

	/** 深海神殿 アトランティス（〈フィールド〉・id=76） */
	public static final short ATLANTIS_FIELD_CARD_ID = 76;

	/** 天界門 ヘヴンズゲート（〈フィールド〉・id=107） */
	public static final short HEAVENS_GATE_FIELD_CARD_ID = 107;

	/** 奇跡トークン（id=112・天界門等で生成。パック・ライブラリ一覧除外） */
	public static final short MIRACLE_TOKEN_CARD_ID = 112;

	/** 堕天使ルシファー（id=115・ルシファー効果で奇跡が変化。パック・ライブラリ一覧除外） */
	public static final short FALLEN_ANGEL_LUCIFER_CARD_ID = 115;

	/** ミカエル（id=106） */
	public static final short MIKAEL_FIGHTER_CARD_ID = 106;

	/** ミカエルデッキ6枚（ミカエル〈配置〉でデッキ上に置き得る。パック・ライブラリ一覧除外） */
	public static final short MIKAEL_WRATH_CARD_ID = 116;
	public static final short MIKAEL_PUNCH_CARD_ID = 117;
	public static final short MIKAEL_STRATEGY_CARD_ID = 118;
	public static final short MIKAEL_MINION_A_CARD_ID = 119;
	public static final short MIKAEL_MINION_B_CARD_ID = 120;
	public static final short MIKAEL_FLASH_CARD_ID = 121;

	/** ルシファー（id=108） */
	public static final short LUCIFER_FIGHTER_CARD_ID = 108;

	/** ラミエル（id=99） */
	public static final short RAMIEL_FIGHTER_CARD_ID = 99;

	/** ヴァーチャー（id=100） */
	public static final short VIRTUAL_FIGHTER_CARD_ID = 100;

	/** ドミニオン（id=102） */
	public static final short DOMINION_FIGHTER_CARD_ID = 102;

	/** ドミニオンが生成する「ミニオンソルジャー」（id=113・パック・ライブラリ一覧除外） */
	public static final short MINION_SOLDIER_TOKEN_CARD_ID = 113;

	/** ドミニオンが生成する「ミニオンチャンピオン」（id=114・パック・ライブラリ一覧除外） */
	public static final short MINION_CHAMPION_TOKEN_CARD_ID = 114;

	/** 漫画家（id=96） */
	public static final short MANGAKA_FIGHTER_CARD_ID = 96;

	/** アーサー（id=43） */
	public static final short ARTHUR_FIGHTER_CARD_ID = 43;

	/** 信奉者（id=50） */
	public static final short BELIEVER_FIGHTER_CARD_ID = 50;

	/** ボットバイク（id=57） */
	public static final short BOT_BIKE_FIGHTER_CARD_ID = 57;

	/** メカニック（id=45・ルール用語「メカニック」の参照元カード） */
	public static final short MECHANIC_RULE_REFERENCE_CARD_ID = 45;

	/** 決戦の地 カムイ（〈フィールド〉・id=49） */
	public static final short KAMUI_FIELD_CARD_ID = 49;

	/** 紅蓮峡谷 フレイムガルド（〈フィールド〉・id=84） */
	public static final short FLAMEGUARD_FIELD_CARD_ID = 84;

	/** 霊園教会 デスバウンス（〈フィールド〉・id=68） */
	public static final short DEATH_BOUNCE_FIELD_CARD_ID = 68;

	/** ザドキエル（id=98） */
	public static final short ZADKIEL_FIGHTER_CARD_ID = 98;

	/** セレスティア（id=101） */
	public static final short CELESTIA_FIGHTER_CARD_ID = 101;

	/** セラフィム（id=103） */
	public static final short SERAPHIM_FIGHTER_CARD_ID = 103;

	/** ガブリエル（id=104） */
	public static final short GABRIEL_FIGHTER_CARD_ID = 104;

	/** エンジェルメイジ（id=105） */
	public static final short ANGEL_MAGE_FIGHTER_CARD_ID = 105;

	/** ドラゴンの卵（id=27） */
	public static final short DRAGON_EGG_CARD_ID = 27;

	/** ベヒモス（id=81） */
	public static final short BEHEMOTH_FIGHTER_CARD_ID = 81;

	/** ファフニール（id=83） */
	public static final short FAFNIR_FIGHTER_CARD_ID = 83;

	/** バハムート（id=82） */
	public static final short BAHAMUT_FIGHTER_CARD_ID = 82;

	/** コミックダイナソー（id=97） */
	public static final short COMIC_DINOSAUR_FIGHTER_CARD_ID = 97;

	/** インクナイト（id=86） */
	public static final short INK_KNIGHT_FIGHTER_CARD_ID = 86;

	/** スケッチャー（id=87） */
	public static final short SKETCHER_FIGHTER_CARD_ID = 87;

	/** コミックウィッチ（id=88） */
	public static final short COMIC_WITCH_FIGHTER_CARD_ID = 88;

	/** コミックヒーロー（id=91） */
	public static final short COMIC_HERO_FIGHTER_CARD_ID = 91;

	/** インクキング（id=111・キングメーカー等で生成。パック・ライブラリ一覧除外） */
	public static final short INK_KING_FIGHTER_CARD_ID = 111;

	/** キングメーカー（id=90） */
	public static final short KING_MAKER_FIGHTER_CARD_ID = 90;

	/** ページウォーカー（id=89） */
	public static final short PAGE_WALKER_FIGHTER_CARD_ID = 89;

	/** 週刊少年 CAMP（〈フィールド〉・id=93） */
	public static final short WEEKLY_SHONEN_CAMP_FIELD_CARD_ID = 93;

	/** 世界の再構築（〈フィールド〉・id=92） */
	public static final short WORLD_REBUILD_FIELD_CARD_ID = 92;

	/** ペーパーシティ（〈フィールド〉・id=95） */
	public static final short PAPER_CITY_FIELD_CARD_ID = 95;

	/** 鳥獣戯画（〈フィールド〉・id=94） */
	public static final short CHOJU_GIGA_FIELD_CARD_ID = 94;

	public static boolean excludedFromPackOpenAndLibraryListing(Short id) {
		if (id == null) {
			return false;
		}
		short v = id.shortValue();
		return v == FOSSIL_FIELD_TRANSFORMS_TOKEN_CARD_ID || v == SWORDFISH_TOKEN_CARD_ID
				|| v == INK_KING_FIGHTER_CARD_ID || v == MIRACLE_TOKEN_CARD_ID
				|| v == FALLEN_ANGEL_LUCIFER_CARD_ID
				|| v == MIKAEL_WRATH_CARD_ID || v == MIKAEL_PUNCH_CARD_ID || v == MIKAEL_STRATEGY_CARD_ID
				|| v == MIKAEL_MINION_A_CARD_ID || v == MIKAEL_MINION_B_CARD_ID || v == MIKAEL_FLASH_CARD_ID
				|| v == MINION_SOLDIER_TOKEN_CARD_ID || v == MINION_CHAMPION_TOKEN_CARD_ID;
	}

	/**
	 * カード面イラストで {@code image_file} を「カード名.PNG」より先に使う ID。
	 * 固定ファイル名（アンダースコア等）に加え、単一種族ドラゴン既定解決より DB 名を優先するカード（スタンダード3 の OT ドラゴン・複合マーフォーク等）。
	 * 単一種族エルフでも {@code image_file} を優先するカード（例: id=85 研究者フローラ）。
	 * 〈フィールド〉でローマ字ファイル名のイラストを使う OT のドラゴン系フィールド（共有画／カード名.PNG より {@code image_file} を先に）。
	 */
	private static final Set<Short> PORTRAIT_USE_DB_IMAGE_FILE_IDS = Set.of(
			(short) 41, (short) 42, (short) 65, (short) 68,
			(short) 77, (short) 78, (short) 79, (short) 80, (short) 81, (short) 82, (short) 83,
			(short) 84, (short) 85, (short) 109, (short) 110, (short) 111, (short) 112, (short) 113, (short) 114,
			(short) 115, (short) 116, (short) 117, (short) 118, (short) 119, (short) 120, (short) 121);

	/**
	 * ドラゴン＋マーフォーク複合（{@code DRAGON_MERFOLK}）／人間＋マーフォーク複合（{@code HUMAN_MERFOLK}）の共有イラスト。
	 * カード名ではなく固定ファイル名（{@code static/images/cards/}）。
	 */
	private static final String PORTRAIT_FILE_DRAGON_MERFOLK = "ドラゴンマーフォーク.PNG";
	private static final String PORTRAIT_FILE_HUMAN_MERFOLK = "人間マーフォーク.PNG";

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

	/**
	 * ②種族バー。既定は {@link #encCardFileNfc}（従来どおり）。リポジトリ上 NFD 名のみ存在する
	 * 単一種族 {@code MERFOLK} だけ {@link #encCardFile}（NFD URL）。複合のドラゴン／人間マーフォークは
	 * 素材が NFC 名のため {@link #encCardFileNfc}（{@code merfolkBarAssetUsesNfdUrl} が false）。
	 * {@code COMIC}／{@code HUMAN_COMIC} は {@link #encCardFileNfc}（リポジトリ上 NFC 名の素材）。
	 * {@code ANGEL}／{@code ANGEL_UNDEAD}／{@code DRAGON_COMIC} は、日本語ファイル名の NFC/NFD と静的配信の相性で 404 になり得るため、
	 * {@code TRIBE_BAR_ANGEL.PNG} 等の ASCII 名の同一画像を {@link #encCardFileNfc} で参照する（元の「エンジェルバー.PNG」等と同内容）。
	 */
	public static String cardLayerBarPath(String attribute) {
		String key = attribute == null ? "" : attribute.trim();
		if (key.isEmpty()) {
			key = "HUMAN";
		} else {
			key = key.toUpperCase(Locale.ROOT);
		}
		String file = switch (key) {
			case "HUMAN" -> "人間バー.PNG";
			case "ELF" -> "エルフバー.PNG";
			case "UNDEAD" -> "アンデッドバー.PNG";
			case "DRAGON" -> "ドラゴンバー.PNG";
			case "ELF_UNDEAD" -> "エルフアンデッドバー.PNG";
			case "HUMAN_UNDEAD" -> "人間アンデッドバー.PNG";
			case "HUMAN_ELF" -> "人間エルフバー.PNG";
			case "MACHINE" -> "マシンバー.PNG";
			case "CARBUNCLE" -> "カーバンクルバー.PNG";
			case "MERFOLK" -> "マーフォークバー.PNG";
			case "DRAGON_MERFOLK" -> "ドラゴンマーフォークバー.PNG";
			case "HUMAN_MERFOLK" -> "人間マーフォークバー.PNG";
			case "COMIC" -> "コミックバー.PNG";
			case "HUMAN_COMIC" -> "人間コミックバー.PNG";
			case "ANGEL" -> "TRIBE_BAR_ANGEL.PNG";
			case "ANGEL_UNDEAD" -> "TRIBE_BAR_ANGEL_UNDEAD.PNG";
			case "DRAGON_COMIC" -> "TRIBE_BAR_DRAGON_COMIC.PNG";
			default -> "人間バー.PNG";
		};
		if ("COMIC".equals(key)
				|| "HUMAN_COMIC".equals(key)
				|| "ANGEL".equals(key)
				|| "ANGEL_UNDEAD".equals(key)
				|| "DRAGON_COMIC".equals(key)) {
			return encCardFileNfc(file);
		}
		return merfolkBarAssetUsesNfdUrl(key) ? encCardFile(file) : encCardFileNfc(file);
	}

	/** 単一種族マーフォークのバー素材のみ {@link #encCardFile}（NFD URL）で配信する。 */
	private static boolean merfolkBarAssetUsesNfdUrl(String attributeKeyUpper) {
		return "MERFOLK".equals(attributeKeyUpper);
	}

	/** DB の {@code __missing__.PNG} はイラスト未着手。カード面イラスト層・サムネ URL は出さない。 */
	private static boolean isMissingImageFilePlaceholder(String imageFile) {
		if (imageFile == null || imageFile.isBlank()) {
			return false;
		}
		return imageFile.trim().equalsIgnoreCase("__missing__.PNG");
	}

	/**
	 * キャライラスト（DB {@code image_file} または「カード名.PNG」）。{@link #encCardFile}（NFD）でリポジトリ資産と一致。
	 */
	public static String cardPortraitPath(String imageFile) {
		if (imageFile == null || imageFile.isBlank()) {
			return "";
		}
		if (isMissingImageFilePlaceholder(imageFile)) {
			return "";
		}
		return encCardFile(imageFile);
	}

	/**
	 * 複合種族カードのイラスト層 URL。
	 * <ul>
	 * <li>{@code DRAGON_MERFOLK}（ドラゴン＋マーフォーク）→ ドラゴンマーフォーク.PNG</li>
	 * <li>{@code HUMAN_MERFOLK}（人間＋マーフォーク）→ 人間マーフォーク.PNG</li>
	 * </ul>
	 * 該当しないときは空。
	 * <p>{@link #isMissingImageFilePlaceholder} が真のときは空（イラスト未実装のカードでは出さない）。
	 * <p>URL は {@link #encCardFileNfc} のみ（リポジトリ上の NFC ファイル名と一致。{@code バ} の合成／分解差で NFD URL が 404 になるのを避ける）。
	 */
	private static String fixedCompositeMerfolkPortraitLayer(String attribute) {
		if (attribute == null || attribute.isBlank()) {
			return "";
		}
		String au = attribute.trim().toUpperCase(Locale.ROOT);
		if ("DRAGON_MERFOLK".equals(au)) {
			return encCardFileNfc(PORTRAIT_FILE_DRAGON_MERFOLK);
		}
		if ("HUMAN_MERFOLK".equals(au)) {
			return encCardFileNfc(PORTRAIT_FILE_HUMAN_MERFOLK);
		}
		return "";
	}

	private static String namedTribePortraitLayerPathInternal(String attribute, String cardName, boolean nfdUrl) {
		String fixed = fixedCompositeMerfolkPortraitLayer(attribute);
		if (!fixed.isBlank()) {
			return fixed;
		}
		if (attribute == null) {
			return "";
		}
		String attr = attribute.trim().toUpperCase(Locale.ROOT);
		if (cardName == null) {
			return "";
		}
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
		return nfdUrl ? encCardFile(n + ".PNG") : encCardFileNfc(n + ".PNG");
	}

	/**
	 * カード面のイラスト層（①基盤と種族バーの間）。素材は {@code カード名.PNG}（または DB の image_file）。
	 * 種族: 人間・エルフ・アンデッド・ドラゴン・エルフアンデッド・カーバンクル（カード名とファイル名を一致）、
	 * 複合のドラゴンマーフォーク・人間マーフォークは {@code ドラゴンマーフォーク.PNG} / {@code 人間マーフォーク.PNG}。
	 */
	public static String namedTribePortraitLayerPath(String attribute, String cardName) {
		return namedTribePortraitLayerPathInternal(attribute, cardName, true);
	}

	private static String portraitFileUrl(String imageFile, boolean nfdUrl) {
		if (imageFile == null || imageFile.isBlank()) {
			return "";
		}
		String t = imageFile.trim();
		return nfdUrl ? encCardFile(t) : encCardFileNfc(t);
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
	 * {@link #PORTRAIT_USE_DB_IMAGE_FILE_IDS} に含まれる ID は {@code image_file} を最優先（カード名.PNG や複合種族の共有画より前）。
	 * <p>{@code image_file} が {@code __missing__.PNG} のときは複合・単一の名前解決をせず空を返す（イラスト未実装）。
	 */
	public static String cardFacePortraitLayerPath(String attribute, String cardName, String imageFile, Short cardId) {
		return cardFacePortraitLayerPathInternal(attribute, cardName, imageFile, cardId, true);
	}

	/**
	 * 主 URL（{@link #cardFacePortraitLayerPath}）が NFD のとき、ローカルで NFC 名のみ置いた場合の予備 URL。
	 * 主と同じ文字列なら空（クライアントは data 属性を付けない）。
	 */
	public static String cardFacePortraitLayerPathAltNfc(String attribute, String cardName, String imageFile, Short cardId) {
		String primary = cardFacePortraitLayerPathInternal(attribute, cardName, imageFile, cardId, true);
		String alt = cardFacePortraitLayerPathInternal(attribute, cardName, imageFile, cardId, false);
		if (alt.isBlank() || alt.equals(primary)) {
			return "";
		}
		return alt;
	}

	private static String cardFacePortraitLayerPathInternal(
			String attribute, String cardName, String imageFile, Short cardId, boolean nfdUrl) {
		String img = imageFile != null ? imageFile.trim() : "";
		boolean missingArt = isMissingImageFilePlaceholder(imageFile);
		if (cardId != null
				&& PORTRAIT_USE_DB_IMAGE_FILE_IDS.contains(cardId)
				&& !img.isEmpty()
				&& !missingArt) {
			return encCardFileNfc(imageFile.trim());
		}
		if (!missingArt) {
			String composite = fixedCompositeMerfolkPortraitLayer(attribute);
			if (!composite.isBlank()) {
				return composite;
			}
			String named = namedTribePortraitLayerPathInternal(attribute, cardName, nfdUrl);
			if (named != null && !named.isBlank()) {
				return named;
			}
		}
		if (!img.isEmpty() && !missingArt) {
			return portraitFileUrl(imageFile, nfdUrl);
		}
		return "";
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
	 * パック絵の実ファイル名（略称なし・{@code static/images/cards/} 上のベース名＋{@code .PNG}）。
	 */
	public static final String PACK_ART_FILE_STANDARD_1 = "スタンダードパック1.PNG";
	public static final String PACK_ART_FILE_WINDY_HILL = "風吹く丘パック.PNG";
	public static final String PACK_ART_FILE_EVIL_THREAT = "邪悪なる脅威パック.PNG";
	public static final String PACK_ART_FILE_STANDARD_2 = "スタンダードパック2.PNG";
	public static final String PACK_ART_FILE_JEWEL_UTOPIA = "宝石の秘境パック.PNG";
	public static final String PACK_ART_FILE_IRON_FLEET = "鉄面の艦隊パック.PNG";
	/**
	 * 実ファイルの拡張子は {@code .PNG} にすること。{@link #normalizeImageExtension} が URL を {@code .PNG} に統一するため、
	 * リポジトリ上が {@code .png} のままだと（特に Linux）静的配信が 404 になる。
	 */
	public static final String PACK_ART_FILE_STANDARD_3 = "スタンダードパック3.PNG";
	public static final String PACK_ART_FILE_OCEAN_TIDE = "海底の潮流パック.PNG";
	public static final String PACK_ART_FILE_CREATION_SANCTUM = "創世の神域パック.PNG";

	/**
	 * 創世の神域パック絵の配信 URL（ASCII のみ）。{@link com.example.nineuniverse.web.PackArtController} が
	 * {@link #PACK_ART_FILE_CREATION_SANCTUM} をクラスパス上で NFC/NFD いずれでも解決する。
	 * {@code /images/cards/} 直リンクは環境によって 404 になりやすいためこちらを使う。
	 */
	public static final String PACK_ART_WEB_CREATION_SANCTUM = "/pack-art/creationSanctum.png";

	/**
	 * パック絵（{@code static/images/cards/〇〇パック.PNG}）を {@code /images/cards/…} で返す URL。
	 * {@link #encCardFile} と同一規則（{@link com.example.nineuniverse.web.CardImageStaticResourceConfig} の Unicode フォールバック対象）。
	 */
	public static String packArtImageWebPath(String logicalFilenameWithExtension) {
		if (logicalFilenameWithExtension == null || logicalFilenameWithExtension.isBlank()) {
			return "";
		}
		return encCardFile(logicalFilenameWithExtension.trim());
	}

	/**
	 * 開封結果など: セッションの {@code pack_last_type}（{@link com.example.nineuniverse.service.PackService.PackType} 名）からパック絵 URL。
	 * リサイクル系・二つ名ボーナス等は汎用 {@link #packImageUrl()}。
	 */
	public static String packArtImageWebPathForLastOpenedPackType(String packTypeName) {
		if (packTypeName == null || packTypeName.isBlank()) {
			return packImageUrl();
		}
		String u = packTypeName.trim().toUpperCase(Locale.ROOT);
		if (u.startsWith("RECYCLE_") || u.startsWith("BONUS_")) {
			return packImageUrl();
		}
		return switch (u) {
			case "STANDARD" -> packArtImageWebPath(PACK_ART_FILE_STANDARD_1);
			case "WINDY_HILL" -> packArtImageWebPath(PACK_ART_FILE_WINDY_HILL);
			case "EVIL_THREAT" -> packArtImageWebPath(PACK_ART_FILE_EVIL_THREAT);
			case "STANDARD_2" -> packArtImageWebPath(PACK_ART_FILE_STANDARD_2);
			case "JEWEL_UTOPIA" -> packArtImageWebPath(PACK_ART_FILE_JEWEL_UTOPIA);
			case "IRON_FLEET" -> packArtImageWebPath(PACK_ART_FILE_IRON_FLEET);
			case "STANDARD_3" -> packArtImageWebPath(PACK_ART_FILE_STANDARD_3);
			case "OCEAN_TIDE" -> packArtImageWebPath(PACK_ART_FILE_OCEAN_TIDE);
			case "CREATION_SANCTUM" -> PACK_ART_WEB_CREATION_SANCTUM;
			default -> packImageUrl();
		};
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

	/** おしらせ配布（カード表示の修正・サーバー環境の整備） */
	public static final String ANNOUNCEMENT_CARD_DISPLAY_SERVER_OPS_2026_05_KEY =
			"card_display_server_ops_2026_05_01";

	public static final int ANNOUNCEMENT_CARD_DISPLAY_SERVER_OPS_2026_05_GEMS = 15;

	public static final LocalDate ANNOUNCEMENT_CARD_DISPLAY_SERVER_OPS_2026_05_START = LocalDate.of(2026, 5, 1);

	public static final LocalDate ANNOUNCEMENT_CARD_DISPLAY_SERVER_OPS_2026_05_LAST_DAY =
			ANNOUNCEMENT_CARD_DISPLAY_SERVER_OPS_2026_05_START.plusDays(30 - 1);

	/** おしらせ配布（デスクトップ版アイコンをゲーム風アートに変更・最新版の再インストール案内） */
	public static final String ANNOUNCEMENT_DESKTOP_APP_ICON_DESKTOP01_KEY = "desktop_app_icon_desktop01_2026_05";

	public static final int ANNOUNCEMENT_DESKTOP_APP_ICON_DESKTOP01_GEMS = 10;

	public static final LocalDate ANNOUNCEMENT_DESKTOP_APP_ICON_DESKTOP01_START = LocalDate.of(2026, 5, 2);

	public static final LocalDate ANNOUNCEMENT_DESKTOP_APP_ICON_DESKTOP01_LAST_DAY =
			ANNOUNCEMENT_DESKTOP_APP_ICON_DESKTOP01_START.plusDays(30 - 1);

	/** おしらせ・配布（ユーザー登録者数80名突破記念：20ジェム＋10000クリスタル） */
	public static final String ANNOUNCEMENT_80_USERS_MILESTONE_KEY = "celebrate_80_users_2026_05";

	public static final int ANNOUNCEMENT_80_USERS_MILESTONE_GEMS = 20;

	public static final int ANNOUNCEMENT_80_USERS_MILESTONE_CRYSTAL = 10000;

	public static final LocalDate ANNOUNCEMENT_80_USERS_MILESTONE_START = LocalDate.of(2026, 5, 11);

	public static final LocalDate ANNOUNCEMENT_80_USERS_MILESTONE_LAST_DAY =
			ANNOUNCEMENT_80_USERS_MILESTONE_START.plusDays(30 - 1);

	/** おしらせ・自動配布（スタンダード3／海底の激流／創世の神域・リーグ対戦・調整まとめ：30ジェム。初回ホーム表示で付与） */
	public static final String ANNOUNCEMENT_STD3_LEAGUE_UI_UPDATE_2026_05_KEY = "std3_league_ui_update_2026_05_15";

	public static final int ANNOUNCEMENT_STD3_LEAGUE_UI_UPDATE_2026_05_GEMS = 30;

	public static final LocalDate ANNOUNCEMENT_STD3_LEAGUE_UI_UPDATE_2026_05_START = LocalDate.of(2026, 5, 15);

	public static final LocalDate ANNOUNCEMENT_STD3_LEAGUE_UI_UPDATE_2026_05_LAST_DAY =
			ANNOUNCEMENT_STD3_LEAGUE_UI_UPDATE_2026_05_START.plusDays(30 - 1);

	/** おしらせ配布（化石〈フィールド〉の表示・種族補正の修正） */
	public static final String ANNOUNCEMENT_FOSSIL_FIELD_FIX_2026_05_KEY = "fossil_field_fix_2026_05_15";

	public static final int ANNOUNCEMENT_FOSSIL_FIELD_FIX_2026_05_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_FOSSIL_FIELD_FIX_2026_05_START = LocalDate.of(2026, 5, 15);

	public static final LocalDate ANNOUNCEMENT_FOSSIL_FIELD_FIX_2026_05_LAST_DAY =
			ANNOUNCEMENT_FOSSIL_FIELD_FIX_2026_05_START.plusDays(30 - 1);

	/** おしらせ配布（ドラゴンライダーのパラメータ修正） */
	public static final String ANNOUNCEMENT_DRAGON_RIDER_PARAM_FIX_2026_05_KEY = "dragon_rider_param_fix_2026_05_15";

	public static final int ANNOUNCEMENT_DRAGON_RIDER_PARAM_FIX_2026_05_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_DRAGON_RIDER_PARAM_FIX_2026_05_START = LocalDate.of(2026, 5, 15);

	public static final LocalDate ANNOUNCEMENT_DRAGON_RIDER_PARAM_FIX_2026_05_LAST_DAY =
			ANNOUNCEMENT_DRAGON_RIDER_PARAM_FIX_2026_05_START.plusDays(30 - 1);

	/** おしらせ配布（コミックダイナソーのパラメータ・効果修正） */
	public static final String ANNOUNCEMENT_COMIC_DINOSAUR_PARAM_FIX_2026_05_KEY = "comic_dinosaur_param_fix_2026_05_15";

	public static final int ANNOUNCEMENT_COMIC_DINOSAUR_PARAM_FIX_2026_05_GEMS = 3;

	public static final LocalDate ANNOUNCEMENT_COMIC_DINOSAUR_PARAM_FIX_2026_05_START = LocalDate.of(2026, 5, 15);

	public static final LocalDate ANNOUNCEMENT_COMIC_DINOSAUR_PARAM_FIX_2026_05_LAST_DAY =
			ANNOUNCEMENT_COMIC_DINOSAUR_PARAM_FIX_2026_05_START.plusDays(30 - 1);

	/** ログイン時ポップアップ「次回から表示しない」用（配布受領記録とは別行）。 */
	public static final String ANNOUNCEMENT_80_USERS_MILESTONE_POPUP_SUPPRESS_KEY =
			"celebrate_80_users_popup_suppress_2026_05";

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
	public static final String ANNOUNCEMENT_UI_EPOCH = "2026-05-15-comic-dinosaur";

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
