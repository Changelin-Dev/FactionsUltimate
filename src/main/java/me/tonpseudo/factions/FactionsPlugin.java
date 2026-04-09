package me.changelin.factions;

import me.changelin.factions.managers.FactionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FactionsPlugin extends JavaPlugin {

    private static FactionsPlugin instance;
    private FactionManager factionManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialisation de la logique
        this.factionManager = new FactionManager();
        
        getLogger().info("Factions Ultimate : Phase 1 activée !");
    }

    public static FactionsPlugin getInstance() { return instance; }
    public FactionManager getFactionManager() { return factionManager; }
}