package me.changelin.factions.quests;

public class QuestHistoryEntry {

    private String factionName;
    private String questId;
    private String monthKey;
    private String completedAt;

    public QuestHistoryEntry() {
    }

    public QuestHistoryEntry(String factionName, String questId, String monthKey, String completedAt) {
        this.factionName = factionName;
        this.questId = questId;
        this.monthKey = monthKey;
        this.completedAt = completedAt;
    }

    public String getFactionName() {
        return factionName;
    }

    public String getQuestId() {
        return questId;
    }

    public String getMonthKey() {
        return monthKey;
    }

    public String getCompletedAt() {
        return completedAt;
    }
}
