package me.changelin.factions.social;

public enum FactionUpgrade {
    LEVEL_1(1, 10, 1.0, 0.0, false, false),
    LEVEL_2(2, 15, 1.15, 5000.0, true, false),
    LEVEL_3(3, 20, 1.30, 15000.0, true, true);

    private final int level;
    private final int maxMembers;
    private final double powerMultiplier;
    private final double upgradeCost;
    private final boolean homeUnlocked;
    private final boolean expertQuestsUnlocked;

    FactionUpgrade(int level, int maxMembers, double powerMultiplier, double upgradeCost, boolean homeUnlocked,
                   boolean expertQuestsUnlocked) {
        this.level = level;
        this.maxMembers = maxMembers;
        this.powerMultiplier = powerMultiplier;
        this.upgradeCost = upgradeCost;
        this.homeUnlocked = homeUnlocked;
        this.expertQuestsUnlocked = expertQuestsUnlocked;
    }

    public int getLevel() {
        return level;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public double getPowerMultiplier() {
        return powerMultiplier;
    }

    public double getUpgradeCost() {
        return upgradeCost;
    }

    public boolean isHomeUnlocked() {
        return homeUnlocked;
    }

    public boolean isExpertQuestsUnlocked() {
        return expertQuestsUnlocked;
    }

    public FactionUpgrade next() {
        return switch (this) {
            case LEVEL_1 -> LEVEL_2;
            case LEVEL_2 -> LEVEL_3;
            case LEVEL_3 -> null;
        };
    }

    public static FactionUpgrade fromLevel(int level) {
        for (FactionUpgrade upgrade : values()) {
            if (upgrade.level == level) {
                return upgrade;
            }
        }
        return LEVEL_1;
    }
}
