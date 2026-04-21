package me.changelin.factions.social;

public enum FactionRole {
    LEADER("Chef", 100),
    COLEADER("Officier", 75),
    MEMBER("Membre", 50),
    RECRUIT("Recrue", 10);

    private final String displayName;
    private final int weight;

    FactionRole(String displayName, int weight) {
        this.displayName = displayName;
        this.weight = weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean atLeast(FactionRole other) {
        return weight >= other.weight;
    }
}
