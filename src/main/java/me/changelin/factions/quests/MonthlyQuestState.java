package me.changelin.factions.quests;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MonthlyQuestState {

    private String monthKey;
    private String questId;
    private String objective;
    private double targetPoints;
    private double totalPoints;
    private Map<UUID, QuestContribution> contributions = new HashMap<>();

    public MonthlyQuestState() {
    }

    public MonthlyQuestState(String monthKey, String questId, String objective, double targetPoints) {
        this.monthKey = monthKey;
        this.questId = questId;
        this.objective = objective;
        this.targetPoints = targetPoints;
        this.totalPoints = 0.0;
        this.contributions = new HashMap<>();
    }

    public String getMonthKey() {
        return monthKey;
    }

    public String getQuestId() {
        return questId;
    }

    public String getObjective() {
        return objective;
    }

    public double getTargetPoints() {
        return targetPoints;
    }

    public double getTotalPoints() {
        return totalPoints;
    }

    public Map<UUID, QuestContribution> getContributions() {
        return contributions;
    }

    public QuestContribution getContribution(UUID playerId) {
        return contributions.computeIfAbsent(playerId, ignored -> new QuestContribution());
    }

    public void addContribution(UUID playerId, double points) {
        double safePoints = Math.max(0.0, points);
        totalPoints += safePoints;
        getContribution(playerId).addPoints(safePoints);
    }

    public double getProgress() {
        if (targetPoints <= 0.0) {
            return 0.0;
        }
        return Math.min(1.0, totalPoints / targetPoints);
    }
}
