package me.changelin.factions.listeners;

import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.core.Faction;
import me.changelin.factions.managers.FactionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public class RaidListener implements Listener {

    private final FactionsPlugin plugin;

    public RaidListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        boolean supportedExplosion = event.getEntity() instanceof TNTPrimed
                || event.getEntity() instanceof Creeper;
        if (!supportedExplosion) {
            return;
        }

        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            Faction owner = plugin.getFactionManager().getFactionAt(block.getChunk());
            if (owner == null) {
                continue;
            }

            if (block.getType() == Material.OBSIDIAN) {
                String blockKey = FactionManager.toBlockKey(block);
                int remaining = plugin.getRaidDataStore().damageObsidian(blockKey);
                if (remaining > 0) {
                    iterator.remove();
                    notifyNearbyPlayers(block, ChatColor.RED + "Obsidienne touchee : " + remaining + " coup(s) restants.");
                } else {
                    notifyNearbyPlayers(block, ChatColor.GOLD + "Une obsidienne a cede sous les explosions.");
                }
                continue;
            }

            if (!plugin.getPowerManager().isUnderPowered(owner)) {
                iterator.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.OBSIDIAN) {
            plugin.getRaidDataStore().clear(FactionManager.toBlockKey(event.getBlock()));
        }
    }

    private void notifyNearbyPlayers(Block origin, String message) {
        for (Player player : origin.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(origin.getLocation()) <= 256) {
                player.sendMessage(message);
            }
        }
    }
}
