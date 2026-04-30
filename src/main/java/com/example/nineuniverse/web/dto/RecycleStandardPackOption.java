package com.example.nineuniverse.web.dto;

import lombok.Data;

/** リサイクル確定パック開封時に選ぶ「スタンダードパック」1種。 */
@Data
public class RecycleStandardPackOption {
	private String packTypeParam;
	private String displayName;
	/** 互換・デバッグ用（ASCII）。表示は {@link #packThumbUrl} を使用。 */
	private String packThumbKey;
	/** パック絵サムネ URL（{@code /images/cards/…}、クエリ {@code v} はキャッシュバスター用） */
	private String packThumbUrl;
	/** パック購入画面と同じ詳細モーダル（{@code data-open-pack-detail} 用の id） */
	private String packDetailModalId;
}
