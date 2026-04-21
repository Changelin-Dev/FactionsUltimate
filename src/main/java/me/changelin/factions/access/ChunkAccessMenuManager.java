package me.changelin.factions.access;

import me.changelin.factions.FactionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChunkAccessMenuManager {

    private static final String TITLE_PREFIX = ChatColor.DARK_GREEN + "Acces Chunk ";

    private final FactionsPlugin plugin;
    private final Map<UUID, ChunkAccessMenuSession> sessions = new HashMap<>();
    private final Map<Integer, ChunkAccessPermission> slots = new HashMap<>();

    public ChunkAccessMenuManager(FactionsPlugin plugin) {
        this.plugin = plugin;
        slots.put(2, ChunkAccessPermission.ACCESS_BUILD);
        slots.put(4, ChunkAccessPermission.ACCESS_INTERACT);
        slots.put(6, ChunkAccessPermission.ACCESS_CONTAINER);
    }

    public void openMenu(Player editor, Player target, String chunkId) {
        ChunkAccessProfile profile = plugin.getChunkPermissionStore().getProfile(chunkId, target.getUniqueId());
        Inventory inventory = Bukkit.createInventory(null, 9, buildTitle(target.getName()));

        for (Map.Entry<Integer, ChunkAccessPermission> entry : slots.entrySet()) {
            inventory.setItem(entry.getKey(), createPermissionItem(entry.getValue(), profile.has(entry.getValue())));
        }

        sessions.put(editor.getUniqueId(), new ChunkAccessMenuSession(editor.getUniqueId(), target.getUniqueId(), target.getName(), chunkId));
        editor.openInventory(inventory);
    }

    public boolean isManagedTitle(String title) {
        return title != null && title.startsWith(TITLE_PREFIX);
    }

    public ChunkAccessPermission getPermissionAtSlot(int slot) {
        return slots.get(slot);
    }

    public ChunkAccessMenuSession getSession(UUID editorId) {
        return sessions.get(editorId);
    }

    public void close(UUID editorId) {
        sessions.remove(editorId);
    }

    public Inventory rebuildInventory(ChunkAccessMenuSession session) {
        Inventory inventory = Bukkit.createInventory(null, 9, buildTitle(session.getTargetName()));
        populateInventory(inventory, session);
        return inventory;
    }

    public void refreshInventory(Inventory inventory, ChunkAccessMenuSession session) {
        populateInventory(inventory, session);
    }

    private void populateInventory(Inventory inventory, ChunkAccessMenuSession session) {
        ChunkAccessProfile profile = plugin.getChunkPermissionStore().getProfile(session.getChunkId(), session.getTargetId());
        for (Map.Entry<Integer, ChunkAccessPermission> entry : slots.entrySet()) {
            inventory.setItem(entry.getKey(), createPermissionItem(entry.getValue(), profile.has(entry.getValue())));
        }
    }

    private String buildTitle(String targetName) {
        return TITLE_PREFIX + targetName;
    }

    private ItemStack createPermissionItem(ChunkAccessPermission permission, boolean enabled) {
        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((enabled ? ChatColor.GREEN : ChatColor.RED) + permission.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Etat : " + (enabled ? ChatColor.GREEN + "Autorise" : ChatColor.RED + "Refuse"));
            lore.add(ChatColor.YELLOW + "Cliquez pour inverser.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
