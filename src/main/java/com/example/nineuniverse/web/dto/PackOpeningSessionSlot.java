package com.example.nineuniverse.web.dto;

import java.io.Serializable;

/**
 * パック開封演出用セッション。時間ゲージのボーナスでカードと二つ名が混在する場合の順序を保持する。
 */
public record PackOpeningSessionSlot(String kind, Short cardId, String epithetUpper, String epithetLower)
		implements Serializable {

	public static PackOpeningSessionSlot card(short id) {
		return new PackOpeningSessionSlot("CARD", id, null, null);
	}

	public static PackOpeningSessionSlot epithet(String upper, String lower) {
		return new PackOpeningSessionSlot("EPITHET", null, upper, lower);
	}
}
