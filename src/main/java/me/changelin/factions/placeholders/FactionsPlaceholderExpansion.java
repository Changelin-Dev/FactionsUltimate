package me.changelin.factions.placeholders;

import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.core.Faction;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class FactionsPlaceholderExpansion extends PlaceholderExpansion {

    private final FactionsPlugin plugin;

    public FactionsPlaceholderExpansion(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "factions";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Changelin";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || player.getUniqueId() == null) {
            return "";
        }

        Faction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());

        return switch (params.toLowerCase(Locale.ROOT)) {
            case "name" -> faction != null ? faction.getName() : "Aucune";
            case "power" -> String.valueOf(plugin.getPowerManager().getCurrentPower(player.getUniqueId()));
            case "power_max" -> String.valueOf(plugin.getPowerManager().getMaxPower(player.getUniqueId()));
            case "land_count" -> String.valueOf(faction != null ? faction.getLandCount() : 0);
            case "is_overclaimed" -> faction != null && plugin.getPowerManager().isUnderPowered(faction) ? "Oui" : "Non";
            default -> null;
        };
    }
}
