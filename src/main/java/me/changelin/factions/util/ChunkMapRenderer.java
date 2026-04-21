package me.changelin.factions.util;

import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.core.Faction;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class ChunkMapRenderer {

    private static final int RADIUS = 2;

    private ChunkMapRenderer() {
    }

    public static void send(Player player, FactionsPlugin plugin) {
        Chunk center = player.getLocation().getChunk();
        World world = center.getWorld();
        Faction viewerFaction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());

        player.sendMessage(ChatColor.GOLD + "--- Carte des claims ---");
        for (int z = RADIUS; z >= -RADIUS; z--) {
            StringBuilder line = new StringBuilder();
            for (int x = -RADIUS; x <= RADIUS; x++) {
                Faction owner = plugin.getFactionManager().getFactionAt(world.getChunkAt(center.getX() + x, center.getZ() + z));

                if (x == 0 && z == 0) {
                    line.append(ChatColor.YELLOW).append('+');
                    continue;
                }

                if (owner == null) {
                    line.append(ChatColor.GRAY).append('-');
                } else if (viewerFaction != null && owner.getName().equalsIgnoreCase(viewerFaction.getName())) {
                    line.append(ChatColor.GREEN).append('O');
                } else {
                    line.append(ChatColor.RED).append('X');
                }
                line.append(ChatColor.DARK_GRAY).append(' ');
            }
            player.sendMessage(line.toString());
        }
        player.sendMessage(ChatColor.GRAY + "+ = Votre position | " + ChatColor.GREEN + "O" + ChatColor.GRAY + " = Vous | "
                + ChatColor.RED + "X" + ChatColor.GRAY + " = Ennemi | " + ChatColor.GRAY + "-" + ChatColor.GRAY + " = Libre");
    }
}
