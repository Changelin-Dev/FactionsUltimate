package me.changelin.factions.social;

import org.bukkit.ChatColor;

public enum RelationType {
    ALLY("Allie", ChatColor.GREEN),
    NEUTRAL("Neutre", ChatColor.GRAY),
    ENEMY("Ennemi", ChatColor.RED);

    private final String displayName;
    private final ChatColor color;

    RelationType(String displayName, ChatColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }
}
