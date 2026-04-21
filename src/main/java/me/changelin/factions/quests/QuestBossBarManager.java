package me.changelin.factions.quests;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestBossBarManager {

    private final JavaPlugin plugin;

    public QuestBossBarManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void showProgress(Player player, String objective, double progress) {
        BossBar bossBar = Bukkit.createBossBar("Quete: " + objective, BarColor.BLUE, BarStyle.SOLID);
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
        bossBar.addPlayer(player);

        Bukkit.getScheduler().runTaskLater(plugin, bossBar::removeAll, 20L * 6L);
    }
}
