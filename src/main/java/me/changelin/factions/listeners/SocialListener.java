package me.changelin.factions.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.changelin.factions.FactionsPlugin;
import me.changelin.factions.core.Faction;
import me.changelin.factions.social.RelationType;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SocialListener implements Listener {

    private final FactionsPlugin plugin;

    public SocialListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(plugin, plugin.getHudManager()::refreshAll);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTask(plugin, plugin.getHudManager()::refreshAll);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = getDamager(event.getDamager());
        if (!(event.getEntity() instanceof Player victim) || attacker == null) {
            return;
        }

        Faction attackerFaction = plugin.getFactionManager().getFactionByPlayer(attacker.getUniqueId());
        Faction victimFaction = plugin.getFactionManager().getFactionByPlayer(victim.getUniqueId());
        RelationType relation = plugin.getFactionManager().getRelationBetween(attackerFaction, victimFaction);
        if (attackerFaction != null && victimFaction != null
                && attackerFaction.getName().equalsIgnoreCase(victimFaction.getName())) {
            relation = RelationType.ALLY;
        }

        if (relation == RelationType.ALLY) {
            event.setCancelled(true);
            attacker.sendMessage("§cVous ne pouvez pas blesser un membre ou un allie.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String content = PlainTextComponentSerializer.plainText().serialize(event.message());
        event.setCancelled(true);

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                String display = plugin.getHudManager().formatChatDisplay(viewer, sender);
                viewer.sendMessage(display + " §7» §f" + content);
            }
        });
    }

    private Player getDamager(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }
}
