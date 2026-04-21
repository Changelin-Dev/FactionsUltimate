package me.changelin.factions.listeners;

import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.access.ChunkAccessMenuSession;
import me.changelin.factions.access.ChunkAccessPermission;
import me.changelin.factions.core.Faction;
import me.changelin.factions.managers.FactionManager;
import me.changelin.factions.social.FactionRole;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

public class ChunkAccessListener implements Listener {

    private final FactionsPlugin plugin;

    public ChunkAccessListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean canBuild(Player player, Chunk chunk) {
        return hasAccess(player, chunk, ChunkAccessPermission.ACCESS_BUILD, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Container container)) {
            return;
        }

        if (hasAccess(player, container.getBlock().getChunk(), ChunkAccessPermission.ACCESS_CONTAINER, true)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "Vous ne pouvez pas ouvrir ce conteneur ici.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block.getState() instanceof InventoryHolder) {
            if (!hasAccess(player, block.getChunk(), ChunkAccessPermission.ACCESS_CONTAINER, true)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Vous ne pouvez pas ouvrir ce conteneur ici.");
            }
            return;
        }

        Material type = block.getType();
        boolean interactable = block.getBlockData() instanceof Openable
                || type.name().endsWith("BUTTON")
                || type.name().endsWith("LEVER")
                || type.name().endsWith("PRESSURE_PLATE")
                || type.name().contains("DOOR")
                || type.name().contains("TRAPDOOR");

        if (!interactable) {
            return;
        }

        if (!hasAccess(player, block.getChunk(), ChunkAccessPermission.ACCESS_INTERACT, false)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas interagir ici.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (!plugin.getChunkAccessMenuManager().isManagedTitle(title)) {
            return;
        }

        event.setCancelled(true);
        ChunkAccessMenuSession session = plugin.getChunkAccessMenuManager().getSession(player.getUniqueId());
        if (session == null) {
            return;
        }

        ChunkAccessPermission permission = plugin.getChunkAccessMenuManager().getPermissionAtSlot(event.getRawSlot());
        if (permission == null) {
            return;
        }

        boolean current = plugin.getChunkPermissionStore().hasPermission(session.getChunkId(), session.getTargetId(), permission);
        plugin.getChunkPermissionStore().setPermission(session.getChunkId(), session.getTargetId(), permission, !current);
        plugin.getChunkAccessMenuManager().refreshInventory(event.getInventory(), session);
        player.sendMessage(ChatColor.GREEN + permission.getDisplayName() + ChatColor.GRAY + " pour "
                + ChatColor.YELLOW + session.getTargetName() + ChatColor.GRAY + " : "
                + (!current ? ChatColor.GREEN + "active" : ChatColor.RED + "desactive"));
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event) {
        if (plugin.getChunkAccessMenuManager().isManagedTitle(event.getView().getTitle())) {
            plugin.getChunkAccessMenuManager().close(event.getPlayer().getUniqueId());
        }
    }

    private boolean hasAccess(Player player, Chunk chunk, ChunkAccessPermission permission, boolean allowPillage) {
        Faction owner = plugin.getFactionManager().getFactionAt(chunk);
        if (owner == null) {
            return true;
        }

        if (owner.isMember(player.getUniqueId())) {
            if (permission == ChunkAccessPermission.ACCESS_CONTAINER
                    && owner.getRole(player.getUniqueId()) == FactionRole.RECRUIT) {
                return false;
            }
            return true;
        }

        String chunkId = FactionManager.toChunkId(chunk);
        if (plugin.getChunkPermissionStore().hasPermission(chunkId, player.getUniqueId(), permission)) {
            return true;
        }

        if (allowPillage && permission == ChunkAccessPermission.ACCESS_CONTAINER && canPillage(player, owner)) {
            return true;
        }

        return false;
    }

    private boolean canPillage(Player player, Faction owner) {
        Faction attacker = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());
        return attacker != null
                && !attacker.getName().equalsIgnoreCase(owner.getName())
                && plugin.getPowerManager().isUnderPowered(owner);
    }
}
