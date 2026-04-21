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
        Faction owner = plugin.getFactionManager().getOwnerAt(event.getBlock().getChunk());

        if (owner != null) {
            // Si le joueur n'est pas dans la faction propriétaire
            if (!owner.getMembers().contains(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage("§cCe terrain appartient à §e" + owner.getName());
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Faction owner = plugin.getFactionManager().getOwnerAt(event.getBlock().getChunk());

        if (owner != null && !owner.getMembers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cVous ne pouvez pas construire ici (§e" + owner.getName() + "§c)");
        }
    }
}