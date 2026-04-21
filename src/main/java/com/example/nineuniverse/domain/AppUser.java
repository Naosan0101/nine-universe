package com.example.nineuniverse.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AppUser {
	private Long id;
	private String username;
	/** バトルなどに表示する名前（ログインIDの {@link #username} とは別に変更可） */
	private String displayName;
	private String passwordHash;
	private Integer coins;
	/** カードリサイクルで貯まるクリスタル（ポイント） */
	private Integer recycleCrystal;
	/** 初回ホーム訪問時のウェルカムジェムを既に付与したか（新規ユーザーは false から開始） */
	private Boolean welcomeHomeBonusGranted;
	private LocalDate lastMissionDate;
	private LocalDateTime createdAt;
	/** 最終アクセス時刻 */
	private LocalDateTime lastAccessAt;

	/** CPU戦で「考え中」の待ち時間の長さ（FAST / NORMAL / SLOW） */
	private String cpuThinkSpeed;

	/** 無料スタンダードパック用ゲージのサイクル開始（この時刻から12時間でMAX） */
	private Instant timePackCycleStart;

	/**
	 * 新規登録プレゼントの「スタンダードパック1」未開封数（0 なら表示・開封不可）。
	 */
	private Integer starterGiftStandard1Remaining;
}
