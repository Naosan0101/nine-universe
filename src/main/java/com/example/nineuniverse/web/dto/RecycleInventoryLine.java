package com.example.nineuniverse.web.dto;

import com.example.nineuniverse.domain.LibraryCardView;
import lombok.Data;

@Data
public class RecycleInventoryLine {
	private LibraryCardView card;
	private int owned;
	private int inDecks;
	private int recyclable;
	private int crystalPerCard;
}
