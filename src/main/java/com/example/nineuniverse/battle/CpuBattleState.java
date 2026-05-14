package com.example.nineuniverse.battle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CpuBattleState implements Serializable {
	/** 対人戦（ゲストは状態上の cpu 側だが人間が操作する） */
	private boolean pvp;
	/** CPU戦のみ。デッキ構成と AI の挙動 */
	private CpuBattleMode cpuBattleMode = CpuBattleMode.ORIGIN;
	private int cpuLevel;
	private boolean humanGoesFirst;
	private boolean humansTurn;
	/** 人間側の「ターン開始」回数（先攻1ターン目の例外判定用） */
	private int humanTurnStarts;
	/** CPU側の「ターン開始」回数（先攻1ターン目の例外判定用） */
	private int cpuTurnStarts;
	private BattlePhase phase = BattlePhase.HUMAN_INPUT;
	/** 現在の手番の開始時刻（ms）。持ち時間カウント用（HUMAN_INPUT / CPU_THINKING のみ進む） */
	private long turnStartedAtMs;
	/**
	 * 持ち時間ペナルティ段階（0:90s → 1:60s → 2:30s → 3:15s。段階3で時間切れしたら強制降参）
	 * human はホスト側、cpu はゲスト側（CPU戦では cpu=CPU）。
	 */
	private int humanTimePenaltyStage;
	private int cpuTimePenaltyStage;
	/** 配置後、効果表示→resolve で処理するための保留 */
	private PendingEffect pendingEffect;
	/** 人間の選択が必要な場合の保留（任意効果/対象選択など） */
	private PendingChoice pendingChoice;
	/** エルフの巫女: 次に配置するファイター強さ+1 */
	private int humanNextDeployBonus;
	private int cpuNextDeployBonus;
	/** ウッドエルフ・森のハープ弾き: 次に配置するエルフなら加算（ターン終了まで {@link ZoneFighter#getTemporaryPowerBonus()}） */
	private int humanNextElfOnlyBonus;
	private int cpuNextElfOnlyBonus;
	/** 隊長: 次に配置するファイターのコストぶん強化（重ねがけ可） */
	private int humanNextDeployCostBonusTimes;
	private int cpuNextDeployCostBonusTimes;
	/** メカニック: 次に配置するファイターはコスト+1・強さ+3（ターン終わりまで）／重ねがけ可 */
	private int humanNextMechanicStacks;
	private int cpuNextMechanicStacks;
	/** 科学者: 強さ入れ替え（次のターン終了まで） */
	private boolean powerSwapActive;
	/** 古竜: 次の相手ターン終了までの一時強化（自分のレストのエルフ枚数ぶん） */
	private int humanKoryuBonus;
	private int cpuKoryuBonus;
	/** クリスタクル: 任意ストーン支払い後、次の配置に加算する強さ（未消費分） */
	private int humanNextCrystakulDeployBonus;
	private int cpuNextCrystakulDeployBonus;
	/** クリスタクル: 場のファイターに加算（次の相手ターン終了まで。beginTurnGainStone で解除） */
	private int humanCrystakulCombatBonus;
	private int cpuCrystakulCombatBonus;
	/** 「能力後も相手以上になれない」場合の確認用スナップショット（キャンセルで巻き戻す） */
	private CpuBattleState confirmAcceptLossSnapshot;
	/** SPEC-666: 次にホスト（human スロット）が配置するファイターをアンデッド扱いにする */
	private boolean spec666NextHumanUndead;
	/** SPEC-666: 次にゲスト／CPU が配置するファイターをアンデッド扱いにする */
	private boolean spec666NextCpuUndead;

	/**
	 * クラーケン〈配置〉: 次に当該プレイヤー（human スロット／cpu スロット）のターンが始まったとき、
	 * レストに「ソードフィッシュ」があれば手札に1枚加える予約の数（重ねがけ可）。
	 */
	private int humanKrakenNextTurnSwordfishAdds;
	private int cpuKrakenNextTurnSwordfishAdds;

	/**
	 * ラミエル〈配置〉: 次に当該プレイヤー（human スロット／cpu スロット）のターンが始まったときに手札へ加える「奇跡」の枚数（重ねがけ可）。
	 */
	private int humanRamielNextTurnMiracleAdds;
	private int cpuRamielNextTurnMiracleAdds;

	/** ザドキエル: ホストが「奇跡」をレストに置いた直後、次のバトルゾーン配置に相手ターン中 +3 を付与する予約 */
	private boolean humanPendingZadkielNextDeployOppTurnPower3;
	/** ザドキエル: ゲスト／CPU が「奇跡」をレストに置いた直後の同上 */
	private boolean cpuPendingZadkielNextDeployOppTurnPower3;

	/**
	 * ルシファー〈配置〉: バトル終了まで、当該スロットの手札へ加わる「奇跡」は「堕天使ルシファー」として扱う（既存の奇跡も変化済み）。
	 */
	private boolean humanMiraclesBecomeFallenLucifer;
	private boolean cpuMiraclesBecomeFallenLucifer;

	private List<BattleCard> humanDeck = new ArrayList<>();
	private List<BattleCard> humanHand = new ArrayList<>();
	private List<BattleCard> humanRest = new ArrayList<>();
	private ZoneFighter humanBattle;
	private int humanStones;

	/** 場に出ている〈フィールド〉（全プレイヤーで1枚のみ。上書きで入れ替わる） */
	private BattleCard activeField;
	/** 現在の〈フィールド〉を配置した側。true=ホスト（human スロット）、false=ゲスト（cpu スロット）。場に無いときは null */
	private Boolean activeFieldOwnerHuman;
	/**
	 * 廃棄工場 5C-R4P が場にあるときの残りターン表示用（4→1）。場に無い・廃棄工場でないときは 0。
	 * ターン開始ごとに減り、1 の相手ターン終了時に場から使用者レストへ移る。
	 */
	private int scrapyardFieldTurnsRemaining;
	/**
	 * 霊園教会 デスバウンス が場にあるときの残りターン（6→1）。場に無い・該当フィールドでないときは 0。
	 * ターン開始ごとに減り、1 の相手ターン終了時に場から使用者レストへ移る。
	 */
	private int deathbounceFieldTurnsRemaining;

	/**
	 * アトランティス〈フィールド〉の右上カウント表示（2→0）。該当フィールドでないときは 0。
	 */
	private int atlantisFieldCounterDisplay;
	/**
	 * アトランティス配置直後は true。フィールド所有者の次のターン開始時にカウント0を処理して false。
	 */
	private boolean atlantisAwaitingCount0;

	/**
	 * 週刊少年 CAMP の右上カウント（6→1）。該当〈フィールド〉でないときは 0。
	 * ターン開始ごとに 1 ずつ減り（1 の間は減らさない）、1 の相手ターン終了時に場から使用者レストへ。
	 */
	private int weeklyShonenCampFieldCounterDisplay;
	/** カウント2 マイルストーン到達後、〈常時〉相当のコミック+4（+2 と合わせて+6） */
	private boolean weeklyShonenCampCount2ComicBonus;
	/** カウント3 到達ターン中のみ。すべてのカードの配置コスト+1（ターン終了で解除） */
	private boolean weeklyShonenCampGlobalDeployCostPlusOneThisTurn;

	/**
	 * 世界の再構築〈フィールド〉のカウント（4→0）。該当〈フィールド〉でないときは 0。
	 * ターン開始ごとに 1 減らし、0 になったターンに条件を満たせばバトル開始時の手札・デッキ・ストーンに戻す。
	 */
	private int worldRebuildFieldCounterDisplay;
	/**
	 * ペーパーシティ〈フィールド〉の右上カウント（6→0）。該当でないときは 0。
	 * ターン開始ごとに 1 減らす。6 で配置直後にインクナイト、4 でインクナイト、2 でストーン+2、0 で場から所有者レストへ。
	 */
	private int paperCityFieldCounterDisplay;
	/**
	 * 鳥獣戯画〈フィールド〉が場にある間、次にホスト側（human スロット）が配置するファイターにドラゴン種族を付与する予約。
	 */
	private boolean chojuGigaPendingHumanSlotNextDeployDragon;
	/**
	 * 鳥獣戯画〈フィールド〉が場にある間、次にゲスト／CPU 側が配置するファイターに人間種族を付与する予約。
	 */
	private boolean chojuGigaPendingCpuSlotNextDeployHuman;

	/** バトル開始直後のホスト手札（不変スナップショット） */
	private List<BattleCard> worldRebuildOpenHumanHand = new ArrayList<>();
	/** バトル開始直後のホストデッキ（インデックス0 がドロー先頭） */
	private List<BattleCard> worldRebuildOpenHumanDeck = new ArrayList<>();
	private List<BattleCard> worldRebuildOpenCpuHand = new ArrayList<>();
	private List<BattleCard> worldRebuildOpenCpuDeck = new ArrayList<>();
	/** 先攻0・後攻1（バトル開始時の所持ストーン想定） */
	private int worldRebuildOpenHumanStones;
	private int worldRebuildOpenCpuStones;

	private List<BattleCard> cpuDeck = new ArrayList<>();
	private List<BattleCard> cpuHand = new ArrayList<>();
	private List<BattleCard> cpuRest = new ArrayList<>();
	private ZoneFighter cpuBattle;
	private int cpuStones;

	private String lastMessage;
	private boolean gameOver;
	private boolean humanWon;
	/** CPU 戦のみ。ミッション重複通知防止 */
	private boolean cpuWinMissionNotified;
	/** CPU 戦の人間プレイヤー */
	private Long cpuBattleUserId;
	/** CPU戦: プレイヤーのデッキID。対人戦: ホストのデッキ（humanスロット） */
	private Long humanSlotDeckId;
	/** 対人戦のみ: ゲストのデッキ（cpuスロット）。CPU戦では null */
	private Long cpuSlotDeckId;
	private List<String> eventLog = new ArrayList<>();
	/**
	 * 前列メインが配置・忍者入れ替え等で置かれるたびに増える。ガラクタレッグ系〈常時〉の「先出し」判定に使う。
	 */
	private int battleMainLineSeqCounter;

	/** @return 新しい前列メインの通し番号（1起算） */
	public int takeNextBattleMainLineSeq() {
		return ++battleMainLineSeqCounter;
	}

	public void addLog(String line) {
		eventLog.add(line);
		if (eventLog.size() > 40) {
			eventLog.remove(0);
		}
	}
}
