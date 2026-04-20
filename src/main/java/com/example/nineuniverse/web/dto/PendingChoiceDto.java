package com.example.nineuniverse.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PendingChoiceDto(
		String kind,
		String prompt,
		@JsonProperty("forHuman") boolean forHuman,
		@JsonProperty("cpuSlotChooses") boolean cpuSlotChooses,
		String abilityDeployCode,
		int stoneCost,
		List<String> optionInstanceIds,
		boolean viewerMayRespond
) {
}

