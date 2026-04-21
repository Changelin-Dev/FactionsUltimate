package me.changelin.factions.hud;

import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.core.Faction;
import me.changelin.factions.social.RelationType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HudManager {

    private static final String OBJECTIVE_NAME = "factions_sidebar";

    private final FactionsPlugin plugin;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public HudManager(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refresh(player);
        }
    }

    public void refresh(Player viewer) {
        Scoreboard scoreboard = boards.computeIfAbsent(viewer.getUniqueId(), ignored -> Bukkit.getScoreboardManager().getNewScoreboard());
        viewer.setScoreboard(scoreboard);
        refreshSidebar(viewer, scoreboard);
        refreshTeams(viewer, scoreboard);
    }

    public String formatChatDisplay(Player viewer, Player target) {
        Faction viewerFaction = plugin.getFactionManager().getFactionByPlayer(viewer.getUniqueId());
        Faction targetFaction = plugin.getFactionManager().getFactionByPlayer(target.getUniqueId());
        RelationType relation = plugin.getFactionManager().getRelationBetween(viewerFaction, targetFaction);
        if (viewerFaction != null && targetFaction != null && viewerFaction.getName().equalsIgnoreCase(targetFaction.getName())) {
            relation = RelationType.ALLY;
        }

        String prefix = targetFaction != null ? "[" + targetFaction.getName() + "] " : "";
        return relation.getColor() + prefix + ChatColor.RESET + relation.getColor() + target.getName() + ChatColor.RESET;
    }

    private void refreshSidebar(Player viewer, Scoreboard scoreboard) {
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy", ChatColor.GOLD + "Factions");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        Faction faction = plugin.getFactionManager().getFactionByPlayer(viewer.getUniqueId());
        String factionName = faction != null ? faction.getName() : "Aucune";
        String bank = faction != null ? formatMoney(faction.getBankBalance()) : "0";
        String online = faction != null ? String.valueOf(countOnline(faction)) : "0";

        objective.getScore(ChatColor.YELLOW + "Faction: " + ChatColor.WHITE + factionName).setScore(5);
        objective.getScore(ChatColor.YELLOW + "Power: " + ChatColor.WHITE
                + plugin.getPowerManager().getCurrentPower(viewer.getUniqueId())
                + "/" + plugin.getPowerManager().getMaxPower(viewer.getUniqueId())).setScore(4);
        objective.getScore(ChatColor.YELLOW + "En ligne: " + ChatColor.WHITE + online).setScore(3);
        objective.getScore(ChatColor.YELLOW + "Richesse: " + ChatColor.WHITE + bank).setScore(2);
        objective.getScore(ChatColor.DARK_GRAY + " ").setScore(1);
    }

    private void refreshTeams(Player viewer, Scoreboard scoreboard) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            String teamName = "p_" + target.getUniqueId().toString().replace("-", "").substring(0, 14);
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }

            clearEntryFromOtherTeams(scoreboard, target.getName(), teamName);
            if (!team.hasEntry(target.getName())) {
                team.addEntry(target.getName());
            }

            Faction viewerFaction = plugin.getFactionManager().getFactionByPlayer(viewer.getUniqueId());
            Faction targetFaction = plugin.getFactionManager().getFactionByPlayer(target.getUniqueId());
            RelationType relation = plugin.getFactionManager().getRelationBetween(viewerFaction, targetFaction);
            if (viewerFaction != null && targetFaction != null && viewerFaction.getName().equalsIgnoreCase(targetFaction.getName())) {
                relation = RelationType.ALLY;
            }

            team.setColor(relation.getColor());
            team.setPrefix(relation.getColor().toString());
            team.setSuffix(targetFaction != null ? ChatColor.GRAY + " [" + targetFaction.getName() + "]" : "");
        }
    }

    private void clearEntryFromOtherTeams(Scoreboard scoreboard, String entry, String currentTeam) {
        for (Team team : scoreboard.getTeams()) {
            if (!team.getName().equals(currentTeam) && team.hasEntry(entry)) {
                team.removeEntry(entry);
            }
        }
    }

    private int countOnline(Faction faction) {
        int count = 0;
        for (UUID memberId : faction.getMembers()) {
            Player player = Bukkit.getPlayer(memberId);
            if (player != null && player.isOnline()) {
                count++;
            }
        }
        return count;
    }

    private String formatMoney(double amount) {
        return String.format("%.2f$", amount);
    }
}
