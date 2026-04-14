package com.example.nineuniverse.domain;

import java.time.LocalDate;
import lombok.Data;

@Data
public class UserDailyMission {
	private Long userId;
	private LocalDate missionDate;
	private Short slot;
	private String missionCode;
	private String title;
	private Integer targetCount;
	private Integer progress;
	private Boolean rewardGranted;
	/** 受け取り時に付与するジェム数（デイリーは3〜4） */
	private Integer rewardGems;
}
