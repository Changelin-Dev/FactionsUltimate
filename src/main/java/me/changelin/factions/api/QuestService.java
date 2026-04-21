package me.changelin.factions.api;

import me.changelin.factions.quests.QuestHistoryEntry;
import me.changelin.factions.quests.MonthlyQuestState;

import java.util.List;

public interface QuestService extends AutoSavable {
    List<QuestHistoryEntry> getHistory();

    void recordHistory(QuestHistoryEntry entry);

    MonthlyQuestState getCurrentQuest();
}
