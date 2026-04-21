package me.changelin.factions.power;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PowerListener implements Listener {

    private final PowerManager powerManager;

    public PowerListener(PowerManager powerManager) {
        this.powerManager = powerManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        powerManager.ensureProfile(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getEntity() != null) {
            powerManager.handleDeath(event.getEntity());
        }
    }
}
