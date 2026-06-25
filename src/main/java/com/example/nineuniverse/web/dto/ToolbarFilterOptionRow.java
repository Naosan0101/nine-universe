package com.example.nineuniverse.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ツールバー絞り込みドロップダウン1行（{@code tribe-filter-dropdown.js} 用）。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolbarFilterOptionRow {

	private String v;
	private String t;
}
