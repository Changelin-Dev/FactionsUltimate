package me.changelin.factions.quests;

public class QuestContribution {

    private double totalPoints;
    private boolean claimedTier1;
    private boolean claimedTier2;
    private boolean claimedTier3;

    public double getTotalPoints() {
        return totalPoints;
    }

    public void addPoints(double amount) {
        totalPoints += Math.max(0.0, amount);
    }

    public boolean hasClaimedTier(int tier) {
        return switch (tier) {
            case 1 -> claimedTier1;
            case 2 -> claimedTier2;
            case 3 -> claimedTier3;
            default -> true;
        };
    }

    public void setClaimedTier(int tier) {
        switch (tier) {
            case 1 -> claimedTier1 = true;
            case 2 -> claimedTier2 = true;
            case 3 -> claimedTier3 = true;
            default -> {
            }
        }
    }
}
