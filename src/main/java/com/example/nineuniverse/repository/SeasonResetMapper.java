package com.example.nineuniverse.repository;

public interface SeasonResetMapper {

	int deleteAllDeckEntries();

	int deleteAllDecks();

	int deleteAllLeagueDeckSets();

	int deleteAllDailyMissions();

	int deleteAllWeeklyMissions();

	int deleteAllUserEpithetsOwned();

	int resetAllUsersForSeason();

	int grantBeginnerEpithetsToAllUsers();
}
