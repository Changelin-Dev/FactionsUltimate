package me.changelin.factions;

import me.changelin.factions.managers.FactionManager;
import me.changelin.factions.commands.CmdFaction;
import me.changelin.factions.listeners.FactionListener;

import org.bukkit.plugin.java.JavaPlugin;

public class FactionsPlugin extends JavaPlugin {

    private static FactionsPlugin instance;
    private FactionManager factionManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialisation de la logique
        this.factionManager = new FactionManager(this);
        
        getCommand("f").setExecutor(new CmdFaction(this));
        
        getLogger().info("Factions Ultimate : Phase 1 activée !");

        getServer().getPluginManager().registerEvents(new FactionListener(this), this);
    
        getLogger().info("FactionsUltimate est prêt !");
    }

    public static FactionsPlugin getInstance() { return instance; }
    public FactionManager getFactionManager() { return factionManager; }
}