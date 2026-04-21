package com.example.nineuniverse.web.dto;

import lombok.Data;

/** リサイクル確定パック開封時に選ぶ「スタンダードパック」1種。 */
@Data
public class RecycleStandardPackOption {
	private String packTypeParam;
	private String displayName;
	/** {@link com.example.nineuniverse.GameConstants#packArtImageUrl} 相当の相対URL */
	private String packArtImageUrl;
	/** パック購入画面と同じ詳細モーダル（{@code data-open-pack-detail} 用の id） */
	private String packDetailModalId;
}
