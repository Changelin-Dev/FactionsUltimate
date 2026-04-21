package me.changelin.factions.listeners;

import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.core.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class FactionListener implements Listener {

    private final FactionsPlugin plugin;

    public FactionListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Faction owner = plugin.getFactionManager().getFactionAt(event.getBlock().getChunk());

        if (owner != null && !plugin.getChunkAccessListener().canBuild(player, event.getBlock().getChunk())) {
            event.setCancelled(true);
            player.sendMessage("§cCe terrain appartient a §e" + owner.getName());
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Faction owner = plugin.getFactionManager().getFactionAt(event.getBlock().getChunk());

        if (owner != null && !plugin.getChunkAccessListener().canBuild(player, event.getBlock().getChunk())) {
            event.setCancelled(true);
            player.sendMessage("§cVous ne pouvez pas construire ici (§e" + owner.getName() + "§c)");
        }
    }
}
