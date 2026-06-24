package com.example.nineuniverse.repository;

public interface SeasonResetMapper {

	int deleteAllUserCollections();

	int deleteAllDeckEntries();

	int deleteAllDecks();

	int deleteAllLeagueDeckSets();

	int deleteAllDailyMissions();

	int deleteAllWeeklyMissions();

	int deleteAllUserEpithetsOwned();

	int resetAllUsersForSeason();

	int grantBeginnerEpithetsToAllUsers();
}
