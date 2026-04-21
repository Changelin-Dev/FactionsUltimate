package me.changelin.factions.listeners;

import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.quests.MonthlyQuestService;
import me.changelin.factions.quests.MonthlyQuestState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class QuestListener implements Listener {

    public static final String QUEST_TITLE = ChatColor.DARK_AQUA + "Entrepot Mensuel";

    private final FactionsPlugin plugin;

    public QuestListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openDepositInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, QUEST_TITLE);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (!QUEST_TITLE.equals(event.getView().getTitle())) {
            return;
        }

        ItemStack[] contents = event.getInventory().getContents();
        MonthlyQuestService.QuestDepositResult result = plugin.getQuestService().deposit(player, contents);

        event.getInventory().clear();
        for (ItemStack item : contents) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            if (plugin.getQuestService().getPointsFor(item.getType()) <= 0.0) {
                player.getInventory().addItem(item);
            }
        }

        if (result.points() <= 0.0) {
            player.sendMessage("§eAucun item valide pour la quete mensuelle n'a ete depose.");
            return;
        }

        MonthlyQuestState quest = plugin.getQuestService().getCurrentQuest();
        player.sendMessage("§aContribution ajoutee : §e" + String.format(Locale.US, "%.1f", result.points()) + " pts");
        player.sendMessage("§7Progression globale : §e" + plugin.getQuestService().formatProgress());
        plugin.getQuestBossBarManager().showProgress(player, quest.getObjective(), result.progress());
    }
}
