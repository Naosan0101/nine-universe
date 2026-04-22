package com.example.nineuniverse.domain;

import lombok.Data;

@Data
public class PvpInviteNotice {
	private long inviteId;
	private String matchId;
	private long counterpartUserId;
	/** 相手側に表示する名前（申し込み受信なら申込者、送信なら相手フレンド） */
	private String counterpartDisplayName;
}
